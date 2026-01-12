package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.config.LogFileConfig;
import com.toxicstoxm.YAJL.config.YAJLManagerConfig;
import com.toxicstoxm.YAJL.level.LogLevel;
import com.toxicstoxm.YAJSI.SettingsManager;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Singleton manager class for YAJL, responsible for configuration management and logging setup.
 * <p>
 * This class provides methods to configure YAJL, manage logging settings, and interact with
 * the configuration system, including YAML-based configuration persistence.
 * </p>
 *
 * @see YAJLManagerConfig
 * @author ToxicStoxm
 */
public class YAJLManager {
    protected static YAJLManagerConfig config = new YAJLManagerConfig(new File(""));;  // Holds the current logging configuration
    protected static LogFileHandler logFileHandler;  // Manages log file operations

    private static YAJLManager instance;

    public static YAJLManager getInstance() {
        if (instance == null) {
            instance = new YAJLManager();
            instance.init();
        }
        return instance;
    }

    /**
     * Initializes the YAJLManager, setting up configuration, log file handling,
     * and shutdown hooks.
     */
    public void init() {
        logFileHandler = new LogFileHandler();

        if (config.isEnableYAMLConfig()) {
            SettingsManager.getInstance().registerConfig(config);
        }

        // Add a shutdown hook to properly close resources before the application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!config.isMuteLogger()) {
                System.out.println("[YAJL] Processing shutdown handles");
            }
            if (config.isEnableYAMLConfig()) {
                if (!config.isMuteLogger()) {
                    System.out.println("[YAJL] Saving YAML configuration.");
                }
                saveSettingsToYAMLConfigFile();
            }
            if (!config.isMuteLogger()) {
                System.out.println("[YAJL] Shutting down log file handler");
            }
            logFileHandler.shutdown();

            if (!config.isMuteLogger()) {
                System.out.println("[YAJL] Shutting down, Goodbye!");
            }
        }));

        setLogAreaFilterPatterns(config.getLogAreaFilterConfig().getLogAreaFilterPatterns());
        logFileHandler = new LogFileHandler();
    }

    /**
     * Saves the current settings to the YAML configuration file.
     * <p><b>Note:</b> This only works if YAML configuration is enabled.</p>
     *
     */
    public static void saveSettingsToYAMLConfigFile() {
        if (config.isEnableYAMLConfig()) {
            SettingsManager.getInstance().save(config);
        }
    }

    public static void setEnableYAMLConfig(boolean enable) {
        config.setEnableYAMLConfig(enable);
    }

    public static void setDefaultLogLevel(LogLevel defaultLogLevel) {
        config.setDefaultLogLevel(defaultLogLevel);
    }

    public static void setEnableColorCoding(boolean enableColorCoding) {
        config.setEnableColorCoding(enableColorCoding);
    }

    public static void setMuteLogger(boolean muteLogger) {
        config.setMuteLogger(muteLogger);
    }

    public static void setLogAreaFilterPatterns(List<String> logAreaFilterPatterns) {
        config.getLogAreaFilterConfig().setLogAreaFilterPatterns(logAreaFilterPatterns);
        config.getLogAreaFilterConfig().setLogFilter(new LogFilter(logAreaFilterPatterns));
    }

    public static void addLogAreaFilterPattern(String logAreaFilterPattern) {
        config.getLogAreaFilterConfig().getLogAreaFilterPatterns().add(logAreaFilterPattern);
        config.getLogAreaFilterConfig().getLogFilter().addFilterPattern(logAreaFilterPattern);
    }

    public static void addLogAreaFilterPatterns(String... logAreaFilterPatterns) {
        Arrays.stream(logAreaFilterPatterns).forEach(YAJLManager::addLogAreaFilterPatterns);
    }

    public static void setLogFileConfig(LogFileConfig logFileConfig) {
        config.setLogFileConfig(logFileConfig);
    }

    public static void setLogFileEnabled(boolean enabled) {
        config.getLogFileConfig().setEnable(enabled);
        logFileHandler.setEnabled(enabled);
    }

    public static void setLogFileLimitationMode(String limitationMode) {
        config.getLogFileConfig().setLimitationMode(limitationMode);
    }

    public static void setLogFileLimitationNumber(int limitationNumber) {
        config.getLogFileConfig().setLimitationNumber(limitationNumber);
    }

    public static void setLogFileCompressOldLogFiles(boolean compressOldLogFiles) {
        config.getLogFileConfig().setCompressOldLogFiles(compressOldLogFiles);
    }

    public static void setLogFileLogDirectory(String logDirectory) {
        config.getLogFileConfig().setLogDirectory(logDirectory);
    }

    public static void setLogFileLogFileName(String logFileName) {
        config.getLogFileConfig().setLogFileName(logFileName);
    }

    public static void setStackTraceLengthLimit(int stackTraceLengthLimit) {
        config.setStackTraceLengthLimit(stackTraceLengthLimit);
    }

    public static void setLogMessageLayout(String logMessageLayout) {
        config.setLogMessageLayout(logMessageLayout);
    }

    public static void setMinimumLogLevel(int minimumLogLevel) {
        config.setMinimumLogLevel(minimumLogLevel);
    }

    public static void setFilterPatternsAsBlacklist(boolean filterPatternsAsBlacklist) {
        config.getLogAreaFilterConfig().setFilterPatternsAsBlacklist(filterPatternsAsBlacklist);
    }

    public static void setLogStream(PrintStream logStream) {
        config.setLogStream(logStream);
    }
}
