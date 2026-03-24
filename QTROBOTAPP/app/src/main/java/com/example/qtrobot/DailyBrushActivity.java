package com.example.qtrobot;

import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyBrushActivity extends BaseActivity {

    private static final String PREFS_BRUSH   = "brush_prefs";
    private static final String KEY_LAST_DATE = "last_brush_date";
    private static final String KEY_STREAK    = "brush_streak";

    private static final int COLOR_YELLOW = 0xFFFFD700; // filled star
    private static final int COLOR_GREY   = 0xFFCCCCCC; // empty star

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailybrush);

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        if (goBackButton != null) goBackButton.setOnClickListener(v -> finish());

        renderStars(); // just display current streak, don't modify it
    }

    /**
     * Updates the streak based on today's date.
     * - If brushed today already: no change
     * - If brushed yesterday: increment streak
     * - If missed a day: reset streak to 1
     */
    private void updateStreak() {
        SharedPreferences prefs = getSharedPreferences(PREFS_BRUSH, MODE_PRIVATE);
        String today     = todayString();
        String lastDate  = prefs.getString(KEY_LAST_DATE, "");
        int    streak    = prefs.getInt(KEY_STREAK, 0);

        if (today.equals(lastDate)) {
            // Already recorded today — nothing to do
            return;
        }

        String yesterday = yesterdayString();
        if (lastDate.equals(yesterday)) {
            streak = Math.min(streak + 1, 7); // cap at 7
        } else {
            streak = 1; // missed a day or first time — reset
        }

        prefs.edit()
                .putString(KEY_LAST_DATE, today)
                .putInt(KEY_STREAK, streak)
                .apply();
    }

    private void renderStars() {
        SharedPreferences prefs = getSharedPreferences(PREFS_BRUSH, MODE_PRIVATE);
        int streak = prefs.getInt(KEY_STREAK, 0);

        int[] starIds = {
            R.id.streak_dot_1, R.id.streak_dot_2, R.id.streak_dot_3,
            R.id.streak_dot_4, R.id.streak_dot_5, R.id.streak_dot_6,
            R.id.streak_dot_7
        };

        for (int i = 0; i < starIds.length; i++) {
            ImageView star = findViewById(starIds[i]);
            if (star == null) continue;
            int color = (i < streak) ? COLOR_YELLOW : COLOR_GREY;
            star.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        }
    }

    private String todayString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String yesterdayString() {
        long yesterday = System.currentTimeMillis() - 86_400_000L;
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(yesterday));
    }
}
