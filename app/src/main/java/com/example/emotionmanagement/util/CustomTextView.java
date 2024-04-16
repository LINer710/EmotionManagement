package com.example.emotionmanagement.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.example.emotionmanagement.MyApp;

public class CustomTextView extends AppCompatTextView {

    public CustomTextView(Context context) {
        super(context);
        init();
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 获取全局字体样式并应用到 TextView
        Typeface customFont = MyApp.getCustomFont();
        setTypeface(customFont);
    }
}
