package io.github.zkhan93.alarmandplayer;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.Utils;
import io.github.zkhan93.alarmandplayer.R;
import io.github.zkhan93.alarmandplayer.service.openweathermap.WeatherClient;
import io.github.zkhan93.alarmandplayer.service.openweathermap.WeatherService;
import io.github.zkhan93.alarmandplayer.service.openweathermap.response.Weather;
import io.github.zkhan93.alarmandplayer.service.openweathermap.response.WeatherResponse;
import io.github.zkhan93.alarmandplayer.utils.ErrorUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private WeatherService weatherService;

    @BindView(R.id.weather_desc)
    public TextView weatherDesc;

    @BindView(R.id.weather_main)
    public TextView weatherMain;

    @BindView(R.id.date)
    public TextView date;

    //    to get the string from open weather map weather code try the below code
    //    this.getResources().getIdentifier("wi_" + code, "string", getPackageName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weatherService = WeatherClient.getInstance().create(WeatherService.class);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        weatherService.getWeather("Bangalore,IN").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                WeatherResponse wres = response.body();
                String icon = "";
                String description = "";
                for(Weather weather: wres.weather){
                    int icon_unicode_id = getResources().getIdentifier(String.format("wi_%d", weather.id), "string", getPackageName());
                    icon = getString(icon_unicode_id);
                    description = weather.description;
                }
                float temp_degree = wres.main.temp;
                weatherMain.setText(getString(R.string.weather_main, temp_degree, icon));
                weatherDesc.setText(description);
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, String.format("error calling openweather API: %s\n%s", t.getMessage(), ErrorUtils.getLogTrace(t)));
            }
        });
    }
}
