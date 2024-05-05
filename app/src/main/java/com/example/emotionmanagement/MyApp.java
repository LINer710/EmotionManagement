package com.example.emotionmanagement;

import android.app.Application;
import android.graphics.Typeface;

import android.content.Context;
import android.content.SharedPreferences;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class MyApp extends Application {
    private static final String PREFS_NAME = "FontPrefs";
    private static final String KEY_FONT_PATH = "fontPath";

    private static Typeface customFont;
    private static SharedPreferences preferences;
    private static Context context; // Store context here
    public static String URL = "http://172.20.10.3:5000";

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext(); // Assign context
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadCustomFont();
        AndroidThreeTen.init(this);

    }

    public static Typeface getCustomFont() {
        return customFont;
    }

    public static void setCustomFont(String fontPath) {
        customFont = Typeface.createFromAsset(context.getAssets(), fontPath);
        saveCustomFont(fontPath);
    }

    private void loadCustomFont() {
        String fontPath = preferences.getString(KEY_FONT_PATH, "fonts/yuanti.ttf");
        customFont = Typeface.createFromAsset(context.getAssets(), fontPath); // Use stored context
    }

    private static void saveCustomFont(String fontPath) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_FONT_PATH, fontPath);
        editor.apply();
    }
}
