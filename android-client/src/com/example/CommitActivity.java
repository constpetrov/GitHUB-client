package com.example;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
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
    private static final String TAG = "github-client/CommitActivity";
    private final float TEXT_SIZE = 16;
    private ProgressDialog dialog;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = (Repository) getIntent().getSerializableExtra(CommitActivity.class.getCanonicalName());
        setContentView(R.layout.commits);
        GetCommitsListTask task = new GetCommitsListTask();
        task.execute(repository, false);
    }

    private LinkedList<RepositoryCommit> loadCommits(Repository repo){
        CommitService service = new CommitService(createClientFromPreferences());
        try {
            LinkedList<RepositoryCommit> commits = new LinkedList<RepositoryCommit>();
            commits.addAll(service.getCommits(repo));
            return commits;
        } catch (IOException e) {
            Log.e(TAG, "IOException while getting commits history for repository "+ repository.getName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(dialog != null){
                        dialog.dismiss();
                    }
                    generateIOExceptionToast();

                }
            });
            return new LinkedList<RepositoryCommit>();
        }
    }

    private LinearLayout createCommitInfo(Date date, String author, String hash, String message){
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout v = (LinearLayout)vi.inflate(R.layout.commititem, null);

        TextView dateView = (TextView) v.findViewById(R.id.commit_date);
        dateView.setText(date.toLocaleString());

        TextView authorView = (TextView) v.findViewById(R.id.commit_author);
        authorView.setText(author);

        TextView hashView = (TextView) v.findViewById(R.id.commit_hash);
        hashView.setText(hash.substring(0,10));

        TextView messageView = (TextView) v.findViewById(R.id.commit_message);
        messageView.setText(message);

        ImageView image = (ImageView)v.findViewById(R.id.commit_userpic);
        image.setImageBitmap(getUserPicture(createClientFromPreferences(), author));
        return v;
    }

    private void createCommitsList(Collection<RepositoryCommit> commits) {
        LinearLayout layout = (LinearLayout)findViewById(R.id.commitsLayout);
        layout.removeAllViewsInLayout();
        for(RepositoryCommit commit: commits){
            String author = commit.getCommit().getCommitter().getName();
            String hash = commit.getSha();
            String message = commit.getCommit().getMessage();
            Date date = commit.getCommit().getCommitter().getDate();
            layout.addView(createCommitInfo(date, author, hash, message), layout.getChildCount(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        }
    }

    private class GetCommitsListTask extends AsyncTask<Object, Integer, LinkedList<RepositoryCommit>> {
        private LinkedList<RepositoryCommit> commits;

        @Override
        protected LinkedList<RepositoryCommit> doInBackground(Object... objects) {
            Repository repository = (Repository)objects[0];
            Boolean reloadFromServer = (Boolean)objects[1];
            commits = repoCommits.get(repository.getName());
            if(commits == null || commits.size() == 0 || reloadFromServer){
                userPics.clear();
                commits = loadCommits((Repository)objects[0]);
                if(commits.size() != 0){
                    repoCommits.put(repository.getName(), commits);
                }
                try {
                    savePersistentCommits();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot save commits", e);
                }
            }
            for(RepositoryCommit commit:commits){
                getUserPicture(createClientFromPreferences(), commit.getCommit().getCommitter().getName());
            }
            return commits;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(CommitActivity.this, "List of commits","Loading...");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(LinkedList<RepositoryCommit> o) {
            createCommitsList(commits);
            dialog.dismiss();
            super.onPostExecute(o);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case REFRESH_MENU_ITEM:
                GetCommitsListTask task = new GetCommitsListTask();
                task.execute(repository, Boolean.TRUE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}