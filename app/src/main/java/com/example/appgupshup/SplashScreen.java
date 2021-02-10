package com.example.appgupshup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appgupshup.Activities.Verification;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {
FirebaseAuth auth;
int delay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getSupportActionBar().hide();

        final Handler handler = new Handler(Looper.getMainLooper());
    handler.postDelayed(new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(SplashScreen.this, Verification.class);
            startActivity(intent);
            finish();
        }

    }, 1500);
 }
}
