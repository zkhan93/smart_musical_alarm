package io.github.zkhan93.alarmandplayer.data;

import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class Alarm {
    @Exclude
    public static final String TAG = Alarm.class.getSimpleName();

    public boolean enabled;
    public int hour;
    public int minute;
    public List<Integer> repeat;

    @Exclude
    public long nextAfterMilli() {
//        todo consider repeat and enabled
        TimeZone IST = TimeZone.getTimeZone("Asia/Kolkata");
        Calendar cal = Calendar.getInstance(IST);
        if (hour > 12) {
            cal.set(Calendar.HOUR, hour - 12);
            cal.set(Calendar.AM_PM, Calendar.PM);
        } else {
            cal.set(Calendar.AM_PM, Calendar.AM);
        }
        cal.set(Calendar.MINUTE, minute);

        if (Calendar.getInstance(IST).getTime().after(cal.getTime())) {
            cal.add(Calendar.HOUR, 24);
        }

        Log.d(TAG, String.valueOf(cal.getTime()));
        long now = Calendar.getInstance(IST).getTimeInMillis();

        Log.d(TAG, String.valueOf(now));
        return cal.getTimeInMillis() - now;
    }
}
