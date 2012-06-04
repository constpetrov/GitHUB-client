package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        TextView[] texts = new TextView[4];
        for(int i = 0; i< 4; i++){
            texts[i] = new TextView(getApplicationContext());
            texts[i].setText("TextView #" + i);
            texts[i].setTextSize(0.5f * (i+1));
        }

        for(TextView view:texts){
            layout.addView(view);
        }
        setContentView(layout);
    }
}