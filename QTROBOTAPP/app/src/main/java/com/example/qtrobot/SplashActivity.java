package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.example.qtrobot.data.local.database.AppDatabase;
import com.example.qtrobot.data.local.entity.ParentAccount;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

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
                    SessionManager session = new SessionManager(SplashActivity.this);
                    boolean isGuest = getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .getBoolean("is_guest", false);

                    Intent intent;
                    if (parent == null && googleAccount == null && !isGuest) {
                        intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                    } else if (googleAccount != null && !session.isLoggedIn() && !isGuest) {
                        intent = new Intent(SplashActivity.this, GoogleSignInActivity.class);
                    } else {
                        intent = ChildNavigationHelper.resolveNextScreen(SplashActivity.this, session, isGuest);
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        startActivity(intent);
                        finish();
                    });
                }), 800);
    }
}
