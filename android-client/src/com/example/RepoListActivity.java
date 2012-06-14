package com.example;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kpetrov
 * Date: 04.06.12
 * Time: 23:36
 * To change this template use File | Settings | File Templates.
 */
public class RepoListActivity extends TemplateActivity {
    private static final String TAG = "RepoList";
    LinearLayout layout;

    private boolean RELOAD_FROM_SERVER;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repolist);
        layout = (LinearLayout) findViewById(R.id.repoItemsContainer);
        ((LinearLayout)findViewById(R.id.repoList)).addView(createUserRow(),0);
        GitHubClient client = createClientFromPreferences();
        task = new GetRepoListTask();
        task.execute(client, false);
    }

    private void createRepoList(Collection<Repository> repos, LinearLayout layout) {
        layout.removeAllViewsInLayout();
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (final Repository repo : repos) {
            TextView repoText = (TextView)vi.inflate(R.layout.repoitem, null);
            repoText.setText(repo.getName());
            repoText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent repoDetailsIntent = new Intent(RepoListActivity.this, RepoDetailsActivity.class);
                    repoDetailsIntent.putExtra(RepoDetailsActivity.class.getCanonicalName(), repo);
                    startActivity(repoDetailsIntent);
                }
            });
            layout.addView(repoText);
        }
    }

    private class GetRepoListTask extends AsyncTask<Object, Integer, LinkedList<Repository>>{
        private ProgressDialog dialog;
        @Override
        protected LinkedList<Repository> doInBackground(Object... objects) {

            Boolean reloadFromServer = (Boolean)objects[1];
            GitHubClient client = (GitHubClient) objects[0];
            LinkedList<Repository> repos = userRepos.get(client.getUser());
            if(repos == null || reloadFromServer){
                userPics.clear();
                repos = new LinkedList<Repository>();

                getUserPicture(client, client.getUser());
                RepositoryService service = new RepositoryService(client);
                try {
                    repos.clear();
                    repos.addAll(service.getRepositories());
                } catch (IOException e) {
                    Log.e(TAG, "IOException while getting list of repositories");
                }
                userRepos.put(client.getUser(), repos);
                try {
                    savePersistentRepos();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot save repositories", e);
                }
            }
            return repos;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(RepoListActivity.this, "List of repositories","Loading...");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(LinkedList<Repository> o) {
            createUserRow();
            createRepoList(o, layout);
            dialog.dismiss();
            super.onPostExecute(o);
        }


    }

    protected AsyncTask getNewTask(){
        return new GetRepoListTask();
    }


}