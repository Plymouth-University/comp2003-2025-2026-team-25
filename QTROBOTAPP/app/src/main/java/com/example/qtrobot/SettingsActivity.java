package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Apply robot image theme
        ImageView robotImage = findViewById(R.id.qt_image);
        RobotImageHelper.applyRobot(robotImage, this);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        if (goBackButton != null) {
            goBackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        SwitchMaterial switchTheme = findViewById(R.id.switchTheme);
        if (switchTheme != null) {
            switchTheme.setChecked(ThemePrefs.isPinkTheme(this));
            switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ThemePrefs.setPinkTheme(this, isChecked);
                // Recreate activity to apply theme
                recreate();
                // Also restart HomeActivity to ensure theme propagates everywhere
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }

        Button signOutButton = findViewById(R.id.sign_out_button);
        if (signOutButton != null) {
            signOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        }

        Button addChildProfileButton = findViewById(R.id.add_child_profile_button);
        if (addChildProfileButton != null) {
            addChildProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SettingsActivity.this, NewProfileActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}
