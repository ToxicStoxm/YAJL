package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.config.LogFileConfig;
import com.toxicstoxm.YAJL.config.YAJLManagerConfig;
import com.toxicstoxm.YAJL.config.YAJLManagerSettings;
import com.toxicstoxm.YAJL.level.LogLevel;
import com.toxicstoxm.YAJSI.api.settings.SettingsManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
 * @see #getInstance()
 * @see #configure()
 * @see YAJLManagerConfig
 * @see YAJLManagerSettings
 * @author ToxicStoxm
 */
public class YAJLManager implements YAJLManagerSettings {

    protected YAJLManagerConfig config;  // Holds the current logging configuration
    protected LogFileHandler logFileHandler;  // Manages log file operations

    private static YAJLManager instance = null; // Singleton instance

    /**
     * Private constructor to enforce the singleton pattern.
     *
     * @param defaultConfig The default configuration to use for the YAJL manager.
     */
    private YAJLManager(YAJLManagerConfig defaultConfig) {
        this.config = defaultConfig;
    }

    /**
     * Returns the singleton instance of the YAJLManager, initializing it if necessary.
     *
     * @param defaultSettings The default settings to use if the instance is created.
     * @return The singleton instance of YAJLManager.
     */
    public static YAJLManager getInstance(YAJLManagerConfig defaultSettings) {
        if (instance == null) {
            instance = new YAJLManager(defaultSettings == null ? YAJLManagerConfig.builder().build() : defaultSettings);
            instance.init();
        }
        return instance;
    }

    /**
     * Returns the singleton instance using default settings.
     *
     * @return The singleton instance of YAJLManager.
     */
    public static YAJLManager getInstance() {
        return getInstance(null);
    }

    /**
     * Initializes the YAJLManager, setting up configuration, log file handling,
     * and shutdown hooks.
     */
    private void init() {
        if (config.isEnableYAMLConfig()) {
            SettingsManager.getInstance().registerYAMLConfiguration(config);
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
        setBridgeYAJSI(config.isBridgeYAJSI());
    }

    /**
     * Configures and returns an instance of YAJLManagerSettings.
     *
     * @return An instance of YAJLManagerSettings.
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull YAJLManagerSettings configure() {
        return getInstance();
    }

    /**
     * Configures and returns an instance of YAJLManagerSettings using the specified settings.
     *
     * @param defaultSettings The default settings for YAJLManager.
     * @return An instance of YAJLManagerSettings.
     */
    public static @NotNull YAJLManagerSettings configure(YAJLManagerConfig defaultSettings) {
        return getInstance(defaultSettings);
    }

    /**
     * Reloads settings from the YAML configuration file.
     * <p><b>Note:</b> This only works if YAML configuration is enabled.</p>
     *
     * @return The current instance for method chaining.
     */
    public YAJLManagerSettings reloadSettingsFromYAMLConfigFile() {
        if (config.isEnableYAMLConfig()) {
            SettingsManager.getInstance().reloadFromFile(config);
        }
        return this;
    }

    /**
     * Saves the current settings to the YAML configuration file.
     * <p><b>Note:</b> This only works if YAML configuration is enabled.</p>
     *
     * @return The current instance for method chaining.
     */
    public YAJLManagerSettings saveSettingsToYAMLConfigFile() {
        if (config.isEnableYAMLConfig()) {
            SettingsManager.getInstance().save(config);
        }
        return this;
    }

    /**
     * Restores default settings and saves them to the YAML configuration file.
     * <p><b>Note:</b> This only works if YAML configuration is enabled.</p>
     *
     * @return The current instance for method chaining.
     */
    public YAJLManagerSettings restoreDefaultSettings() {
        if (config.isEnableYAMLConfig()) {
            SettingsManager.getInstance().restoreDefaultsFor(config);
        }
        return this;
    }

    public YAJLManagerSettings setDefaultLogLevel(LogLevel defaultLogLevel) {
        config.setDefaultLogLevel(defaultLogLevel);
        return this;
    }

    public YAJLManagerSettings setEnableColorCoding(boolean enableColorCoding) {
        config.setEnableColorCoding(enableColorCoding);
        return this;
    }

    public YAJLManagerSettings setMuteLogger(boolean muteLogger) {
        config.setMuteLogger(muteLogger);
        return this;
    }

    public YAJLManagerSettings setBridgeYAJSI(boolean bridgeYAJSI) {
        config.setBridgeYAJSI(bridgeYAJSI);
        if (bridgeYAJSI) {
            SettingsManager.configure()
                    .setEnableLogBuffer(true)
                    .setLogger(Logger.builder()
                                    .logPrefix("YAJSI")
                                    .logArea("YAJSI")
                                    .build()
                    );
        }
        return this;
    }

    @Override
    public YAJLManagerSettings setLogAreaFilterPatterns(List<String> logAreaFilterPatterns) {
        config.getLogAreaFilterConfig().setLogAreaFilterPatterns(logAreaFilterPatterns);
        config.getLogAreaFilterConfig().setLogFilter(new LogFilter(logAreaFilterPatterns));
        return this;
    }

    @Override
    public YAJLManagerSettings addLogAreaFilterPattern(String logAreaFilterPattern) {
        config.getLogAreaFilterConfig().getLogAreaFilterPatterns().add(logAreaFilterPattern);
        config.getLogAreaFilterConfig().getLogFilter().addFilterPattern(logAreaFilterPattern);
        return this;
    }

    @Override
    public YAJLManagerSettings addLogAreaFilterPatterns(String... logAreaFilterPatterns) {
        Arrays.stream(logAreaFilterPatterns).forEach(this::addLogAreaFilterPattern);
        return this;
    }

    @Override
    public YAJLManagerSettings setLogFileConfig(LogFileConfig logFileConfig) {
        config.setLogFileConfig(logFileConfig);
        return this;
    }

    @Override
    public YAJLManagerSettings setLogFileEnabled(boolean enabled) {
        config.getLogFileConfig().setEnable(enabled);
        logFileHandler.setEnabled(enabled);
        return this;
    }

    @Override
    public YAJLManagerSettings setLogFileLimitationMode(String limitationMode) {
        config.getLogFileConfig().setLimitationMode(limitationMode);
        return this;
    }

    @Override
    public YAJLManagerSettings setLogFileLimitationNumber(int limitationNumber) {
        config.getLogFileConfig().setLimitationNumber(limitationNumber);
        return this;
    }

    @Override
    public YAJLManagerSettings setLogFileCompressOldLogFiles(boolean compressOldLogFiles) {
        config.getLogFileConfig().setCompressOldLogFiles(compressOldLogFiles);
        return this;
    }

    @Override
    public YAJLManagerSettings setLogFileLogDirectory(String logDirectory) {
        config.getLogFileConfig().setLogDirectory(logDirectory);
        return this;
    }

    @Override
    public YAJLManagerSettings setLogFileLogFileName(String logFileName) {
        config.getLogFileConfig().setLogFileName(logFileName);
        return this;
    }

    @Override
    public YAJLManagerSettings setStackTraceLengthLimit(int stackTraceLengthLimit) {
        config.setStackTraceLengthLimit(stackTraceLengthLimit);
        return this;
    }

    @Override
    public YAJLManagerSettings setLogMessageLayout(String logMessageLayout) {
        config.setLogMessageLayout(logMessageLayout);
        return this;
    }

    @Override
    public YAJLManagerSettings setMinimumLogLevel(int minimumLogLevel) {
        config.setMinimumLogLevel(minimumLogLevel);
        return this;
    }

    @Override
    public YAJLManagerSettings setFilterPatternsAsBlacklist(boolean filterPatternsAsBlacklist) {
        config.getLogAreaFilterConfig().setFilterPatternsAsBlacklist(filterPatternsAsBlacklist);
        return this;
    }

    @Override
    public YAJLManagerSettings setLogStream(PrintStream logStream) {
        config.setLogStream(logStream);
        return this;
    }
}
