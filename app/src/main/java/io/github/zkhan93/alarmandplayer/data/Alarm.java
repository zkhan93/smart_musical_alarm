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

    public String key;
    public boolean enabled;
    public int hour;
    public int minute;
    public List<Integer> repeat;

    @Exclude
    public long nextAfterMilli() {
        //Todo: consider repeat and enabled
        TimeZone IST = TimeZone.getTimeZone("Asia/Kolkata");
        Calendar cal = Calendar.getInstance(IST);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);

        if (Calendar.getInstance(IST).getTime().after(cal.getTime())) {
            cal.add(Calendar.HOUR, 24);
        }
        long now = Calendar.getInstance(IST).getTimeInMillis();
        return cal.getTimeInMillis() - now;
    }
}
