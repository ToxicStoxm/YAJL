package com.toxicstoxm.YAJL.level;

import java.awt.*;

/**
 * YAJL LogLevel. Can be implemented to use your own custom log levels.
 */
public interface LogLevel {

    /**
     * Display name of the LogLevel.
     * @return
     */
    String getName();

    /**
     * Display color of the LogLevel
     * @return
     */
    Color getColor();

    /**
     * Actual level value used for filtering levels.
     * @return
     */
    int getLevel();
}
