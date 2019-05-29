package io.github.zkhan93.alarmandplayer.service.openweathermap.response;

public class Sys {
    public int type;
    public int  id;
    public float message;
    public String country;
    public long sunrise;
    public long sunset;

    @Override
    public String toString() {
        return "Sys{" +
                "type=" + type +
                ", id=" + id +
                ", message=" + message +
                ", country='" + country + '\'' +
                ", sunrise=" + sunrise +
                ", sunset=" + sunset +
                '}';
    }

}
