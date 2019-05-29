package io.github.zkhan93.alarmandplayer.service.openweathermap.response;

public class Weather {
    public int id;
    public String main;
    public String description;
    public String icon;

    @Override
    public String toString() {
        return "Weather{" +
                "id=" + id +
                ", main='" + main + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
