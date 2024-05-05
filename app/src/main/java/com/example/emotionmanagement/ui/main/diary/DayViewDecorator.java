package com.example.emotionmanagement.ui.main.diary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

class MyDayDecorator implements DayViewDecorator {
    private final CalendarDay date;
    private final Drawable background;

    public MyDayDecorator(Context context, CalendarDay date, int drawableResId) {
        this.date = date;
        this.background = ContextCompat.getDrawable(context, drawableResId);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.equals(date);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(background);
    }
}
