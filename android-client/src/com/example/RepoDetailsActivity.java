package com.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.eclipse.egit.github.core.Repository;

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

        LinearLayout layout = (LinearLayout) findViewById(R.id.repoDetailsLayout);

        if (repository != null) {
            TextView text = new TextView(this);
            text.setText((repository.getOwner().getName() != null ? repository.getOwner().getName() : repository.getOwner().getLogin()) + "\n"
                    + (repository.getDescription().equals("") ? "no description" : repository.getDescription()) + "\n"
                    + "Forks: " + repository.getForks() + "\n"
                    + "Watchers: " + repository.getWatchers());
            text.setTextSize(20);
            Button seeCommits = new Button(this);
            seeCommits.setText("See commits for this repo");
            seeCommits.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent showCommitsIntent = new Intent(RepoDetailsActivity.this, CommitActivity.class);
                    showCommitsIntent.putExtra(CommitActivity.class.getCanonicalName(), repository);
                    startActivity(showCommitsIntent);
                }
            });
            layout.addView(text);
            layout.addView(seeCommits);
        }
        setContentView(layout);
    }
}