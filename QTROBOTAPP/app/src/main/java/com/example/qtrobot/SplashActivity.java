package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.example.qtrobot.data.local.database.AppDatabase;
import com.example.qtrobot.data.local.entity.ParentAccount;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        //RobotImageHelper.applyRobot(robotImage, this);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
<<<<<<< Updated upstream

                // Check Room DB on a background thread to see if a local account exists
                AppDatabase.databaseWriteExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // Check local Room DB for an Email account
                        ParentAccount parent = AppDatabase.getInstance(SplashActivity.this)
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
                    }
                });
=======
                SessionManager session = new SessionManager(SplashActivity.this);

                Intent intent;
                if (session.isLoggedIn()) {
                    // Active session exists — go straight to Home
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                } else {
                    // Check Google's cached account as fallback
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(SplashActivity.this);
                    if (account != null) {
                        // Google is signed in but our session is stale — let GoogleSignInActivity re-hydrate it
                        intent = new Intent(SplashActivity.this, GoogleSignInActivity.class);
                    } else {
                        intent = new Intent(SplashActivity.this, GoogleSignInActivity.class);
                    }
                }

                startActivity(intent);
                finish();
>>>>>>> Stashed changes
            }
        }, 800);
    }
}
