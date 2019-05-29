package io.github.zkhan93.alarmandplayer.service.openweathermap;

import io.github.zkhan93.alarmandplayer.service.openweathermap.response.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("weather")
    Call<WeatherResponse> getWeather(@Query("q") String cityCountry);
}
