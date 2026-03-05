package com.example.qtrobot;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Set the theme BEFORE super.onCreate
        if (ThemePrefs.isPinkTheme(this)) {
            setTheme(R.style.Theme_App_Pink);
        } else {
            setTheme(R.style.Theme_App_Blue);
        }
        super.onCreate(savedInstanceState);
    }
}