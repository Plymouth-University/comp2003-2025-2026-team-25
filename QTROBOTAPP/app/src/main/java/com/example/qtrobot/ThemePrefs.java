package com.example.qtrobot;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemePrefs {
    private static final String PREFS_NAME = "QTrobotThemePrefs";
    private static final String KEY_THEME_PINK = "theme_pink";

    public static void setPinkTheme(Context context, boolean isPink) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_THEME_PINK, isPink).apply();
    }

    public static boolean isPinkTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_THEME_PINK, false);
    }
}