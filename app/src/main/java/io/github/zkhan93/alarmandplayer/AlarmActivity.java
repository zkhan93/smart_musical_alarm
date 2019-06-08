package io.github.zkhan93.alarmandplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.alarmandplayer.data.Alarm;
import io.github.zkhan93.alarmandplayer.utils.Weekday;

public class AlarmActivity extends AppCompatActivity {
    public static final String TAG = AlarmActivity.class.getSimpleName();

    public static final String ACTION_NEW = "io.github.zkhan93.alarmandplayer.AlarmActivity.NEW";
    public static final String ACTION_UPDATE = "io.github.zkhan93.alarmandplayer.AlarmActivity" +
            ".UPDATE";

    @BindView(R.id.back)
    public TextView btnBack;

    @BindView(R.id.time_picker)
    public TimePicker timePicker;


    private int[] weekViewIds = new int[]{R.id.sunday, R.id.monday, R.id.tuesday, R.id.wednesday,
            R.id.thursday, R.id.friday, R.id.saturday};
    private int[] weekValues = new int[]{Weekday.SUNDAY, Weekday.MONDAY, Weekday.TUESDAY,
            Weekday.WEDNESDAY, Weekday.THURSDAY, Weekday.FRIDAY, Weekday.SATURDAY};
    private SparseArray<CheckBox> weekValueToCheckbox;
    private SparseIntArray weekIdToWeekValue;
    private Alarm alarm;
    private CollectionReference alarmsRef;

    private View.OnClickListener clicksListener;
    private TimePicker.OnTimeChangedListener alarmTimeChangeListener;
    private CompoundButton.OnCheckedChangeListener repeatChangeListener;
    private OnCompleteListener<DocumentReference> alarmCreateTask;
    private OnCompleteListener<Void> alarmUpdateTask;

    {

        clicksListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.back:
                        onBackPressed();
                        break;
                }
            }
        };

        alarmTimeChangeListener = new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                alarm.hour = hourOfDay;
                alarm.minute = minute;
                updateAlarmOnFirestore();
            }
        };
        repeatChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
                if (alarm.repeat == null)
                    alarm.repeat = new ArrayList<>();
                int day = weekIdToWeekValue.get(btn.getId());
                setDayInRepeat(isChecked, day);
                updateAlarmOnFirestore();
            }
        };
        alarmUpdateTask = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "alarm updated");
                } else {
                    Log.d(TAG, "alarm update failed");
                }
            }
        };
        alarmCreateTask = new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    alarm.key = task.getResult().getId();
                    Log.d(TAG, "alarm created");
                } else {
                    Log.d(TAG, "alarm creation failed");
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);

        weekIdToWeekValue = new SparseIntArray(7);
        for (int i = 0; i < weekViewIds.length; i++) {
            weekIdToWeekValue.put(weekViewIds[i], weekValues[i]);
        }

        CheckBox[] weekViews = new CheckBox[weekViewIds.length];
        for (int i = 0; i < weekViewIds.length; i++) {
            weekViews[i] = findViewById(weekViewIds[i]);
        }

        weekValueToCheckbox = new SparseArray<>();
        for (int i = 0; i < weekValues.length; i++) {
            weekValueToCheckbox.put(weekValues[i], weekViews[i]);
        }

        checkIntentAndUpdateInitialValues();

        btnBack.setOnClickListener(clicksListener);
        timePicker.setOnTimeChangedListener(alarmTimeChangeListener);
        timePicker.forceLayout();

        for (CheckBox weekCheckbox : weekViews)
            weekCheckbox.setOnCheckedChangeListener(repeatChangeListener);

        alarmsRef = ((App) getApplication()).getFirestore().collection("alarms");
    }

    private void checkIntentAndUpdateInitialValues() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String action = intent.getAction();
        if (action == null) {
            showToastAndAbort("No action received!");
        } else if (action.equals(ACTION_NEW)) {
            alarm = new Alarm();
            alarm.enabled = true;
        } else if (action.equals(ACTION_UPDATE)) {
            if (bundle == null) {
                showToastAndAbort("Update action requires existing alarm!");
            } else {
                alarm = bundle.getParcelable("alarm");
                if (alarm == null) {
                    showToastAndAbort("Update action requires non null alarm!");
                }
                timePicker.setHour(alarm.hour);
                timePicker.setMinute(alarm.minute);
                CheckBox weekCheckbox;
                if (alarm.repeat != null) {
                    for (int day : alarm.repeat) {
                        weekCheckbox = weekValueToCheckbox.get(day);
                        if (weekCheckbox != null) {
                            weekCheckbox.setChecked(true);
                        }
                    }
                }
            }
        } else {
            showToastAndAbort("Invalid action received!");
        }
    }

    private void showToastAndAbort(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void setDayInRepeat(boolean isChecked, int day) {
        if (isChecked) {
            if (alarm.repeat.contains(day)) {
                Log.d(TAG, "day already exists " + day);
            } else {
                alarm.repeat.add(day);
                Log.d(TAG, "add day " + day);
            }
        } else {
            if (alarm.repeat.contains(day)) {
                alarm.repeat.remove((Integer)day);
                Log.d(TAG, "add removed " + day);
            } else {
                Log.d(TAG, "day was not there " + day);
            }
        }
    }

    private void updateAlarmOnFirestore() {
        if (alarm.repeat.size() == 0)
            alarm.repeat = null;
        if (alarm.key == null) {
            alarmsRef.add(alarm).addOnCompleteListener(alarmCreateTask);
        } else {
            alarmsRef.document(alarm.key).set(alarm).addOnCompleteListener(alarmUpdateTask);
        }
    }

}
