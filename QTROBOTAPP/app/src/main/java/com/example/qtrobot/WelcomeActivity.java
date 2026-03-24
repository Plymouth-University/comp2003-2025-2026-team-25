package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.Button;

// Database tools import
import com.example.qtrobot.data.repository.DataRepository;


public class WelcomeActivity extends BaseActivity {
    private DataRepository dataRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initializing repository
        dataRepository = new DataRepository(getApplication());

        // Apply robot image
        ImageView robotImage = findViewById(R.id.qtrobot_image);
        if (robotImage != null) {
            RobotImageHelper.applyRobot(robotImage, this);
        }

        // Email Login choice
        Button emailLoginBtn = findViewById(R.id.btn_email_login);
        emailLoginBtn.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        });

        // Google Login choice
        Button googleBtn = findViewById(R.id.googleSignInButton);
        googleBtn.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, GoogleSignInActivity.class));
        });

        Button tryAppBtn = findViewById(R.id.btn_try_app);
        if (tryAppBtn != null) {
            tryAppBtn.setOnClickListener(v -> {
                // Save the guest status permanently in shared preferences
                getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_guest", true)
                        .apply();
                // Navigate to child profile setup first
                Intent intent = new Intent(WelcomeActivity.this, NewProfileActivity.class);
                intent.putExtra(NewProfileActivity.PARENT_ID_KEY, -1L);
                startActivity(intent);
                finish();
            });
        }
    }
}
