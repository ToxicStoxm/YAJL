package com.toxicstoxm.YAJL.level;

import java.awt.*;

/**
 * YAJL LogLevel. Can be implemented to use your own custom log levels.
 */
public interface LogLevel {

    /**
     * Display name of the LogLevel.
     */
    String getName();

    /**
     * Display color of the LogLevel
     */
    Color getColor();

    /**
     * Actual level value used for filtering levels.
     */
    int getLevel();
}
