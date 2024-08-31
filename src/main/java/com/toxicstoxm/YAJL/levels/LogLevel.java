package com.toxicstoxm.YAJL.levels;

import java.awt.*;

public interface LogLevel {
    boolean isEnabled();
    String getText();
    Color getColor();
}