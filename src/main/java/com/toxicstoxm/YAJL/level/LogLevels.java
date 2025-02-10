package com.toxicstoxm.YAJL.level;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Default YAJL LogLevels.
 */
public enum LogLevels implements LogLevel {
    STACKTRACE("STACKTRACE", new Color(71, 71, 71), -3),
    VERBOSE("VERBOSE", new Color(131, 0, 255), -2),
    DEBUG("DEBUG", new Color(0, 140, 255), -1),
    INFO("INFO", new Color(228, 228, 228), 0),
    WARN("WARN", new Color(255, 220, 21), 1),
    ERROR("ERROR", new Color(255, 0, 0), 2),
    FATAL("FATAL", new Color(115, 0, 0), 3);

    private final String name;
    private final Color color;
    private final int level;

    LogLevels(String name, Color color, int level) {
        this.name = name;
        this.color = color;
        this.level = level;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public int getLevel() {
        return level;
    }

    /**
     * Returns the corresponding LogLevel to the specified level value.
     * @param level log level value to get the corresponding log level for.
     * @return returns the corresponding LogLevel to the specified level value
     * @throws IllegalArgumentException if the provided has no corresponding log level
     */
    public static @NotNull LogLevel fromLevel(int level) throws IllegalArgumentException {
        for (LogLevel lev : LogLevels.values()) {
            if (lev.getLevel() == level) {
                return lev;
            }
        }
        throw new IllegalArgumentException("No log level found for level id: " + level + "!");
    }
}
