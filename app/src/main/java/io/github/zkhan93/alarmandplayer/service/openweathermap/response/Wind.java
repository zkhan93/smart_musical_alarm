package io.github.zkhan93.alarmandplayer.service.openweathermap.response;

public class Wind {
    public float speed;
    public float deg;

    @Override
    public String toString() {
        return "Wind{" +
                "speed=" + speed +
                ", deg=" + deg +
                '}';
    }
}
