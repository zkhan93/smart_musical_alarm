package io.github.zkhan93.alarmandplayer.service.openweathermap.response;

public class Coord {
    public float lon;
    public float lat;

    @Override
    public String toString() {
        return "Coord{" +
                "lon=" + lon +
                ", lat=" + lat +
                '}';
    }
}
