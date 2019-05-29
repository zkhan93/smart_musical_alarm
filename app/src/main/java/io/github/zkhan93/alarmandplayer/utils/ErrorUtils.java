package io.github.zkhan93.alarmandplayer.utils;

public class ErrorUtils {
    public static String getLogTrace(Throwable t){
        StringBuilder strb = new StringBuilder();
        for (StackTraceElement e: t.getStackTrace()){
            strb.append(e.toString());
            strb.append("\n");
        }
        return strb.toString();
    }
}
