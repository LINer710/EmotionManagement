package com.example.emotionmanagement.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class ThemeManager {
    private static ThemeManager instance;
    private Context context;
    private int currentThemeColor;

    private ThemeManager(Context context) {
        this.context = context;
        SharedPreferences prefs = context.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        currentThemeColor = prefs.getInt("theme_color", Color.BLACK);  // 默认颜色
    }

    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context);
        }
        return instance;
    }

    public int getCurrentThemeColor() {
        return currentThemeColor;
    }

    public void setCurrentThemeColor(int color) {
        currentThemeColor = color;
        SharedPreferences prefs = context.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("theme_color", color);
        editor.apply();
    }
}
