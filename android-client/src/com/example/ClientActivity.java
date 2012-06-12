package com.example;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: kpetrov
 * Date: 02.06.12
 * Time: 21:15
 * To change this template use File | Settings | File Templates.
 */
public class ClientActivity extends Activity implements View.OnClickListener {
    private EditText loginEdit;
    private EditText passwordEdit;
    private Button submitLogin;
    private CheckBox saveCred;
    private SharedPreferences prefs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);
        loginEdit = (EditText) findViewById(R.id.loginEdit);
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        submitLogin = (Button) findViewById(R.id.submitLogin);
        submitLogin.setOnClickListener(this);
        saveCred = (CheckBox)findViewById(R.id.saveCredentials);


        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loginEdit.setText(prefs.getString("savedLogin", ""));
        passwordEdit.setText(prefs.getString("savedPassword",""));
        saveCred.setChecked(prefs.getBoolean("saveCred",false));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submitLogin: {
                saveSettings();
//                getSomeInfo();
                startActivity(new Intent(this, RepoListActivity.class));
                break;
            }
            default: {

            }
        }
    }

    private void saveSettings() {
        if(saveCred.isChecked()){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("saveCred", true);
            editor.putString("savedLogin", loginEdit.getText().toString());
            editor.putString("savedPassword", passwordEdit.getText().toString());
            editor.commit();
        }
    }

    private void getSomeInfo() {
        GitHubClient client = new GitHubClient();
        client.setCredentials(loginEdit.getText().toString(), passwordEdit.getText().toString());

        StringBuilder builder = new StringBuilder();
        try {
            RepositoryService service = new RepositoryService(client);
            for (Repository repo : service.getRepositories(client.getUser()))
                builder.append(repo.getName()).append(" Watchers: ").append(repo.getWatchers()).append("\n");

        } catch (IOException e) {
            builder.append("We've got IOException!");
        }
        Toast.makeText(this, builder.toString(), Toast.LENGTH_LONG).show();

    }
}