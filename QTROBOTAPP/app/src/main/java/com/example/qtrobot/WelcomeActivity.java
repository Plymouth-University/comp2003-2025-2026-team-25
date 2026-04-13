package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import com.google.android.material.button.MaterialButton;

import android.widget.Button;

// Database tools import
import com.example.qtrobot.data.repository.DataRepository;


public class WelcomeActivity extends BaseActivity {
    private static final String PREFS_NAME   = "user_prefs";
    private static final String KEY_IS_GUEST = "is_guest";
    private DataRepository dataRepository;
    private MaterialButton emailLoginBtn;
    private MaterialButton googleBtn;
    private MaterialButton tryAppBtn;



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
        emailLoginBtn = findViewById(R.id.btn_email_login);
        emailLoginBtn.setOnClickListener(v -> {
            setGuestStatus(false);
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        });

        // Google Login choice
<<<<<<< HEAD
        Button googleBtn = findViewById(R.id.googleSignInButton);
=======
        googleBtn = findViewById(R.id.googleSignInButton);
>>>>>>> welcome-feature-backup
        googleBtn.setOnClickListener(v -> {
            setGuestStatus(false);
            startActivity(new Intent(WelcomeActivity.this, GoogleSignInActivity.class));
        });

        tryAppBtn = findViewById(R.id.btn_try_app);
        if (tryAppBtn != null) {
            tryAppBtn.setOnClickListener(v -> {
<<<<<<< HEAD
                // Save the guest status permanently in shared preferences
                getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_guest", true)
                        .apply();
                // Navigate to child profile setup first
                Intent intent = new Intent(WelcomeActivity.this, NewProfileActivity.class);
                intent.putExtra(NewProfileActivity.PARENT_ID_KEY, -1L);
=======
                // Save the guest status in shared preferences
                setGuestStatus(true);
                // Navigate to Home Activity
                Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
>>>>>>> welcome-feature-backup
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
