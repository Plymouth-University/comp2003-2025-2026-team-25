package com.example.qtrobot;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (ThemePrefs.isPinkTheme(this)) {
            setTheme(R.style.Theme_App_Pink);
        } else {
            setTheme(R.style.Theme_App_Blue);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ThemePrefs.isPinkTheme(this)) {
            tintButtonsPink(getWindow().getDecorView());
        }
    }

    /**
     * Walk the entire view tree and recolour any view using solid_blue_rounded
     * as its background to use pink instead.
     */
    private void tintButtonsPink(View view) {
        if ("no_tint".equals(view.getTag())) return;

        int pinkColor = ContextCompat.getColor(this, R.color.primary_pink);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                tintButtonsPink(group.getChildAt(i));
            }
        }
        // Tint ImageButtons and TextViews that use solid_blue_rounded background
        Object tag = view.getTag(R.id.tint_tag);
        if (tag != null) return; // already processed

        if (view instanceof ImageButton || view instanceof TextView) {
            android.graphics.drawable.Drawable bg = view.getBackground();
            if (bg != null) {
                try {
                    bg = bg.mutate();
                    bg.setTint(pinkColor);
                    view.setBackground(bg);
                    view.setTag(R.id.tint_tag, true);
                } catch (Exception ignored) {}
            }
        }
    }
}
