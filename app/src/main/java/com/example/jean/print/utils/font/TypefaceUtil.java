package com.example.jean.print.utils.font;

import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by kkurtukov on 22.09.2016.
 */

public class TypefaceUtil {
    public static void initRoboto(TextView v) {
        initFont(v, "Roboto-Regular.ttf");
    }

    public static void initRobotoBold(TextView v) {
        initFont(v, "Roboto-Bold.ttf");
    }

    public static void initFont(TextView v, String fontName) {
        Typeface font = Typeface.createFromAsset(v.getContext().getAssets(), fontName);
        v.setTypeface(font);
    }
}
