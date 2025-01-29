package com.toxicstoxm.YAJL;

import java.awt.*;

public enum LogLevels implements LogLevel {
    STACKTRACE("STACKTRACE", new Color(71, 71, 71)),
    VERBOSE("VERBOSE", new Color(131, 0, 255)),
    DEBUG("DEBUG", new Color(0, 140, 255)),
    INFO("INFO", new Color(228, 228, 228)),
    WARN("WARN", new Color(255, 220, 21)),
    ERROR("ERROR", new Color(255, 0, 0)),
    FATAL("FATAL", new Color(115, 0, 0));

    private final String name;
    private final Color color;

    LogLevels(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Color getColor() {
        return color;
    }
}
