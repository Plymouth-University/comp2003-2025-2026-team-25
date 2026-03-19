package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

        // Apply robot image theme
        ImageView robotImage = findViewById(R.id.qt_image);
        RobotImageHelper.applyRobot(robotImage, this);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        if (goBackButton != null) {
            goBackButton.setOnClickListener(v -> finish());
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
                Intent intent = new Intent(SettingsActivity.this, NewProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void signOut() {
        // to clear Guest flag (when signed in as guest)
        getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply();

        // Clear the Room Database user data using DataRepository
        // We do this on a background thread via the executor in AppRoomDatabase
        DataRepository repository = DataRepository.getInstance(getApplication());

        repository.clearAllLocalData();

        // Clear Google Sign-In session
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(SettingsActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            // Go back to the (Welcome activity)
            Intent intent = new Intent(SettingsActivity.this, WelcomeActivity.class);

            //clears the entire activity stack so users can't "Go Back" into the app
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
