package com.example.qtrobot;

import android.content.Intent;
import android.os.Build;
import android.app.NotificationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qtrobot.data.repository.DataRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

                // Show signed-in user details from the active session
        SessionManager session = new SessionManager(this);
        TextView userNameText  = findViewById(R.id.user_name_text);
        TextView userEmailText = findViewById(R.id.user_email_text);
        if (userNameText != null) {
            String name = session.getParentName();
            if (name != null && !name.isEmpty()) {
                userNameText.setText(name);
                userNameText.setVisibility(android.view.View.VISIBLE);
            }
        }
        if (userEmailText != null) {
            String email = session.getParentEmail();
            if (email != null && !email.isEmpty()) {
                userEmailText.setText(email);
                userEmailText.setVisibility(android.view.View.VISIBLE);
            }
        }

        // Apply robot image theme
        ImageView robotImage = findViewById(R.id.qt_image);
        RobotImageHelper.applyRobot(robotImage, this);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        if (goBackButton != null) {
            goBackButton.setOnClickListener(v -> finish());
        }

        // Reminders switch — schedules/cancels daily brush notification
        SwitchMaterial remindersSwitch = findViewById(R.id.reminders_switch);
        if (remindersSwitch != null) {
            android.content.SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
            boolean remindersOn = prefs.getBoolean("reminders_enabled", false);
            remindersSwitch.setChecked(remindersOn);
            remindersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("reminders_enabled", isChecked).apply();
                if (isChecked) {
                    // Request POST_NOTIFICATIONS permission on Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
                    }
                    NotificationScheduler.schedule(SettingsActivity.this);
                    android.widget.Toast.makeText(SettingsActivity.this,
                            "Daily brush reminder set for 8:00 AM", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    NotificationScheduler.cancel(SettingsActivity.this);
                    android.widget.Toast.makeText(SettingsActivity.this,
                            "Reminders turned off", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }

        SwitchMaterial switchTheme = findViewById(R.id.switchTheme);
        if (switchTheme != null) {
            switchTheme.setChecked(ThemePrefs.isPinkTheme(this));
            switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ThemePrefs.setPinkTheme(this, isChecked);
                // Restart app to apply theme globally
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        Button signOutButton = findViewById(R.id.sign_out_button);
        if (signOutButton != null) {
            signOutButton.setOnClickListener(v -> signOut());
        }

        Button addChildProfileButton = findViewById(R.id.add_child_profile_button);
        if (addChildProfileButton != null) {
            addChildProfileButton.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, ChildSelectionActivity.class);
                startActivity(intent);
            });
        }
    }

    private void signOut() {
        // Clear Guest flag
        getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply();

        // Clear the Room Database user data
        DataRepository repository = new DataRepository(getApplication());
        repository.clearAllLocalData();

        // Clear our session and reset the Retrofit client
        new SessionManager(this).clearSession();
        com.example.qtrobot.data.remote.RetrofitClient.reset();

        // Clear Google Sign-In session
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(SettingsActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            // Redirect to WelcomeActivity
            Intent intent = new Intent(SettingsActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
