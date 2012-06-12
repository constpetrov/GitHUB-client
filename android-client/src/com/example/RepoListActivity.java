package com.example;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.AndroidCharacter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.Tag;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

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
        layout = (LinearLayout) findViewById(R.id.repoListLayout);

        GitHubClient client = createClientFromPreferences();
        GetRepoListTask task = new GetRepoListTask();
        task.execute(client);
    }

    private void createUserRow() {
        GitHubClient client = createClientFromPreferences();
        ImageView image = (ImageView) findViewById(R.id.repoListPic);
        image.setImageBitmap(getUserPicture(client, client.getUser()));
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RepoListActivity.this, ClientActivity.class));
            }
        });
        TextView clientName = (TextView) findViewById(R.id.repoListUserName);
        clientName.setText(client.getUser());
        clientName.setTextSize(24f);
        clientName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RepoListActivity.this, ClientActivity.class));
            }
        });
    }

    private void createRepoList(Collection<Repository> repos, LinearLayout layout) {
        List<View> repoList = new LinkedList<View>();
        for (final Repository repo : repos) {
            TextView repoText = new TextView(getApplicationContext());
            repoText.setTextSize(24f);
            repoText.setText(repo.getName());
            repoText.setHighlightColor(android.R.color.white);
            repoText.setTypeface(Typeface.DEFAULT_BOLD);
            repoText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent repoDetailsIntent = new Intent(RepoListActivity.this, RepoDetailsActivity.class);
                    repoDetailsIntent.putExtra(RepoDetailsActivity.class.getCanonicalName(), repo);
                    startActivity(repoDetailsIntent);
                }
            });
            repoList.add(repoText);
        }

        for (View view : repoList) {
            layout.addView(view);
        }
    }

    private class GetRepoListTask extends AsyncTask<GitHubClient, Integer, Collection<Repository>>{
        private Collection<Repository> repos;
        private ProgressDialog dialog;
        @Override
        protected Collection<Repository> doInBackground(GitHubClient... gitHubClients) {
            repos = userRepos.get(gitHubClients[0].getUser());
            if(repos == null || RELOAD_FROM_SERVER){
                repos = new LinkedList<Repository>();
                getUserPicture(gitHubClients[0], gitHubClients[0].getUser());
                RepositoryService service = new RepositoryService(gitHubClients[0]);
                try {
                    repos = service.getRepositories();
                } catch (IOException e) {
                    Log.e(TAG, "IOException while getting list of repositories");
                }
                userRepos.put(gitHubClients[0].getUser(), repos);
            }
            return repos;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(RepoListActivity.this, "List of repositories","Loading...");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Collection<Repository> o) {
            createUserRow();
            createRepoList(repos, layout);
            dialog.dismiss();
            super.onPostExecute(o);
        }


    }
}