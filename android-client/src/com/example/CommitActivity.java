package com.example;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
        row.setBackgroundResource(R.drawable.border);
        return row;
    }

    private void createCommitsList(Collection<RepositoryCommit> commits) {
        LinearLayout layout = (LinearLayout)findViewById(R.id.commitsLayout);
        layout.removeAllViewsInLayout();
        for(RepositoryCommit commit: commits){
            String author = commit.getCommit().getCommitter().getName();
            String hash = commit.getSha();
            String message = commit.getCommit().getMessage();
            Date date = commit.getCommit().getCommitter().getDate();
            layout.addView(createCommitInfo(date, author, hash, message));
        }
    }

    private class GetCommitsListTask extends AsyncTask<Object, Integer, LinkedList<RepositoryCommit>> {
        private LinkedList<RepositoryCommit> commits;
        private ProgressDialog dialog;
        @Override
        protected LinkedList<RepositoryCommit> doInBackground(Object... objects) {
            Repository repository = (Repository)objects[0];
            Boolean reloadFromServer = (Boolean)objects[1];
            commits = repoCommits.get(repository.getName());
            if(commits == null || reloadFromServer){
                userPics.clear();
                commits = loadCommits((Repository)objects[0]);
                repoCommits.put(repository.getName(), commits);
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
                task.execute(repository, true);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}