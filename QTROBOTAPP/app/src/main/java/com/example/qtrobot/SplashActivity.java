package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is already signed in with Google
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(SplashActivity.this);
                
                Intent intent;
                if (account != null) {
                    // Already signed in, go to Home
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                } else {
                    // Not signed in, go to Google Sign-In screen
                    intent = new Intent(SplashActivity.this, GoogleSignInActivity.class);
                }
                
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
