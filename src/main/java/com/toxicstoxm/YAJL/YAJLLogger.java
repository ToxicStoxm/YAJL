package com.toxicstoxm.YAJL;

public class YAJLLogger {
    public static void log(String message) {
        LoggerManager.getSettings().getOutput().println(message);
    }
}
