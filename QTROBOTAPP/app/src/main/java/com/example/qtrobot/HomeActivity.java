package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.qtrobot.ui.viewmodel.ChildViewModel;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Session guard — allow logged in users and guests
        SessionManager session = new SessionManager(this);
        boolean isGuest = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getBoolean("is_guest", false);
        if (!session.isLoggedIn() && !isGuest) {
            Intent intent = new Intent(this, GoogleSignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

                // Apply robot image theme
        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        // Greet by child's preferred name (observed from Room DB)
        TextView greetingText = findViewById(R.id.greeting_text);
        if (greetingText != null) {
            ChildViewModel viewModel = new ViewModelProvider(this).get(ChildViewModel.class);
            LiveData<com.example.qtrobot.data.local.entity.ChildProfile> childLiveData;
            if (isGuest) {
                // Guest users have no parentId — just get first child from DB
                childLiveData = viewModel.getChildFromRoom();
            } else {
                viewModel.setParentId(session.getParentId());
                childLiveData = viewModel.getChildForCurrentParent();
            }
            childLiveData.observe(this, child -> {
                if (child != null && child.preferredName != null && !child.preferredName.isEmpty()) {
                    greetingText.setText(getString(R.string.hi_greeting, child.preferredName));
                    greetingText.setVisibility(View.VISIBLE);
                } else {
                    String parentName = session.getParentName();
                    if (parentName != null && !parentName.isEmpty()) {
                        greetingText.setText(getString(R.string.hi_greeting, parentName));
                        greetingText.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        ImageButton qrCodeButton = findViewById(R.id.qr_code_button);
        if (qrCodeButton != null) {
            qrCodeButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, QrScanPage.class)));
        }

        ImageButton upcomingApptTop = findViewById(R.id.upcoming_appointment_top);
        if (upcomingApptTop != null) {
            upcomingApptTop.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AppointmentsActivity.class)));
        }

        ImageButton settingsButton = findViewById(R.id.app_settings_top);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
        }

        ImageButton learnButton = findViewById(R.id.navigation_learn);
        if (learnButton != null) {
            learnButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, LearnActivity.class)));
        }

        ImageButton dailyBrushButton = findViewById(R.id.navigation_daily_brush);
        if (dailyBrushButton != null) {
            dailyBrushButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, DailyBrushActivity.class)));
        }

        ImageButton brushTimerButton = findViewById(R.id.navigation_brush_time);
        if (brushTimerButton != null) {
            brushTimerButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, BrushTimerActivity.class)));
        }
    }
}
