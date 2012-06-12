package com.example;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.service.CommitService;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kpetrov
 * Date: 11.06.12
 * Time: 0:01
 * To change this template use File | Settings | File Templates.
 */
public class CommitActivity extends TemplateActivity {
    private Repository repository;
    private static final String TAG = "CommitActivity";
    private boolean RELOAD_FROM_SERVER;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = (Repository) getIntent().getSerializableExtra(CommitActivity.class.getCanonicalName());
        setContentView(R.layout.commits);
        GetRepoListTask task = new GetRepoListTask();
        task.execute(repository);
    }

    private List<RepositoryCommit> loadCommits(Repository repo){
        CommitService service = new CommitService(createClientFromPreferences());
        try {
            return service.getCommits(repo);
        } catch (IOException e) {
            Log.e(TAG, "IOException while getting commits history for repository "+ repository.getName());
            return new LinkedList<RepositoryCommit>();
        }
    }

    private LinearLayout createCommitInfo(Date date, String author, String hash, String message){
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setPadding(0,0,0,8);

        LinearLayout mini = new LinearLayout(this);
        mini.setOrientation(LinearLayout.HORIZONTAL);
        mini.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout micro = new LinearLayout(this);
        micro.setOrientation(LinearLayout.VERTICAL);
        micro.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView dateView = new TextView(this);
        dateView.setText(date.toLocaleString());
        TextView authorView = new TextView(this);
        authorView.setText(author);
        TextView hashView = new TextView(this);
        hashView.setText(hash.substring(0,10));

        micro.addView(dateView);
        micro.addView(authorView);
        micro.addView(hashView);

        mini.addView(micro);
        ImageView image = new ImageView(this);
        image.setImageBitmap(getUserPicture(createClientFromPreferences(), author));
        mini.addView(image);
        row.addView(mini);
        TextView messageView = new TextView(this);
        messageView.setText(message);
        row.addView(messageView);
        return row;
    }

    private void createCommitsList(Collection<RepositoryCommit> commits) {
        LinearLayout layout = (LinearLayout)findViewById(R.id.commitsLayout);
        for(RepositoryCommit commit: commits){
            String author = commit.getCommit().getCommitter().getName();
            String hash = commit.getSha();
            String message = commit.getCommit().getMessage();
            Date date = commit.getCommit().getCommitter().getDate();
            layout.addView(createCommitInfo(date, author, hash, message));
        }
    }

    private class GetRepoListTask extends AsyncTask<Repository, Integer, Collection<RepositoryCommit>> {
        private Collection<RepositoryCommit> commits;
        private ProgressDialog dialog;
        @Override
        protected Collection<RepositoryCommit> doInBackground(Repository... repositories) {
            commits = repoCommits.get(repositories[0].getName());
            if(commits == null || RELOAD_FROM_SERVER){
                commits = loadCommits(repositories[0]);
                repoCommits.put(repositories[0].getName(), commits);
            }
            return commits;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(CommitActivity.this, "List of commits","Loading...");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Collection<RepositoryCommit> o) {
            createCommitsList(commits);

            dialog.dismiss();
            super.onPostExecute(o);
        }
    }
}