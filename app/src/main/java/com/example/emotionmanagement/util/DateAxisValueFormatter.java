package com.example.emotionmanagement.util;

import com.github.mikephil.charting.formatter.ValueFormatter;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

public class DateAxisValueFormatter extends ValueFormatter {
    private final LocalDate baseDate;  // 基准日期，用于计算相对天数

    public DateAxisValueFormatter(LocalDate baseDate) {
        this.baseDate = baseDate;
    }

    @Override
    public String getFormattedValue(float value) {
        LocalDate date = baseDate.plusDays((long) value);
        return date.format(DateTimeFormatter.ofPattern("M月d日"));
    }
}
