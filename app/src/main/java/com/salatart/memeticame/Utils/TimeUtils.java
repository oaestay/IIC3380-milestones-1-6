package com.salatart.memeticame.Utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by sasalatart on 9/12/16.
 */
public class TimeUtils {
    public static String parseISODate(String dateString) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hod = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            int dom = calendar.get(Calendar.DAY_OF_MONTH);
            int mon = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            return String.format("%02d:%02d %02d/%02d/%d", hod, min, dom, mon, year);
        } catch (ParseException e) {
            Log.e("ERROR", e.toString());
            return null;
        }
    }

    public static String currentISODate() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(new Date());
    }
}
