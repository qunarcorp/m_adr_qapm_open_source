package com.mqunar.atom.voice.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mqunar.qapm.QAPM;
import com.mqunar.qapm.core.ApplicationLifeObserver;
import com.mqunar.qapm.plugin.TracePlugin;

import java.util.Random;

public class MainActivity extends Activity {
    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.fpsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        });
        findViewById(R.id.nextBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
            }
        });
        String[] data = new String[200];
        for (int i = 0; i < 200; i++) {
            data[i] = "QAPM Trace:" + i;
        }
        mList = (ListView) findViewById(R.id.list);
        mList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, data) {
            Random random = new Random();


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                int rand = random.nextInt(10);
                if (rand % 3 == 0) {
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return super.getView(position, convertView, parent);
            }
        });
       // QAPM.getInstance().getCParam();
    }

    @Override
    protected void onDestroy() {
        ApplicationLifeObserver.getInstance().onDestroy();
        super.onDestroy();
    }
}
