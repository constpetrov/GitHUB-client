package com.example;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repolist);
        LinearLayout layout = (LinearLayout)findViewById(R.id.repoListLayout);

        GitHubClient client = createClientFromPreferences();
        List<? extends View> infoList = createRepoList(client);

        for(View view:infoList){
            layout.addView(view);
        }
        setContentView(layout);
    }

    private GitHubClient createClientFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String login = prefs.getString("savedLogin", "");
        String password = prefs.getString("savedPassword","");

        GitHubClient client = new GitHubClient();
        client.setCredentials(login, password);

        return client;

    }

    private List<? extends View> createRepoList(GitHubClient client){
        List<TextView> repoList = new LinkedList<TextView>();
        try {
            RepositoryService service = new RepositoryService(client);
            for (Repository repo : service.getRepositories(client.getUser())){
                TextView repoText = new TextView(getApplicationContext());
                repoText.setTextSize(16f);
                repoText.setText(repo.getName() + ", Watchers: " + repo.getWatchers());
                repoList.add(repoText);
            }

        } catch (IOException e) {
            Toast.makeText(this, "We've got IOException!", Toast.LENGTH_SHORT).show();
        }
        return repoList;
    }
}