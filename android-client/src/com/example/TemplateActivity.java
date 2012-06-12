package com.example;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: Kostya
 * Date: 12.06.12
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */
public class TemplateActivity extends Activity {
    protected static Map<String, Bitmap> userPics = new TreeMap<String, Bitmap>();
    protected static Map<String, Collection<Repository>> userRepos = new TreeMap<String, Collection<Repository>>();
    protected static Map<String, Collection<RepositoryCommit>> repoCommits = new TreeMap<String, Collection<RepositoryCommit>>();
    protected String TAG = "github-client/Activity";

    protected Bitmap getUserPicture(GitHubClient client, String username) {
        if(userPics.containsKey(username) && userPics.get(username) != null){
            return userPics.get(username);
        } else {
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
            userPics.put(username, icon);
            return icon;
        }
    }

    protected GitHubClient createClientFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String login = prefs.getString("savedLogin", "");
        String password = prefs.getString("savedPassword", "");

        GitHubClient client = new GitHubClient();
        client.setCredentials(login, password);

        return client;
    }
}
