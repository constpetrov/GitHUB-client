package com.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;

/**
 * Created with IntelliJ IDEA.
 * User: kpetrov
 * Date: 10.06.12
 * Time: 19:31
 * To change this template use File | Settings | File Templates.
 */
public class RepoDetailsActivity extends TemplateActivity {
    private Repository repository;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repodetails);
        repository = (Repository) getIntent().getSerializableExtra(RepoDetailsActivity.class.getCanonicalName());

        if (repository != null) {
            TextView nameView = (TextView)findViewById(R.id.repository_name);
            nameView.setText(repository.getName());

            TextView descriptionView = (TextView)findViewById(R.id.repository_description);
            descriptionView.setText(repository.getDescription());

            TextView forksView = (TextView)findViewById(R.id.repository_forks);
            forksView.setText(String.valueOf(repository.getForks()));

            TextView watchersView = (TextView)findViewById(R.id.repository_watchers);
            watchersView.setText(String.valueOf(repository.getWatchers()));

            Button seeCommits = (Button)findViewById(R.id.see_commits_button);
            seeCommits.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent showCommitsIntent = new Intent(RepoDetailsActivity.this, CommitActivity.class);
                    showCommitsIntent.putExtra(CommitActivity.class.getCanonicalName(), repository);
                    startActivity(showCommitsIntent);
                }
            });
        }
        LinearLayout layout = (LinearLayout)findViewById(R.id.repoDetailsLayout);
        layout.addView(createUserRow(), 0);
    }

    @Override
    protected AsyncTask getNewTask() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}