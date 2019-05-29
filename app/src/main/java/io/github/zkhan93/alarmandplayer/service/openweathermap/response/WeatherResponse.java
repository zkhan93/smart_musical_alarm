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

    @Override
    public String toString() {
        return "WeatherResponse{" +
                "coord=" + coord +
                ", weather=" + weather +
                ", main=" + main +
                ", wind=" + wind +
                ", clouds=" + clouds +
                ", sys=" + sys +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
