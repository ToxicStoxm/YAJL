package com.toxicstoxm.YAJL;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Logger {
    private final String name;
    @Contract(pure = true)
    public Logger(@NotNull Class<?> clazz) {
        String[] parts = clazz.getName().split("\\.");
        name = parts[parts.length - 1];
    }

    public void log(String message) {
        YAJLLogger.log("[" + name + "]: " + message);
    }
}
