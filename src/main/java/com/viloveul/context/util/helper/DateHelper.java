package com.viloveul.context.util.helper;

import java.util.Calendar;
import java.util.Date;

public final class DateHelper {

    private DateHelper() {
        // not for initialize
    }

    public static Calendar with(Date date, int field, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(field, value);
        return calendar;
    }

    public static Calendar with(int field, int value) {
        return with(new Date(), field, value);
    }

    public static Calendar startTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar;
    }

    public static Calendar lastTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        return calendar;
    }

    public static Calendar currentWeekStartDate(Date currDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(currDate);
        cal.add(Calendar.DATE, -2);
        return cal;
    }

    public static Calendar currentWeekEndDate(Date currDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(currDate);
        cal.add(Calendar.DATE, 2);
        return cal;
    }
}
