package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageButton upcomingApptTop = findViewById(R.id.upcoming_appointment_top);
        if (upcomingApptTop != null) {
            upcomingApptTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, AppointmentsActivity.class);
                    startActivity(intent);
                }
            });
        }

        ImageButton settingsButton = findViewById(R.id.app_settings_top);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Bottom Left: Learn
        ImageButton learnButton = findViewById(R.id.navigation_learn);
        if (learnButton != null) {
            learnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, LearnActivity.class);
                    startActivity(intent);
                }
            });
        }

        ImageButton dailyBrushButton = findViewById(R.id.navigation_daily_brush);
        if (dailyBrushButton != null) {
            dailyBrushButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, DailyBrushActivity.class);
                    startActivity(intent);
                }
            });
        }

        ImageButton brushTimerButton = findViewById(R.id.navigation_brush_time);
        if (brushTimerButton != null) {
            brushTimerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, BrushTimerActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}