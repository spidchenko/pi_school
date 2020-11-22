package com.spidchenko.week2task;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class SplashScreenActivity extends AppCompatActivity {
    private static final int SPLASH_SCREEN_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Hide action bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        //Start timer only once
        if (savedInstanceState == null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }, SPLASH_SCREEN_DELAY);
        }
    }
}