package io.github.zkhan93.alarmandplayer.service.openweathermap.response;

import java.util.List;

public class WeatherResponse {
    public Coord coord;
    public List<Weather> weather;
    public Main main;
    public Wind wind;
    public Cloud clouds;
    public Sys sys;
    public int id;
    public String name;
}
