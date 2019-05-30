package io.github.zkhan93.alarmandplayer.job;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import io.github.zkhan93.alarmandplayer.service.openweathermap.WeatherClient;
import io.github.zkhan93.alarmandplayer.service.openweathermap.WeatherService;
import io.github.zkhan93.alarmandplayer.service.openweathermap.response.Weather;
import io.github.zkhan93.alarmandplayer.service.openweathermap.response.WeatherResponse;
import io.github.zkhan93.alarmandplayer.utils.ErrorUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadWeatherDataJob extends JobService {
    public static final String TAG = DownloadWeatherDataJob.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private WeatherService weatherService;
    private ConnectivityManager connectivityManager;

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.d(TAG, "job started");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        weatherService = WeatherClient.getInstance().create(WeatherService.class);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (isConnected())
            fetchWeatherInfo(params);
        else
            Log.d(TAG, "not connection skipped fetching weather data");

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private boolean isConnected() {
        if (connectivityManager == null)
            return false;
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork.isConnected();
    }

    private void fetchWeatherInfo(final JobParameters params) {
        String city = sharedPreferences.getString("query_city", "Bangalore,IN");
        weatherService.getWeather(city).enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                Log.d(TAG, String.format("response: %s", response.body()));
                WeatherResponse wres = response.body();
                if (wres == null) {
                    Log.e(TAG, "response from api was either blank or I couldn't parse it");
                    return;
                }
                int weatherId = -1;
                String description = null;
                for (Weather weather : wres.weather) {
                    weatherId = weather.id;
                    description = weather.description;
                }
                float temp_degree = wres.main.temp;
                float temp_min = wres.main.temp_min;
                float temp_max = wres.main.temp_max;

                sharedPreferences.edit()
                        .putFloat("weather_temp", temp_degree)
                        .putFloat("weather_temp_min", temp_min)
                        .putFloat("weather_temp_max", temp_max)
                        .putString("weather_description", description)
                        .putInt("weather_id", weatherId).apply();
                jobFinished(params, false);
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, String.format("error calling open weather API: %s\n%s", t.getMessage(),
                        ErrorUtils.getLogTrace(t)));
                jobFinished(params, true);
            }
        });
    }
}
