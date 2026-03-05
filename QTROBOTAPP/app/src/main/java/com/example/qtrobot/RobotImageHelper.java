package com.example.qtrobot;

import android.content.Context;
import android.widget.ImageView;

public class RobotImageHelper {
    public static void applyRobot(ImageView imageView, Context context) {
        if (imageView == null) return;
        if (ThemePrefs.isPinkTheme(context)) {
            imageView.setImageResource(R.drawable.pink_qt);
        } else {
            imageView.setImageResource(R.drawable.qtrobot);
        }
    }
}