package io.github.zkhan93.alarmandplayer.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class Alarm implements Parcelable {
    @Exclude
    public static final String TAG = Alarm.class.getSimpleName();

    @Exclude
    public String key;
    public boolean enabled;
    public int hour;
    public int minute;
    public List<Integer> repeat;

    @Exclude
    public long milliSecBeforeSetOff() {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeByte(this.enabled ? (byte) 1 : (byte) 0);
        dest.writeInt(this.hour);
        dest.writeInt(this.minute);
        dest.writeList(this.repeat);
    }

    public Alarm() {
    }

    protected Alarm(Parcel in) {
        this.key = in.readString();
        this.enabled = in.readByte() != 0;
        this.hour = in.readInt();
        this.minute = in.readInt();
        this.repeat = new ArrayList<Integer>();
        in.readList(this.repeat, Integer.class.getClassLoader());
    }

    public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {
        @Override
        public Alarm createFromParcel(Parcel source) {
            return new Alarm(source);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };
}
