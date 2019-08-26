package com.example.kibinkim.youreyes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {
    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //음악실행
        //startService(new Intent(getApplicationContext(), MusicService.class));

        try{
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this,MainActivity.class));
//      stopService(new Intent(getApplicationContext(), MusicService.class));
        finish();
    }
}
