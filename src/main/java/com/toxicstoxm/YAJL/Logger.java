package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.areas.LogArea;
import com.toxicstoxm.YAJL.levels.LogLevel;

public interface Logger {

    default void setDefaultLogArea(LogArea logArea) {
        unsupportedLogArea();   }

    default LogArea getDefaultLogArea() {
        unsupportedLogArea();
        return null;
    }

    default void fatal(String message) {
        log("[FATAL]:      " + message);
    }
    default void error(String message) {
        log("[ERROR]:      " + message);
    }
    default void warn(String message) {
        log("[WARN]:       " + message);
    }
    default void info(String message) {
        log("[INFO]:       " + message);
    }
    default void debug(String message) {
        log("[DEBUG]:      " + message);
    }
    default void verbose(String message) {
        log("[VERBOSE]:    " + message);
    }
    default void stacktrace(String message) {
        log("[STACKTRACE]: " + message);
    }
    default void log(String message, LogLevel level, LogArea area) {unsupportedOperation("custom log function");}

    default void fatal(String message, LogArea area) {
        unsupportedLogArea();
    }
    default void error(String message, LogArea area) {
        unsupportedLogArea();
    }
    default void warn(String message, LogArea area) {
        unsupportedLogArea();
    }
    default void info(String message, LogArea area) {
        unsupportedLogArea();
    }
    default void debug(String message, LogArea area) {
        unsupportedLogArea();
    }
    default void verbose(String message, LogArea area) {
        unsupportedLogArea();
    }
    default void stacktrace(String message, LogArea area) {
        unsupportedLogArea();
    }

    default void unsupportedLogArea() {
        unsupportedOperation("log areas");
    }

    default void unsupportedOperation(String message) {
        throw new UnsupportedOperationException("This logger implementation does not support " + message + "!");
    }

    void log(String message);
}
