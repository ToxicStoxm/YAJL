package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.level.LogLevel;
import org.jetbrains.annotations.NotNull;

public class Logger {
    public final String logArea;
    public final String logPrefix;

    protected Logger(@NotNull Class<?> clazz) {
        logPrefix = clazz.getSimpleName();
        logArea = clazz.getName();
    }

    protected Logger(@NotNull String area) {
        this.logArea = area;
        this.logPrefix = area;
    }

    public void log(@NotNull String message) {
        log(LoggerManager.getSettings().getDefaultLogLevel(), message);
    }

    public void log(@NotNull LogLevel level, @NotNull String message) {
        if (shouldSkipLog()) return;

        LoggerManager.getSettings().getOutput().println("[" + logPrefix + "] [" + level.getName() + "]: " + message);
    }

    public boolean shouldSkipLog() {
        return LoggerManager.getSettings().isMuteLogger();
    }
}
