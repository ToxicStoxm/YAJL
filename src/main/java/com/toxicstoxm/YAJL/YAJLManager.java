package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.area.LogFilter;
import com.toxicstoxm.YAJL.config.LogFileConfig;
import com.toxicstoxm.YAJL.config.YAJLManagerConfig;
import com.toxicstoxm.YAJL.config.YAJLManagerSettings;
import com.toxicstoxm.YAJL.level.LogLevel;
import com.toxicstoxm.YAJSI.api.settings.SettingsManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class YAJLManager implements YAJLManagerSettings {

    protected YAJLManagerConfig config = YAJLManagerConfig.builder().build();
    protected LogFileHandler logFileHandler;

    private YAJLManager() {}

    private static YAJLManager instance = null;

    public static YAJLManager getInstance() {
        if (instance == null) {
            instance = new YAJLManager();
            instance.init();
        }
        return instance;
    }

    private void init() {
        if (config.isEnableYAMLConfig()) {
            SettingsManager.getInstance().registerYAMLConfiguration(config);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!config.isMuteLogger()) {
                System.out.println("[YAJL] Processing shutdown handles");
            }
            if (config.isEnableYAMLConfig()) {
                if (!config.isMuteLogger()) {
                    System.out.println("[YAJL] Saving YAML configuration.");
                }
                SettingsManager.getInstance().save();
            }
            if (!config.isMuteLogger()) {
                System.out.println("[YAJL] Shutting down log file handler");
            }
            logFileHandler.shutdown();

            if (!config.isMuteLogger()) {
                System.out.println("[YAJL] Shutting down, Goodbye!");
            }
        }));

        setLogAreaFilterPatterns(config.getLogAreaFilterPatterns());
        logFileHandler = new LogFileHandler();
        setBridgeYAJSI(config.isBridgeYAJSI());

    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull YAJLManagerSettings configure() {
        return getInstance();
    }

    public YAJLManagerSettings setDefaultLogLevel(LogLevel defaultLogLevel) {
        config.setDefaultLogLevel(defaultLogLevel);
        return this;
    }

    public YAJLManagerSettings setEnableColorCoding(boolean enableColorCoding) {
        config.setEnableYAMLConfig(enableColorCoding);
        return this;
    }

    public YAJLManagerSettings setMuteLogger(boolean muteLogger) {
        config.setMuteLogger(muteLogger);
        return this;
    }

    public YAJLManagerSettings setEnableYAMLConfig(boolean enableYAMLConfig) {
        config.setEnableYAMLConfig(enableYAMLConfig);
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
        config.setLogAreaFilterPatterns(logAreaFilterPatterns);
        config.setLogFilter(new LogFilter(logAreaFilterPatterns));
        return this;
    }

    @Override
    public YAJLManagerSettings addLogAreaFilterPattern(String logAreaFilterPattern) {
        config.getLogAreaFilterPatterns().add(logAreaFilterPattern);
        config.getLogFilter().addFilterPattern(logAreaFilterPattern);
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
}
