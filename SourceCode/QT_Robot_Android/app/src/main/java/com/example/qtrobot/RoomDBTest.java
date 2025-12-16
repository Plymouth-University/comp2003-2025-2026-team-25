package com.example.qtrobot;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qtrobot.data.AppDatabase;
import com.example.qtrobot.data.UserProfile;
import com.example.qtrobot.utils.AppExecutors;
import java.io.File;

public class RoomDBTest extends AppCompatActivity {

    private UserProfile userProfile;
    private EditText etUsername, etSpeechSpeed, etVolumeLevel, etThemeColor;
    private TextView tvUsernameDisplay;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Link to your new specific layout
        setContentView(R.layout.room_db_test);

        // --- 1. DB FILE CHECK (Moved here so it runs when you open the test screen) ---
        File dbFile = getDatabasePath("qtrobot_database");
        if (dbFile.exists()) {
            Log.e("DB_CHECK", "✅ DATABASE FILE FOUND at: " + dbFile.getAbsolutePath());
        } else {
            Log.e("DB_CHECK", "❌ DATABASE FILE DOES NOT EXIST YET");
            // Force creation
            new Thread(() -> {
                AppDatabase.getInstance(this).userProfileDao().setProfile(new UserProfile());
            }).start();
        }

        // --- 2. INIT VIEWS ---
        userProfile = new UserProfile();
        etUsername = findViewById(R.id.et_username);
        etSpeechSpeed = findViewById(R.id.et_speech_speed);
        etVolumeLevel = findViewById(R.id.et_volume_level);
        etThemeColor = findViewById(R.id.et_theme_color);
        tvUsernameDisplay = findViewById(R.id.tv_username_display);
        btnSave = findViewById(R.id.btn_save);

        // --- 3. SAVE LOGIC ---
        btnSave.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String speechSpeedStr = etSpeechSpeed.getText().toString();
            String volumeLevelStr = etVolumeLevel.getText().toString();
            String themeColor = etThemeColor.getText().toString();

            try {
                float speed = Float.parseFloat(speechSpeedStr);
                int volume = Integer.parseInt(volumeLevelStr);

                userProfile.setUsername(username);
                userProfile.setRobotSpeechSpeed(speed);
                userProfile.setRobotVolumeLevel(volume);
                userProfile.setAppThemeColour(themeColor);
                userProfile.setLocalId("CURRENT_USER");

                tvUsernameDisplay.setText(userProfile.getUsername());

                AppExecutors.getInstance().diskIO().execute(() -> {
                    AppDatabase.getInstance(getApplicationContext()).userProfileDao().setProfile(userProfile);
                    Log.d("DB_WRITE", "Data saved: " + userProfile.getUsername());
                });

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });
    }
}