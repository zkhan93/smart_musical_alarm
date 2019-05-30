package io.github.zkhan93.alarmandplayer.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.alarmandplayer.R;
import io.github.zkhan93.alarmandplayer.data.Alarm;

public class AlarmVH extends RecyclerView.ViewHolder {

    @BindView(R.id.time)
    public TextView time;

    @BindView(R.id.describe)
    public TextView describe;

    @BindView(R.id.enable)
    public Switch enabled;


    public AlarmVH(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setAlarm(@NonNull Alarm alarm) {
        time.setText(getTime(alarm.hour, alarm.minute));
        enabled.setChecked(alarm.enabled);
        describe.setText(getDescription(alarm.repeat));
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
        StringBuilder description = new StringBuilder();
        if (repeat.contains(1)
                && repeat.contains(2)
                && repeat.contains(3)
                && repeat.contains(4)
                && repeat.contains(5)
                && repeat.contains(6)
                && repeat.contains(7)
        ) {
            description.append("Everyday");
        } else {
            Collections.sort(repeat);
            Collections.reverse(repeat);
            for (int day : repeat) {
                switch (day) {
                    case 1:
                        if (description.length() > 0)
                            description.append(", ");
                        description.append("Mon");
                        break;
                    case 2:
                        if (description.length() > 0)
                            description.append(", ");
                        description.append("Tue");
                        break;
                    case 3:
                        if (description.length() > 0)
                            description.append(", ");
                        description.append("Wed");
                        break;
                    case 4:
                        if (description.length() > 0)
                            description.append(", ");
                        description.append("Thu");
                        break;
                    case 5:
                        if (description.length() > 0)
                            description.append(", ");
                        description.append("Fri");
                        break;
                    case 6:
                        if (description.length() > 0)
                            description.append(", ");
                        description.append("Sat");
                        break;
                    case 7:
                        if (description.length() > 0)
                            description.append(", ");
                        description.append("Sun");
                        break;
                }
            }
        }
        return description.toString();
    }

}
