package com.example.abox;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

// APP启动页
public class Activity_startup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        // 隐藏标题栏
        Objects.requireNonNull(getSupportActionBar()).hide();

        // 启动定时器，延时3s进登录页
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(Activity_startup.this, Activity_login.class));
                finish();
            }
        }, 3000);
    }
}