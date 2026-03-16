package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.common.SignInButton;
import com.example.qtrobot.data.local.dao.ChildProfileDao;
import com.example.qtrobot.data.local.dao.ParentAccountDao;
import com.example.qtrobot.data.local.database.AppDatabase;

/**
 * Legacy email/password login screen.
 *
 * NOTE: The primary login path is now GoogleSignInActivity, reached via SplashActivity.
 * This screen is kept for future email/password support, but:
 *  - The login button now requires a valid session to proceed (no bypass).
 *  - The Google Sign-In button routes directly to GoogleSignInActivity.
 */
public class MainActivity extends BaseActivity {

    private EditText passwordInput;
    private ImageButton passwordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If a session already exists, skip this screen entirely
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Initialise Room (touch DB so it is created on first launch)
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        ParentAccountDao parentAccountDao = db.parentAccountDao();
        ChildProfileDao  childProfileDao  = db.childProfileDao();
        AppDatabase.databaseWriteExecutor.execute(parentAccountDao::getSingleParent);

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        passwordInput  = findViewById(R.id.password_input);
        passwordToggle = findViewById(R.id.password_toggle);

        if (passwordToggle != null) {
            passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
        }

        // Email/password login — placeholder until that flow is fully implemented.
        // Does NOT bypass auth; routes to GoogleSignInActivity for now.
        Button loginButton = findViewById(R.id.login_button);
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                // TODO: implement email/password auth against the backend.
                // For now redirect to Google Sign-In so there is always a valid session.
                startActivity(new Intent(MainActivity.this, GoogleSignInActivity.class));
            });
        }


        // Google Sign-In button — primary auth path
        SignInButton googleSignInButton = findViewById(R.id.googleSignInButton);
        if (googleSignInButton != null) {
            googleSignInButton.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, GoogleSignInActivity.class)));
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordInput.setInputType(129); // textPassword
            passwordToggle.setImageResource(R.drawable.passwordvistoggle);
        } else {
            passwordInput.setInputType(145); // textVisiblePassword
            passwordToggle.setImageResource(R.drawable.passwordvistoggle);
        }
        isPasswordVisible = !isPasswordVisible;
        passwordInput.setSelection(passwordInput.getText().length());
    }
}
