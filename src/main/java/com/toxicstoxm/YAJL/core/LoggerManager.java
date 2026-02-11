package com.toxicstoxm.YAJL.core;

import com.toxicstoxm.YAJL.io.LogFileManager;
import com.toxicstoxm.YAJL.layout.*;
import com.toxicstoxm.YAJSI.SettingsManager;
import com.toxicstoxm.YAJSI.upgrading.AutoUpgradingBehaviour;
import lombok.AccessLevel;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoggerManager {
    private static volatile LoggerManager instance;
    public static synchronized void initWithConfigFile(@NotNull File configFileLocation) {
        if (instance == null) {
            instance = new LoggerManager(true, configFileLocation, null);
        }
    }

    private static LoggerManager getInstance() {
        LoggerManager local = instance;
        if (local == null) {
            synchronized (LoggerManager.class) {
                local = instance;
                if (local == null) {
                    local = new LoggerManager(false, null, LoggerConfig.getDefaults());
                    instance = local;
                }
            }
        }
        return local;
    }

    private static final LogFileManager logFileManager = new LogFileManager();

    protected static void writeLogFile(String message) {
        logFileManager.writeLogMessage(message);
    }

    private LoggerManager(boolean useConfigFile, File configFileLocation, LoggerConfig settings) {
        System.out.println("FDUCK");
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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (getSettings().isEnableLogFiles()) {
                logFileManager.shutdown();
            }
        }));
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
    public static synchronized @NotNull LoggerBlueprint configure() {
        return new LoggerBlueprint(getSettings());
    }

    public static class LoggerBlueprint extends LoggerConfig.LoggerConfigBuilder {
        private final List<PrintStream> outputs = new ArrayList<>();
        private final List<String> logAreaFilterPatterns = new ArrayList<>();
        private boolean logFilterChanges = false;
        private boolean filterPatternsAsBlacklist;

        public LoggerBlueprint(@NotNull LoggerConfig existingConfig) {
            System.out.println("FFF");
            this.outputs.addAll(existingConfig.getOutputs());
            this.logAreaFilterPatterns.addAll(existingConfig.getLogAreaFilterPatterns());
            defaultLogLevel(existingConfig.getDefaultLogLevel());
            minimumLogLevel(existingConfig.getMinimumLogLevel());
            enableColorCoding(existingConfig.isEnableColorCoding());
            muteLogger(existingConfig.isMuteLogger());
            stackTraceLengthLimit(existingConfig.getStackTraceLengthLimit());
            logMessageLayout(existingConfig.getLogMessageLayout());
            this.filterPatternsAsBlacklist = existingConfig.isFilterPatternsAsBlacklist();
            logFilter(existingConfig.getLogFilter());
            enableLogFiles(existingConfig.isEnableLogFiles());
            enableLogFiles(existingConfig.isEnableLogFiles());
            logFileLimit(existingConfig.getLogFileLimit());
            compressedFileSizeLimit(existingConfig.getCompressedFileSizeLimit());
            compressOldLogFiles(existingConfig.isCompressOldLogFiles());
            logDirectory(existingConfig.getLogDirectory());
            logFileNamePattern(existingConfig.getLogFileNamePattern());
            internalLog(existingConfig.isInternalLog());
            internalLogOutput(existingConfig.getInternalLogOutput());
        }

        public LoggerBlueprint addLogFilterPattern(String pattern) {
            this.logAreaFilterPatterns.add(pattern);
            this.logFilterChanges = true;
            return this;
        }

        public LoggerBlueprint addOutput(PrintStream output) {
            this.outputs.add(output);
            return this;
        }

        @Override
        public LoggerConfig done() {
            super.outputs(List.copyOf(this.outputs));
            final List<String> filterPatterns = List.copyOf(this.logAreaFilterPatterns);
            super.logAreaFilterPatterns(filterPatterns);
            if (logFilterChanges) {
                super.logFilter(new LogFilter(filterPatterns, filterPatternsAsBlacklist));
            }
            LoggerConfig conf = super.done();

            synchronized (LoggerManager.class) {
                LoggerManager mgr = getInstance();
                LoggerConfig old = mgr.settings;

                mgr.setSettings(conf);

                if (conf.isEnableLogFiles() && !old.isEnableLogFiles()) {
                    logFileManager.init();
                } else if (!conf.isEnableLogFiles() && old.isEnableLogFiles()) {
                    logFileManager.shutdown();
                }
            }

            return conf;
        }

        @Override
        public LoggerConfig.LoggerConfigBuilder logAreaFilterPatterns(List<String> logAreaFilterPatterns) {
            this.logFilterChanges = true;
            return super.logAreaFilterPatterns(logAreaFilterPatterns);
        }

        @Override
        public LoggerConfig.LoggerConfigBuilder filterPatternsAsBlacklist(boolean filterPatternsAsBlacklist) {
            this.logFilterChanges = true;
            this.filterPatternsAsBlacklist = filterPatternsAsBlacklist;
            return this;
        }
    }

    @Setter(value = AccessLevel.PRIVATE)
    private volatile LoggerConfig settings;

    private static volatile CachedLayout cached;

    public static @NotNull CompiledLayout getCompiledLayout() {
        LayoutCacheKey newKey =
                new LayoutCacheKey(
                        getSettings().getLogMessageLayout(),
                        getSettings().isEnableColorCoding()
                );

        CachedLayout current = cached;
        if (current == null || !current.key().equals(newKey)) {
            ParsedLayout parsedLayout = parseLayout(newKey.layout());
            CompiledLayout fresh =
                    new CompiledLayout(newKey.layout(), parsedLayout);
            cached = new CachedLayout(newKey, fresh); // single volatile write
            return fresh;
        }

        return current.layout();
    }

    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{(\\w+)(?::([^}]*))?}");

    private static @NotNull @Unmodifiable ParsedLayout parseLayout(String layout) {
        Matcher m = PLACEHOLDER_PATTERN.matcher(layout);
        List<LayoutToken> tokens = new ArrayList<>();

        int lastEnd = 0;
        boolean hasColor = false;

        while (m.find()) {
            if (m.start() > lastEnd) {
                tokens.add(new TextToken(layout.substring(lastEnd, m.start())));
            }

            String key = m.group(1);
            String rawArgs = m.group(2);

            if (key.toLowerCase().contains("color")) hasColor = true;

            Map<String, String> staticArgs = new LinkedHashMap<>();
            if (rawArgs != null) {
                for (String arg : rawArgs.split(",")) {
                    String[] kv = arg.split("=", 2);
                    staticArgs.put(kv[0], kv.length > 1 ? kv[1] : "");
                }
            }

            tokens.add(new PlaceholderToken(key, staticArgs));
            lastEnd = m.end();
        }

        if (lastEnd < layout.length()) {
            tokens.add(new TextToken(layout.substring(lastEnd)));
        }

        return new ParsedLayout(List.copyOf(tokens), hasColor);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Logger getLogger(@NotNull Class<?> clazz) {
        return new Logger(clazz.getName(), clazz.getSimpleName());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Logger getVirtualLogger(@NotNull String area) {
        return new Logger(area, area);
    }

    protected static void internalLog(String msg) {
        internalLog(msg, null);
    }

    protected static void internalLog(String msg, Exception e) {
        if (!getSettings().isInternalLog()) return;
        PrintStream out = getSettings().getInternalLogOutput();

        out.println("[YAJL]: " + msg);
        if (e != null) {
            e.printStackTrace(out);
        }
    }
}
