package io.github.zkhan93.alarmandplayer.job;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.github.zkhan93.alarmandplayer.R;
import io.github.zkhan93.alarmandplayer.service.openweathermap.WeatherClient;
import io.github.zkhan93.alarmandplayer.service.openweathermap.WeatherService;
import io.github.zkhan93.alarmandplayer.service.openweathermap.response.Weather;
import io.github.zkhan93.alarmandplayer.service.openweathermap.response.WeatherResponse;
import io.github.zkhan93.alarmandplayer.utils.ErrorUtils;
import retrofit2.Call;
import retrofit2.Response;

public class DownloadWeatherDataJob extends JobService {
    public static final String TAG = DownloadWeatherDataJob.class.getSimpleName();
    private ConnectivityManager connectivityManager;
    private FetchDataTask fetchDataTask;

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.d(TAG, "job started");
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (isConnected()) {
            fetchDataTask = new FetchDataTask(this.getApplicationContext(), this, params);
            fetchDataTask.execute();
        } else
            Log.d(TAG, "not connection skipped fetching weather data");

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "job stop requested by system");
        if (fetchDataTask != null)
            fetchDataTask.cancel(true);
        return true;
    }

    private boolean isConnected() {
        if (connectivityManager == null)
            return false;
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork.isConnected();
    }

    static class FetchDataTask extends AsyncTask<Void, Void, Boolean> {
        private WeakReference<DownloadWeatherDataJob> downloadWeatherDataJobWeakReference;
        private JobParameters params;
        private SharedPreferences sharedPreferences;
        private WeatherService weatherService;
        private String locationPrefKey;

        FetchDataTask(Context context, DownloadWeatherDataJob downloadWeatherDataJob,
                      JobParameters params) {
            this.downloadWeatherDataJobWeakReference = new WeakReference<>(downloadWeatherDataJob);
            this.params = params;
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            weatherService = WeatherClient.getInstance().create(WeatherService.class);
            locationPrefKey = context.getString(R.string.pref_setting_location_key);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String city = sharedPreferences.getString(locationPrefKey, null);
            Log.d(TAG, "" + city);
            if (city == null) {
                return false;
            }
            city = city.replace(" ", "");
            try {
                Response<WeatherResponse> response = weatherService.getWeather(city).execute();
                Log.d(TAG, String.format("response: %s", response.body()));
                WeatherResponse wres = response.body();
                if (wres == null) {
                    Log.e(TAG, "response from api was either blank or I couldn't parse it");
                    return null;
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
                return true;
            } catch (IOException ex) {
                Log.e(TAG, String.format("error calling open weather API: %s\n%s", ex.getMessage(),
                        ErrorUtils.getLogTrace(ex)));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            DownloadWeatherDataJob downloadWeatherDataJob =
                    downloadWeatherDataJobWeakReference.get();
            if (downloadWeatherDataJob != null)
                downloadWeatherDataJob.jobFinished(params, !success);
        }
    }
}
