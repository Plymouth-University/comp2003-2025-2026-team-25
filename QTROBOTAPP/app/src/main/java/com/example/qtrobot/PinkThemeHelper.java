package com.example.qtrobot;

import android.content.Context;
import android.graphics.Paint;
import android.widget.TextView;

/**
 * PinkThemeHelper
 *
 * When the pink theme is active, applies a thin purple outline to any
 * TextView whose text would otherwise be white (invisible on pink backgrounds).
 *
 * Usage in any Activity:
 *   PinkThemeHelper.applyOutlineIfPink(this, textView1, textView2, ...);
 */
public class PinkThemeHelper {

    private static final int PURPLE_STROKE = 0xFF000000;
    private static final float STROKE_WIDTH = 1f;

    public static void applyOutlineIfPink(Context context, TextView... views) {
        if (!ThemePrefs.isPinkTheme(context)) return;
        for (TextView tv : views) {
            if (tv == null) continue;
            tv.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            tv.getPaint().setStrokeWidth(STROKE_WIDTH);
            tv.getPaint().setStrokeJoin(Paint.Join.ROUND);
            // Paint stroke colour is separate from text colour in Android —
            // we set the shadow layer to simulate an outline instead
            tv.setShadowLayer(1f, 0, 0, PURPLE_STROKE);
        }
    }
}
