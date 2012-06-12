package com.example;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
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
public class TemplateActivity extends Activity {
    protected static final Map<String, Bitmap> userPics = new TreeMap<String, Bitmap>();
    protected static final TreeMap<String, LinkedList<Repository>> userRepos = new TreeMap<String, LinkedList<Repository>>();
    protected static final TreeMap<String, LinkedList<RepositoryCommit>> repoCommits = new TreeMap<String, LinkedList<RepositoryCommit>>();
    protected String TAG = "github-client/Activity";
    protected final static Object persistenceFileLock = new Object();
    private static final String PROGRAM_MAIN_FOLDER = "/github-client";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(userPics.size() == 0){
            userPics.putAll(readPersistentPics());
        }
        if(userRepos.size() == 0){
            userRepos.putAll(readPersistentRepos());
        }
    }

    @Override
    protected void onDestroy() {
        if(userPics.size() == 0){
            try {
                savePersistentPics();
            } catch (IOException e) {
                Log.e(TAG, "Failed to save userPics",e);
            }
        }
        super.onDestroy();
    }

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
            try {
                savePersistentPics();
            } catch (IOException e) {
                Log.e(TAG, "Failed to save userPics",e);
            }
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

    private void savePersistentPics() throws IOException {
        Log.i(TAG, "saveUserPics(" + userPics.size() + ")");
        synchronized (userPics) {
            for (Map.Entry<String, Bitmap> entry : userPics.entrySet()){
                Bitmap bmp = entry.getValue();
                FileOutputStream out = new FileOutputStream(getPersistenceStoragePath() + "/" + entry.getKey()+ ".png");
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
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

    private File getUserReposFile() {
        return new File(getPersistenceStoragePath(), "userRepos.bin");
    }

    public File getUserReposFileTmp() {
        return new File(getPersistenceStoragePath(), "userRepos.tmp");
    }

    private String getPersistenceStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + PROGRAM_MAIN_FOLDER;
    }
}
