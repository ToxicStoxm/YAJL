package com.toxicstoxm.YAJL.old.config;

import com.toxicstoxm.YAJL.core.level.LogLevel;

import java.io.PrintStream;
import java.util.List;

/**
 * YAJL configuration interface.
 */
public interface YAJLManagerSettings {
    // Log Level Settings
    YAJLManagerSettings setDefaultLogLevel(LogLevel logLevel);

    // Color Coding Settings
    YAJLManagerSettings setEnableColorCoding(boolean enableColorCoding);

    // Logger Mute Settings
    YAJLManagerSettings setMuteLogger(boolean muteLogger);

    // YAJSI Bridge Settings
    YAJLManagerSettings setBridgeYAJSI(boolean bridgeYAJSI);

    // Log Area Filter Settings
    YAJLManagerSettings setLogAreaFilterPatterns(List<String> logAreaFilterPatterns);

    YAJLManagerSettings addLogAreaFilterPattern(String logAreaFilterPattern);

    YAJLManagerSettings addLogAreaFilterPatterns(String... logAreaFilterPatterns);

    // Log File Settings
    YAJLManagerSettings setLogFileConfig(com.toxicstoxm.YAJL.old.config.LogFileConfig logFileConfig);

    // Log File Configuration Methods
    YAJLManagerSettings setLogFileEnabled(boolean enabled);

    YAJLManagerSettings setLogFileLimitationMode(String limitationMode);

    YAJLManagerSettings setLogFileLimitationNumber(int limitationNumber);

    YAJLManagerSettings setLogFileCompressOldLogFiles(boolean compressOldLogFiles);

    YAJLManagerSettings setLogFileLogDirectory(String logDirectory);

    YAJLManagerSettings setLogFileLogFileName(String logFileName);

    // Stacktrace Length Limit Settings
    YAJLManagerSettings setStackTraceLengthLimit(int stackTraceLengthLimit);

    // Log Message Layout Settings
    YAJLManagerSettings setLogMessageLayout(String logMessageLayout);

    YAJLManagerSettings setMinimumLogLevel(int minimumLogLevel);

    YAJLManagerSettings setFilterPatternsAsBlacklist(boolean filterPatternsAsBlacklist);

    YAJLManagerSettings setLogStream(PrintStream logStream);
}
