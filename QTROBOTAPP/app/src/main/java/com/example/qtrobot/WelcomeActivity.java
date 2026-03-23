package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.common.SignInButton;

// Database tools import
import com.example.qtrobot.data.repository.DataRepository;


public class WelcomeActivity extends BaseActivity {
    private static final String PREFS_NAME   = "user_prefs";
    private static final String KEY_IS_GUEST = "is_guest";
    private DataRepository dataRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initializing repository
        dataRepository = DataRepository.getInstance(getApplication());

        // Apply robot image
        ImageView robotImage = findViewById(R.id.qtrobot_image);
        if (robotImage != null) {
            RobotImageHelper.applyRobot(robotImage, this);
        }

        // Email Login choice
        Button emailLoginBtn = findViewById(R.id.btn_email_login);
        emailLoginBtn.setOnClickListener(v -> {
            setGuestStatus(false);
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        });

        // Google Login choice
        SignInButton googleBtn = findViewById(R.id.googleSignInButton);
        googleBtn.setOnClickListener(v -> {
            setGuestStatus(false);
            startActivity(new Intent(WelcomeActivity.this, GoogleSignInActivity.class));
        });

        Button tryAppBtn = findViewById(R.id.btn_try_app);
        if (tryAppBtn != null) {
            tryAppBtn.setOnClickListener(v -> {
                // Save the guest status in shared preferences
                setGuestStatus(true);
                // Navigate to Home Activity
                Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setGuestStatus(boolean isGuest) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_IS_GUEST, isGuest)
                .apply();
    }
}
