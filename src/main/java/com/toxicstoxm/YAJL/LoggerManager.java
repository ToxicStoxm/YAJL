package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJSI.SettingsManager;
import com.toxicstoxm.YAJSI.upgrading.AutoUpgradingBehaviour;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class LoggerManager {
    private static LoggerManager instance;
    public static void initWithConfigFile(@NotNull File configFileLocation) {
        if (instance == null) {
            instance = new LoggerManager(true, configFileLocation, null);
        }
    }

    private static LoggerManager getInstance() {
        if (instance == null) {
            instance = new LoggerManager(false, null, LoggerConfig.getDefaults());
        }
        return instance;
    }

    @Getter
    private static final LogFileManager logFileManager = new LogFileManager();

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

        if (this.settings.isEnableLogFiles()) {
            logFileManager.init();
        }
    }

    public static LoggerConfig getSettings() {
        return getInstance().settings;
    }

    public static @NotNull LoggerBlueprint configure(@NotNull File configFileLocation) {
        initWithConfigFile(configFileLocation);
        return configure();
    }

    public static void resetSettings() {getInstance().setSettings(LoggerConfig.builder().done());}

    @Contract(" -> new")
    public static @NotNull LoggerBlueprint configure() {
        if (instance != null) {
            return new LoggerBlueprint(getSettings());
        }

        return new LoggerBlueprint();
    }

    public static class LoggerBlueprint extends LoggerConfig.LoggerConfigBuilder {
        private final LogFilter logFilter;
        private final List<PrintStream> outputs = new ArrayList<>();
        private boolean enableLogFiles;

        public LoggerBlueprint() {
            this(LoggerConfig.getDefaults());
        }

        public LoggerBlueprint(@NotNull LoggerConfig existingConfig) {
            this.outputs.addAll(existingConfig.getOutputs());
            defaultLogLevel(existingConfig.getDefaultLogLevel());
            minimumLogLevel(existingConfig.getMinimumLogLevel());
            enableColorCoding(existingConfig.isEnableColorCoding());
            muteLogger(existingConfig.isMuteLogger());
            stackTraceLengthLimit(existingConfig.getStackTraceLengthLimit());
            logMessageLayout(existingConfig.getLogMessageLayout());
            filterPatternsAsBlacklist(existingConfig.isFilterPatternsAsBlacklist());
            logAreaFilterPatterns(existingConfig.getLogAreaFilterPatterns());
            this.logFilter = existingConfig.getLogFilter();
            this.enableLogFiles = existingConfig.isEnableLogFiles();
            logFileLimit(existingConfig.getLogFileLimit());
            compressedFileSizeLimit(existingConfig.getCompressedFileSizeLimit());
            compressOldLogFiles(existingConfig.isCompressOldLogFiles());
            logDirectory(existingConfig.getLogDirectory());
            logFileNamePattern(existingConfig.getLogFileNamePattern());
        }

        public LoggerBlueprint addLogFilterPattern(String pattern) {
            this.logFilter.addFilterPattern(pattern);
            return this;
        }

        public LoggerBlueprint addOutput(PrintStream output) {
            this.outputs.add(output);
            return this;
        }

        @Override
        public LoggerConfig done() {
            LoggerConfig conf = super.done();
            conf.setLogFilter(this.logFilter);
            conf.setOutputs(this.outputs);

            if (conf.isEnableLogFiles() != this.enableLogFiles) {
                if (this.enableLogFiles) {
                    logFileManager.init();
                } else {
                    logFileManager.shutdown();
                }
            }
            conf.setEnableLogFiles(this.enableLogFiles);

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
            if (this.logFilter != null) {
                this.logFilter.setFilterPatterns(logAreaFilterPatterns);
            }
            return this;
        }

        @Override
        public LoggerConfig.LoggerConfigBuilder enableLogFiles(boolean enableLogFiles) {
            this.enableLogFiles = enableLogFiles;
            return this;
        }
    }

    @Setter(AccessLevel.PRIVATE)
    private LoggerConfig settings;

    private static volatile CompiledLayout compiledLayout;

    public static @NotNull CompiledLayout getCompiledLayout() {
        String layout = getSettings().getLogMessageLayout();
        CompiledLayout current = compiledLayout;

        if (current == null || !current.layout.equals(layout)) {
            CompiledLayout fresh =
                    new CompiledLayout(layout, Logger.parseLayout(layout));
            compiledLayout = fresh; // volatile write
            return fresh;
        }

        return current;
    }

    @Contract("_ -> new")
    public static @NotNull Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Logger getVirtualLogger(String area) {
        return new Logger(area);
    }

    private static Logger internal;

    protected static void internalError(String msg) {
        internalError(msg, null);
    }

    protected static void internalError(String msg, Exception e) {
        if (internal == null) {
            internal = new Logger("YAJL");
        }
        internal.error(msg);
        internal.stacktrace("REPLACE WITH PROPER EXCEPTION TRACE", e);
    }
}
