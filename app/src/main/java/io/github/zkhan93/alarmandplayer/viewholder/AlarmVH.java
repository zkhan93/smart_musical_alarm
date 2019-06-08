package io.github.zkhan93.alarmandplayer.viewholder;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.alarmandplayer.R;
import io.github.zkhan93.alarmandplayer.data.Alarm;
import io.github.zkhan93.alarmandplayer.utils.Weekday;

public class AlarmVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    @BindView(R.id.time)
    public TextView time;

    @BindView(R.id.describe)
    public TextView describe;

    @BindView(R.id.enable)
    public Switch enabled;

    private Alarm alarm;
    private String[] weekStr = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private Integer[] weekValues = new Integer[]{Weekday.SUNDAY, Weekday.MONDAY, Weekday.TUESDAY,
            Weekday.WEDNESDAY, Weekday.THURSDAY, Weekday.FRIDAY, Weekday.SATURDAY};
    private SparseArray<String> weekValuesToStr = new SparseArray<>(7);

    private ItemInteractionListener itemInteractionListener;
    private CompoundButton.OnCheckedChangeListener alarmEnableChangeListener;
    private CollectionReference alarmsRef;

    {
        alarmEnableChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                itemInteractionListener.onAlarmEnabled(alarm, isChecked);
            }
        };
    }

    public AlarmVH(@NonNull View itemView, ItemInteractionListener itemInteractionListener) {
        super(itemView);
        for (int i = 0; i < weekValues.length; i++) {
            weekValuesToStr.put(weekValues[i], weekStr[i]);
        }
        this.itemInteractionListener = itemInteractionListener;
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
        enabled.setOnCheckedChangeListener(alarmEnableChangeListener);
    }

    public void setAlarm(@NonNull Alarm alarm) {
        this.alarm = alarm;
        time.setText(getTime(alarm.hour, alarm.minute));
        enabled.setChecked(alarm.enabled);
        String desc = getDescription(alarm.repeat);
        if (desc == null) {
            Calendar now = Calendar.getInstance();
            int nowHour = now.get(Calendar.HOUR_OF_DAY);
            int nowMinute = now.get(Calendar.MINUTE);
            if (alarm.hour > nowHour || (alarm.hour == nowHour && alarm.minute > nowMinute))
                desc = "Today";
            else
                desc = "Tomorrow";
        }
        describe.setText(desc);
    }

    private String getTime(int hour, int minute) {
        String a = "AM";
        if (hour > 12) {
            hour -= 12;
            a = "PM";
        }
        return String.format(Locale.ENGLISH, "%d:%02d %s", hour, minute, a);
    }

    private String getDescription(List<Integer> repeat) {
        if (repeat == null || repeat.size() == 0) {
            return null;
        }
        List<Integer> weekValueList = new ArrayList<>(Arrays.asList(weekValues));
        if (repeat.containsAll(weekValueList)) {
            return "Everyday";
        } else {
            Collections.sort(repeat);
            List<String> days = new ArrayList<>();
            for (int day : repeat) {
                days.add(weekValuesToStr.get(day));
            }
            return String.join(", ", days);
        }
    }

    @Override
    public void onClick(View v) {
        itemInteractionListener.onSelect(alarm);
    }

    public interface ItemInteractionListener {
        void onSelect(Alarm alarm);

        void onAlarmEnabled(Alarm alarm, boolean enabled);
    }

}
