package io.github.zkhan93.alarmandplayer;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.things.device.DeviceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.alarmandplayer.data.Alarm;
import io.github.zkhan93.alarmandplayer.job.DownloadWeatherDataJob;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see
 * <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class ClockActivity extends AppCompatActivity {
    public static final String TAG = ClockActivity.class.getSimpleName();


    @BindView(R.id.root)
    public View rootView;

    @BindView(R.id.left_sidebar)
    public View leftSidebar;

    @BindView(R.id.right_sidebar)
    public View rightSidebar;

    @BindView(R.id.date)
    public TextClock date;

    @BindView(R.id.time)
    public TextClock time;

    @BindView(R.id.weather_desc)
    public TextView weatherDesc;

    @BindView(R.id.weather_main)
    public TextView weatherMain;

    @BindView(R.id.temp_min_max)
    public TextView weatherTempMinMax;

    @BindView(R.id.city)
    public TextView weatherCity;

    @BindView(R.id.weather_views)
    public View weatherViews;


    @BindView(R.id.btn_power)
    public TextView btnPower;

    @BindView(R.id.btn_refresh)
    public TextView btnRefresh;

    @BindView(R.id.btn_alarms)
    public TextView btnAlarms;

    @BindView(R.id.btn_stop)
    public TextView btnStop;

    @BindView(R.id.btn_settings)
    public TextView btnSetting;

    private SharedPreferences sharedPreferences;
    private View.OnClickListener clicksListener;
    private View.OnLongClickListener longClicksListener;
    private View.OnTouchListener touchListener;
    private DeviceManager deviceManager;
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    private JobScheduler jobScheduler;
    private Handler ambientModeHandler;
    private Runnable ambientModeRunnable, startAlarmRunnable;

    int LOAD_WEATHER_DATA_JOB_ID = 0;
    private int ALARMS_JOB_ID = 0;
    private boolean isInAmbientMode = false;
    private int ENABLE_AMBIENT_MODE_AFTER = 15000;


    {
        clicksListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (view.getId()) {
                    case R.id.btn_refresh:
                        btnRefreshClicked(view);
                        break;
                    case R.id.btn_alarms:
                        ambientModeHandler.removeCallbacks(ambientModeRunnable);
                        btnAlarmsClicked(view);
                        break;
                    case R.id.btn_stop:
                        stopRinging();
                        break;
                    case R.id.btn_settings:
                        ambientModeHandler.removeCallbacks(ambientModeRunnable);
                        btnSettingClicked(view);
                        break;
                    case R.id.btn_power:
                        Toast.makeText(view.getContext(), "Hold it to reboot",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        longClicksListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_power:
                        btnPowerClicked(view);
                        return false;
                    default:
                        return true;
                }
            }
        };
        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.startsWith("weather_")) {
                    updateWeatherInfo();
                } else if (key.startsWith("setting_")) {
                    updateSetting();
                }
            }
        };
        touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isInAmbientMode) {
                    disableAmbientMode();
                    ambientModeHandler.postDelayed(ambientModeRunnable, ENABLE_AMBIENT_MODE_AFTER);
                } else {
                    ambientModeHandler.removeCallbacks(ambientModeRunnable);
                    ambientModeHandler.postDelayed(ambientModeRunnable, ENABLE_AMBIENT_MODE_AFTER);
                }
                return false;
            }
        };
        ambientModeRunnable = new Runnable() {
            @Override
            public void run() {
                enableAmbientMode();
            }
        };
        startAlarmRunnable = new Runnable() {
            @Override
            public void run() {
                startRinging();
            }
        };
    }

    //    to get the string from open weather map weather code try the below code
    //    this.getResources().getIdentifier("wi_" + code, "string", getPackageName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        ButterKnife.bind(this, this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        deviceManager = DeviceManager.getInstance();
        jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        btnPower.setOnClickListener(clicksListener);
        btnPower.setOnLongClickListener(longClicksListener);
        btnAlarms.setOnClickListener(clicksListener);
        btnRefresh.setOnClickListener(clicksListener);
        btnSetting.setOnClickListener(clicksListener);
        btnStop.setOnClickListener(clicksListener);
        rootView.setOnTouchListener(touchListener);

        ambientModeHandler = new Handler();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        setDateTime();
        watchWeatherInfo(true);
        fetchWeatherNow();
        updateWeatherInfo();
        setRepeatingAlarmToDownloadWeatherData(true);
        fetchAlarms();
        updateSetting();
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        watchWeatherInfo(false);
        setRepeatingAlarmToDownloadWeatherData(false);
        super.onStop();
    }

    private void setDateTime() {
        String timezoneId = getTimeZone();
        date.setTimeZone(timezoneId);
        date.setFormat12Hour("EEE dd MMM, yyyy");
        date.setFormat24Hour("EEE dd MMM, yyyy");
        time.setTimeZone(timezoneId);
    }

    private String getTimeZone() {
        String timezoneId = sharedPreferences.getString("timezone", "Asia/Kolkata");
        Log.d(TAG, String.format("setting timezone %s", timezoneId));
        return timezoneId;
    }

    private void watchWeatherInfo(boolean enable) {
        if (enable)
            sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        else
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    private void updateWeatherInfo() {
        String city = sharedPreferences.getString(getString(R.string.pref_setting_location_key),
                null);
        int id = sharedPreferences.getInt("weather_id", -1);

        String description = sharedPreferences.getString("weather_description", null);
        float temp_degree = sharedPreferences.getFloat("weather_temp", 0);
        float temp_min = sharedPreferences.getFloat("weather_temp_min", 0);
        float temp_max = sharedPreferences.getFloat("weather_temp_max", 0);

        if (id == -1) {
            Log.e(TAG, "weather code is invalid not update UI");
            return;
        }
        String unicode = weatherIdToUnicode(id);
        weatherMain.setText(getString(R.string.weather_main, (int) temp_degree, unicode));

        if (description == null) {
            weatherDesc.setVisibility(View.GONE);
        } else {
            weatherDesc.setText(description);
            weatherDesc.setVisibility(View.VISIBLE);
        }
        weatherDesc.setText(description);

        weatherTempMinMax.setText(String.format(Locale.ENGLISH, "%.0f\u00B0 / %.0f\u00B0",
                temp_min, temp_max));

        if (city == null) {
            weatherCity.setVisibility(View.GONE);
        } else {
            weatherCity.setText(city);
            weatherCity.setVisibility(View.VISIBLE);
        }

        weatherViews.setVisibility(View.VISIBLE);
    }

    private String weatherIdToUnicode(int weatherId) {
        if (weatherId == -1) {
            // should never come here, only written for safety
            return getString(R.string.wi_200);
        }
        int icon_unicode_id = getResources().getIdentifier(String.format(Locale.ROOT, "wi_%d",
                weatherId), "string", getPackageName());
        return getString(icon_unicode_id);
    }

    private void setRepeatingAlarmToDownloadWeatherData(boolean enable) {
        jobScheduler.schedule(new JobInfo.Builder(LOAD_WEATHER_DATA_JOB_ID,
                new ComponentName(this, DownloadWeatherDataJob.class))
                .setPeriodic(15 * 60 * 1000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build());
    }

    private void fetchWeatherNow() {
        jobScheduler.schedule(new JobInfo.Builder(LOAD_WEATHER_DATA_JOB_ID,
                new ComponentName(this, DownloadWeatherDataJob.class))
                .setMinimumLatency(0)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build());
    }

    private void fetchAlarms() {
        Query query = FirebaseFirestore.getInstance()
                .collection("alarms");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                if (snapshot == null) {
                    return;
                }
                List<Alarm> alarms = new ArrayList<>();
                Alarm alarm;
                for (DocumentSnapshot ds : snapshot.getDocuments()) {
                    alarm = ds.toObject(Alarm.class);
                    if (alarm != null) {
                        alarm.key = ds.getId();
                        alarms.add(alarm);
                    }
                }
                setJobForAlarms(alarms);
            }
        });
    }

    private void setJobForAlarms(List<Alarm> alarms) {
        Log.d(TAG, "sethandlerForAlarms");

        ambientModeHandler.removeCallbacks(startAlarmRunnable);

        for (Alarm alarm : alarms) {
            if (alarm.enabled) {
                long millisecInNextAlarm = alarm.nextAfterMilli();
                Calendar cal = Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                cal.add(Calendar.MILLISECOND, (int) millisecInNextAlarm);
//                cal.setTimeInMillis(System.currentTimeMillis() + millisecInNextAlarm);
                Log.d(TAG, String.format("ring at : %d %d:%d", cal.get(Calendar.DATE),
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE)
                ));
                ambientModeHandler.postDelayed(startAlarmRunnable, millisecInNextAlarm);
            }
        }
    }

    private void stopRinging() {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();
        fetchAlarms();
    }

    private void disableAmbientMode() {
        // change the background to blank
        rootView.setBackground(getDrawable(R.drawable.background));
        // increase the brightness
        setVisibilityOfDetailViews(View.VISIBLE, 100);
        isInAmbientMode = false;
    }

    private void enableAmbientMode() {
        // change the background to blank
        rootView.setBackground(getDrawable(R.drawable.ambient_bg));
        setVisibilityOfDetailViews(View.GONE, 2);
        isInAmbientMode = true;
    }

    private void setVisibilityOfDetailViews(int visibility, float brightness) {
        rightSidebar.setVisibility(visibility);
        leftSidebar.setVisibility(visibility);
        weatherDesc.setVisibility(visibility);
        weatherCity.setVisibility(visibility);

        // adjust the brightness
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness / 100.0f;
        getWindow().setAttributes(lp);
    }

    private void updateSetting() {
        ENABLE_AMBIENT_MODE_AFTER =
                sharedPreferences.getInt(getString(R.string.pref_setting_ambient_key),
                        ENABLE_AMBIENT_MODE_AFTER);
        ambientModeHandler.removeCallbacks(ambientModeRunnable);
        ambientModeHandler.postDelayed(ambientModeRunnable, ENABLE_AMBIENT_MODE_AFTER);

    }

    private void setFullVolume() {
        int amStreamMusicMaxVol = audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(audioManager.STREAM_MUSIC, amStreamMusicMaxVol, 0);
    }

    private void startRinging() {
        setFullVolume();
        String path =
                sharedPreferences.getString(getString(R.string.pref_setting_alarmsound_key)
                        , null);
        Log.d(TAG, "path: " + path);
        if (path != null)
            mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.fromFile(new File(path)));
        else
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
//        ambientModeHandler.postDelayed(startAlarmRunnable, 24 * 60 * 60 * 1000);
    }

    /*    click handlers below  */

    private void btnPowerClicked(View view) {
        deviceManager.reboot();
    }

    public void btnRefreshClicked(View view) {
        Toast.makeText(getApplicationContext(), "Reloading weather data...", Toast.LENGTH_LONG).show();
        jobScheduler.schedule(new JobInfo.Builder(LOAD_WEATHER_DATA_JOB_ID,
                new ComponentName(this, DownloadWeatherDataJob.class))
                .setMinimumLatency(2)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build());
    }

    public void btnAlarmsClicked(View view) {
        startActivity(new Intent(this, AlarmActivity.class));
    }

    public void btnSettingClicked(View view) {
        startActivity(new Intent(this, SettingActivity.class));
    }
}
