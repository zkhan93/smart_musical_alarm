package io.github.zkhan93.alarmandplayer.service.openweathermap.response;

public class Main {
    public float temp;
    public int pressure;
    public int humidity;
    public float temp_min;

    @Override
    public String toString() {
        return "Main{" +
                "temp=" + temp +
                ", pressure=" + pressure +
                ", humidity=" + humidity +
                ", temp_min=" + temp_min +
                ", temp_max=" + temp_max +
                '}';
    }

    public float temp_max;
}
