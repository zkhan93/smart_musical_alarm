package io.github.zkhan93.alarmandplayer;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.things.device.DeviceManager;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    @BindView(R.id.btn_location)
    public TextView btnLocation;

    private SharedPreferences sharedPreferences;
    private View.OnClickListener clicksListener;
    private View.OnLongClickListener longClicksListener;
    private DeviceManager deviceManager;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    int LOAD_WEATHERDATA_JOB_ID = 0;
    private JobScheduler jobScheduler;

    {
        clicksListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.city:
                        btnCityClicked(view);
                        break;
                    case R.id.btn_refresh:
                        btnRefreshClicked(view);
                        break;
                    case R.id.btn_alarms:
                        btnAlarmsClicked(view);
                        break;
                    case R.id.btn_location:
                        btnlocationClicked(view);
                        break;
                    case R.id.btn_power:
                        Toast.makeText(view.getContext(), "Hold it to reboot", Toast.LENGTH_SHORT).show();
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
                }
            }
        };
    }

    //    to get the string from open weather map weather code try the below code
    //    this.getResources().getIdentifier("wi_" + code, "string", getPackageName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this, this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        deviceManager = DeviceManager.getInstance();
        jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        weatherCity.setOnClickListener(clicksListener);
        btnPower.setOnClickListener(clicksListener);
        btnPower.setOnLongClickListener(longClicksListener);
        btnAlarms.setOnClickListener(clicksListener);
        btnRefresh.setOnClickListener(clicksListener);
        btnLocation.setOnClickListener(clicksListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        setDateTime();
        watchWeatherInfo(true);
        setRepeatingAlarmToDownloadWeatherData(true);
        updateWeatherInfo();
    }

    @Override
    protected void onStop() {
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
        String city = sharedPreferences.getString("weather_city", "Bangalore");
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
        jobScheduler.schedule(new JobInfo.Builder(LOAD_WEATHERDATA_JOB_ID,
                new ComponentName(this, DownloadWeatherDataJob.class))
                .setPeriodic(60 * 60 * 1000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build());
    }

    /*    click handlers below  */
    private void btnCityClicked(View view) {
        Toast.makeText(getApplicationContext(), "change city", Toast.LENGTH_SHORT).show();
    }

    private void btnPowerClicked(View view) {
        deviceManager.reboot();
    }

    public void btnRefreshClicked(View view) {
        Toast.makeText(getApplicationContext(), "weather data reload", Toast.LENGTH_SHORT).show();
        jobScheduler.schedule(new JobInfo.Builder(LOAD_WEATHERDATA_JOB_ID,
                new ComponentName(this, DownloadWeatherDataJob.class))
                .setMinimumLatency(2)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build());
    }

    public void btnAlarmsClicked(View view) {
        Toast.makeText(getApplicationContext(), "show alarms", Toast.LENGTH_SHORT).show();
    }

    public void btnlocationClicked(View view) {
        Toast.makeText(getApplicationContext(), "show location", Toast.LENGTH_SHORT).show();
    }
}
