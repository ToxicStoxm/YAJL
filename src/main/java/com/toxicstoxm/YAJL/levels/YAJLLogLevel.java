package com.toxicstoxm.YAJL.levels;


import java.awt.*;

public class YAJLLogLevel implements LogLevel {
    private final boolean enabled;
    private final String text;
    private final Color color;

    public YAJLLogLevel(boolean enabled, String text, Color color) {
        this.enabled = enabled;
        this.text = text;
        this.color = color;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Color getColor() {
        return color;
    }
}
