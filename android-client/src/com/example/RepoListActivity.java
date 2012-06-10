package com.example;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.AndroidCharacter;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.Tag;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kpetrov
 * Date: 04.06.12
 * Time: 23:36
 * To change this template use File | Settings | File Templates.
 */
public class RepoListActivity extends Activity {
    private static final String TAG = "RepoList";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repolist);
        LinearLayout layout = (LinearLayout) findViewById(R.id.repoListLayout);

        GitHubClient client = createClientFromPreferences();
        createUserRow(client);
        List<? extends View> infoList = createRepoList(client);

        for (View view : infoList) {

            layout.addView(view);
        }
//        setContentView(layout);
    }

    private GitHubClient createClientFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String login = prefs.getString("savedLogin", "");
        String password = prefs.getString("savedPassword", "");

        GitHubClient client = new GitHubClient();
        client.setCredentials(login, password);

        return client;

    }

    private List<? extends View> createRepoList(GitHubClient client) {
        List<View> repoList = new LinkedList<View>();
        try {
            RepositoryService service = new RepositoryService(client);
            for (final Repository repo : service.getRepositories(client.getUser())) {
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

        } catch (IOException e) {
            Toast.makeText(this, "We've got IOException!", Toast.LENGTH_SHORT).show();
        }
        return repoList;
    }

    private void createUserRow(GitHubClient client) {
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

    private Bitmap getUserPicture(GitHubClient client, String username) {
        UserService service = new UserService(client);
        Bitmap icon = null;
        try {
            String avatarUrl = service.getUser(username).getAvatarUrl();
            URL newurl = new URL(avatarUrl);
            icon = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            Log.e(TAG, "Can not parse avatar url for user " + username);
        } catch (IOException e) {
            Log.e(TAG, "Can not get a connection to server");
        }
        return icon;
    }
}