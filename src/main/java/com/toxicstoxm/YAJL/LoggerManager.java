package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJSI.SettingsManager;
import lombok.AccessLevel;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

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
                    .done();
            LoggerConfigBundle bundle = new LoggerConfigBundle(configFileLocation);
            SettingsManager.getInstance().registerConfig(bundle);
            this.settings = bundle.loggerConfig;
        } else {
            this.settings = settings;
        }
    }

    public static LoggerConfig getSettings() {
        return getInstance().settings.toBuilder().done();
    }

    @Contract(" -> new")
    public static @NotNull LoggerConfig.LoggerConfigBuilder configure() {
        if (instance != null) {
            return new LoggerManager.LoggerBlueprint(getSettings());
        }

        return new LoggerManager.LoggerBlueprint();
    }

    public static class LoggerBlueprint extends LoggerConfig.LoggerConfigBuilder {
        public LoggerBlueprint() {
            this(LoggerConfig.getDefaults());
        }

        public LoggerBlueprint(@NotNull LoggerConfig existingConfig) {
            test(existingConfig.getTest());
        }

        @Override
        public LoggerConfig done() {
            LoggerConfig conf = super.done();
            if (instance == null) {
                instance = new LoggerManager(false, null, conf);
            } else {
                instance.setSettings(conf);
            }
            return conf;
        }
    }

    @Setter(AccessLevel.PRIVATE)
    private LoggerConfig settings;
}
