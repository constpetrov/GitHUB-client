package com.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kostya
 * Date: 12.06.12
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */
public abstract class TemplateActivity extends Activity {
    protected static final Map<String, Bitmap> userPics = new TreeMap<String, Bitmap>();
    protected static final TreeMap<String, LinkedList<Repository>> userRepos = new TreeMap<String, LinkedList<Repository>>();
    protected static final TreeMap<String, LinkedList<RepositoryCommit>> repoCommits = new TreeMap<String, LinkedList<RepositoryCommit>>();
    protected String TAG = "github-client/Activity";
    protected static final int REFRESH_MENU_ITEM = 1;
    protected final static Object persistenceFileLock = new Object();
    private static final String PROGRAM_MAIN_FOLDER = "/github-client";
    protected static GitHubClient client;
    AsyncTask task;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(userPics.size() == 0){
            userPics.putAll(readPersistentPics());
        }
        if(userRepos.size() == 0){
            userRepos.putAll(readPersistentRepos());
        }
        if(repoCommits.size() == 0){
            repoCommits.putAll(readPersistentCommits());
        }
    }

    protected Bitmap getUserPicture(GitHubClient client, String username) {
        if(username == null){
            Log.e(TAG, "Commit without commiter name");
        }
        if(userPics.containsKey(username) /*&& userPics.get(username) != null*/){
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
            try {
                savePersistentPics();
            } catch (IOException e) {
                Log.e(TAG, "Failed to save userPics",e);
            }
            return icon;
        }
    }

    protected GitHubClient createClientFromPreferences() {
        if(client == null){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String login = prefs.getString("savedLogin", "");
            String password = prefs.getString("savedPassword", "");
            client = new GitHubClient();
            client.setCredentials(login, password);
        }
        return client;
    }

    protected void removeClient(){
        client = null;
    }

    private void savePersistentPics() throws IOException {
        Log.i(TAG, "saveUserPics(" + userPics.size() + ")");
        synchronized (userPics) {
            for (Map.Entry<String, Bitmap> entry : userPics.entrySet()){
                Bitmap bmp = entry.getValue();
                if(bmp != null){
                    FileOutputStream out = new FileOutputStream(getPersistenceStoragePath() + "/" + entry.getKey()+ ".png");
                    bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                }
            }
        }
    }

    public Map<String, Bitmap> readPersistentPics(){
        Map<String, Bitmap> values = new TreeMap<String, Bitmap>();
        synchronized (persistenceFileLock) {
            File dir = new File(getPersistenceStoragePath());
            if(dir.exists() && dir.isDirectory()){
                for (String f: dir.list()){
                    Bitmap bmp = BitmapFactory.decodeFile(getPersistenceStoragePath()+ "/" +f);
                    String[] names = f.split("\\.");
                    values.put(names[0],bmp);
                }
            }
        }
        return values;
    }

    protected void savePersistentRepos() throws IOException {
        Log.i(TAG, "saveUserRepos(" + userRepos.size() + ")");
        synchronized (userRepos) {
            //create store directory just in case
            new File(getPersistenceStoragePath()).mkdirs();
            final File file = getUserReposFile();
            final File tmpFile = getUserReposFileTmp();

            ObjectOutputStream os = null;
            try {
                os = new ObjectOutputStream(new FileOutputStream(tmpFile));
                os.writeObject(userRepos);
                os.close();
                os = null;
                file.delete();
                if (!tmpFile.renameTo(file)){
                    Log.e(TAG, "Failed to saveUserRepos()");
                }
            } finally {
                if (os != null) {
                    os.close();
                }
            }

        }
    }

    @SuppressWarnings("unchecked")
    public TreeMap<String, LinkedList<Repository>> readPersistentRepos(){
        TreeMap<String, LinkedList<Repository>> values = new TreeMap<String, LinkedList<Repository>>();
        synchronized (persistenceFileLock) {
            try{
            if (getUserReposFile().exists()) {
                ObjectInputStream is = null;
                try {
                    is = new ObjectInputStream(new FileInputStream(getUserReposFile()));
                    values = (TreeMap<String, LinkedList<Repository>>) is.readObject();
                } finally {
                    if (is!=null){
                        is.close();
                    }
                }
            }
            }catch (ClassNotFoundException e){
                Log.e(TAG, "Cannot find file", e);
            }catch (IOException e){
                Log.e(TAG, "Cannot read file", e);
            }
        }
        return values;
    }

    protected void savePersistentCommits() throws IOException {
        Log.i(TAG, "saveUserCommits(" + repoCommits.size() + ")");
        synchronized (repoCommits) {
            //create store directory just in case
            new File(getPersistenceStoragePath()).mkdirs();
            final File file = getRepoCommitsFile();
            final File tmpFile = getRepoCommitsFileTmp();

            ObjectOutputStream os = null;
            try {
                os = new ObjectOutputStream(new FileOutputStream(tmpFile));
                os.writeObject(repoCommits);
                os.close();
                os = null;
                file.delete();
                if (!tmpFile.renameTo(file)){
                    Log.e(TAG, "Failed to saveUserCommits()");
                }
            } finally {
                if (os != null) {
                    os.close();
                }
            }

        }
    }

    @SuppressWarnings("unchecked")
    public TreeMap<String, LinkedList<RepositoryCommit>> readPersistentCommits(){
        TreeMap<String, LinkedList<RepositoryCommit>> values = new TreeMap<String, LinkedList<RepositoryCommit>>();
        synchronized (persistenceFileLock) {
            try{
                if (getRepoCommitsFile().exists()) {
                    ObjectInputStream is = null;
                    try {
                        is = new ObjectInputStream(new FileInputStream(getRepoCommitsFile()));
                        values = (TreeMap<String, LinkedList<RepositoryCommit>>) is.readObject();
                    } finally {
                        if (is!=null){
                            is.close();
                        }
                    }
                }
            }catch (ClassNotFoundException e){
                Log.e(TAG, "Cannot find file", e);
            }catch (IOException e){
                Log.e(TAG, "Cannot read file", e);
            }
        }
        return values;
    }

    private File getUserReposFile() {
        return new File(getPersistenceStoragePath(), "userRepos.bin");
    }

    public File getUserReposFileTmp() {
        return new File(getPersistenceStoragePath(), "userRepos.tmp");
    }

    private File getRepoCommitsFile() {
        return new File(getPersistenceStoragePath(), "repoCommits.bin");
    }

    public File getRepoCommitsFileTmp() {
        return new File(getPersistenceStoragePath(), "repoCommits.tmp");
    }

    private String getPersistenceStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + PROGRAM_MAIN_FOLDER;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        MenuItem m;

        if(! (this instanceof ClientActivity) && ! (this instanceof RepoDetailsActivity)){
            m = menu.add(0, REFRESH_MENU_ITEM, 0, R.string.reload);
            m.setIcon(R.drawable.ic_menu_refresh);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case REFRESH_MENU_ITEM:
                task = getNewTask();
                task.execute(client, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected abstract AsyncTask getNewTask();

    protected LinearLayout createUserRow() {
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout data = (LinearLayout)vi.inflate(R.layout.userdata, null);

        GitHubClient client = createClientFromPreferences();
        ImageView image = (ImageView) data.findViewById(R.id.repoListPic);
        image.setImageBitmap(getUserPicture(client, client.getUser()));
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ClientActivity.class));
            }
        });
        TextView clientName = (TextView) data.findViewById(R.id.repoListUserName);
        clientName.setText(client.getUser());
        clientName.setTextSize(24f);
        clientName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ClientActivity.class));
            }
        });
        return data;
    }

}
