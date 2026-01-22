package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJSI.SettingsManager;
import com.toxicstoxm.YAJSI.upgrading.AutoUpgradingBehaviour;
import lombok.AccessLevel;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class LoggerManager {
    private static LoggerManager instance;
    public static LoggerManager getInstance(File configFileLocation) {
        if (instance == null) {
            instance = new LoggerManager(true, configFileLocation, null);
        }
        return instance;
    }

    public static LoggerManager getInstance() {
        if (instance == null) {
            instance = new LoggerManager(false, null, LoggerConfig.getDefaults());
        }
        return instance;
    }

    private LoggerManager(boolean useConfigFile, File configFileLocation, LoggerConfig settings) {
        if (useConfigFile) {
            SettingsManager.configure()
                    .addSupplier(LoggerConfig.class, LoggerConfig::getDefaults)
                    .autoUpgrade(true)
                    .autoUpgradeBehaviour(AutoUpgradingBehaviour.REMOVE)
                    .done();
            LoggerConfigBundle bundle = new LoggerConfigBundle(configFileLocation);
            SettingsManager.getInstance().registerConfig(bundle);
            this.settings = bundle.loggerConfig;
        } else {
            this.settings = settings;
        }
    }

    public static LoggerConfig getSettings() {
        return getInstance().settings;
    }

    public static void resetSettings() {getInstance().setSettings(LoggerConfig.builder().done());}

    @Contract(" -> new")
    public static @NotNull LoggerManager.LoggerBlueprint configure() {
        if (instance != null) {
            return new LoggerManager.LoggerBlueprint(getSettings());
        }

        return new LoggerManager.LoggerBlueprint();
    }

    public static class LoggerBlueprint extends LoggerConfig.LoggerConfigBuilder {
        private LogFilter logFilter;

        public LoggerBlueprint() {
            this(LoggerConfig.getDefaults());
        }

        public LoggerBlueprint(@NotNull LoggerConfig existingConfig) {
            output(existingConfig.getOutput());
            defaultLogLevel(existingConfig.getDefaultLogLevel());
            minimumLogLevel(existingConfig.getMinimumLogLevel());
            enableColorCoding(existingConfig.isEnableColorCoding());
            muteLogger(existingConfig.isMuteLogger());
            stackTraceLengthLimit(existingConfig.getStackTraceLengthLimit());
            logMessageLayout(existingConfig.getLogMessageLayout());
            filterPatternsAsBlacklist(existingConfig.isFilterPatternsAsBlacklist());
            logAreaFilterPatterns(existingConfig.getLogAreaFilterPatterns());
            this.logFilter = existingConfig.getLogFilter();
        }

        public LoggerBlueprint addLogFilterPattern(String pattern) {
            this.logFilter.addFilterPattern(pattern);
            return this;
        }

        @Override
        public LoggerConfig done() {
            LoggerConfig conf = super.done();
            conf.setLogFilter(this.logFilter);
            if (instance == null) {
                instance = new LoggerManager(false, null, conf);
            } else {
                instance.setSettings(conf);
            }
            return conf;
        }

        @Override
        public LoggerBlueprint logAreaFilterPatterns(List<String> logAreaFilterPatterns) {
            super.logAreaFilterPatterns(logAreaFilterPatterns);
            this.logFilter = new LogFilter(logAreaFilterPatterns);
            return this;
        }
    }

    @Setter(AccessLevel.PRIVATE)
    private LoggerConfig settings;

    @Contract("_ -> new")
    public static @NotNull Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Logger getVirtualLogger(String area) {
        return new Logger(area);
    }
}
