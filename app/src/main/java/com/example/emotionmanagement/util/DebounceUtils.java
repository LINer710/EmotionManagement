package com.example.emotionmanagement.util;

import android.os.Handler;
import android.os.Looper;

public class DebounceUtils {

    private static final long DEFAULT_DELAY_MILLIS = 300; // 默认延迟时间为 300 毫秒

    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void debounce(Runnable runnable) {
        debounce(DEFAULT_DELAY_MILLIS, runnable);
    }

    public static void debounce(long delayMillis, Runnable runnable) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(runnable, delayMillis);
    }
}

