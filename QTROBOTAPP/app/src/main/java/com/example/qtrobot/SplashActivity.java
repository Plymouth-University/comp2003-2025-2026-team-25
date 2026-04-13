package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.example.qtrobot.data.local.database.AppRoomDatabase;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.example.qtrobot.data.local.entity.ParentAccount;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        new Handler(Looper.getMainLooper()).postDelayed(() ->
            AppDatabase.databaseWriteExecutor.execute(() -> {
                ParentAccount parent = AppDatabase.getInstance(SplashActivity.this)
                        .parentAccountDao().getSingleParent();
                GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(SplashActivity.this);

<<<<<<< HEAD
                new Handler(Looper.getMainLooper()).post(() -> {
                    boolean isGuest = getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .getBoolean("is_guest", false);
                    Intent intent;
                    if (parent != null || googleAccount != null || isGuest) {
                        intent = new Intent(SplashActivity.this, HomeActivity.class);
                    } else {
                        intent = new Intent(SplashActivity.this, WelcomeActivity.class);
=======
                // Check Room DB on a background thread to see if a local account exists
                AppRoomDatabase.databaseWriteExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // Check local Room DB for an Email account
                        ParentAccount parent = AppRoomDatabase.getDatabaseInstance(SplashActivity.this)
                                .parentAccountDao().getSingleParent();
                        
                        // Check if Google sign in is active
                        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(SplashActivity.this);

                        // Switch back to Main Thread for navigation and final isGuest check
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // Check if a Google account is active
                                boolean isGuest = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                        .getBoolean("is_guest", false);
                                Intent intent;
                                // If any of these are true, the user is "logged in"
                                if (parent != null || googleAccount != null || isGuest) {
                                    // go to Home screen
                                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                                } else {
                                    // Not logged in, go to the Welcome screen
                                    intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                                }
                                
                                startActivity(intent);
                                finish(); // Prevent user from going back to Splash
                            }
                        });
>>>>>>> welcome-feature-backup
                    }
                    startActivity(intent);
                    finish();
                });
            }), 800);
    }
}
