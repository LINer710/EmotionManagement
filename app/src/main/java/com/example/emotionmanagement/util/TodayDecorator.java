package com.example.emotionmanagement.util;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.threeten.bp.LocalDate;

public class TodayDecorator implements DayViewDecorator {
    private final int color;
    private final CalendarDay today;

    public TodayDecorator(int color) {
        this.color = color;
        this.today = CalendarDay.today(); // 获取今天的日期
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return today.equals(day); // 仅当日历上的日期与今天相同时才装饰
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new android.text.style.ForegroundColorSpan(color)); // 设置字体颜色为红色
    }
}
