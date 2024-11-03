package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.areas.*;
import com.toxicstoxm.YAJL.colors.ColorConverter;
import com.toxicstoxm.YAJL.colors.YAJLMessage;
import com.toxicstoxm.YAJL.levels.YAJLLogLevels;
import com.toxicstoxm.YAJL.levels.LogLevel;
import com.toxicstoxm.YAJSI.api.settings.YAJSISettingsManager;
import lombok.Getter;
import lombok.NonNull;

import java.awt.*;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static com.toxicstoxm.YAJL.YAJLSettingsBundle.*;

public class YAJLLogger implements Logger {

    public record LogMessageBluePrint(LogLevel logLevel, LogArea logArea, String message) {}

    public LogAreaManager logAreaManager;
    public Spacer elementSpacer;
    public YAJSISettingsManager settingsManager;

    private LogArea defaultLogArea;
    private PrintStream out;

    @Getter
    private boolean showSettingsLog = false;
    public record LogMeta(LogLevel level, LogArea area) {}
    @Getter
    private LogMeta yajsiLogMeta = null;

    private boolean initialized = true;

    private static class TestBundle implements LogAreaBundle {}

    public static void main(String[] args) {
        System.out.println("YAJL (Yet another Java logger) is a library and can't be used as a standalone!");
        System.out.println("Running test:");
        YAJLLogger logger = YAJLLogger.withArea(System.getProperty("user.home"), System.out, new YAJLLogArea(new Color(0, 120, 255)), new TestBundle(), true);
        logger.debug("     If you see this congrats!       \n     If you see this double congrats!      ");
        System.out.println("Test was successful!");
    }

    private YAJLLogger() {
        initialized = false;
    }

    private void setShowSettingsLog(boolean showSettingsLog) {
        this.showSettingsLog = showSettingsLog;
    }
    private void setYajsiLogMeta(LogMeta logMeta) {
        this.yajsiLogMeta = logMeta;
    }

    public YAJLLogger(String projectDir, PrintStream out, LogArea defaultLogArea, boolean enableAreaWildcard) {
       init(projectDir, out, defaultLogArea, enableAreaWildcard);
    }

    public void init(String projectDir, PrintStream out, LogArea defaultLogArea, boolean enableAreaWildcard) {
        this.out = out;
        this.defaultLogArea = defaultLogArea;

        if (settingsManager == null) {
            settingsManager = YAJSISettingsManager.builder()
                    .buildWithConfigFile(
                            new YAJSISettingsManager.ConfigFile(
                                    projectDir + "/yajl-config.yaml",
                                    getClass().getClassLoader().getResource("yajl-config.yaml")
                            ),
                            YAJLSettingsBundle.class
                    );
        } else settingsManager = settingsManager
                .addConfigFile(new YAJSISettingsManager.YAMLConfig(
                new YAJSISettingsManager.ConfigFile(
                        projectDir + "/yajl-config.yaml",
                        getClass().getClassLoader().getResource("yajl-config.yaml")
                ),
                YAJLSettingsBundle.class
        ));
        logAreaManager = new YAJLLogAreaManger();
        if (enableAreaWildcard) logAreaManager.registerArea(new YAJLLogArea("ALL"));
        logAreaManager.enableAreasByName(ShownAreas.getInstance().get());
        elementSpacer = new YAJLSpacer();
    }

    public void saveSettings() {
        settingsManager.save();
    }

    public static YAJLLogger withAreas(String projectDir, PrintStream out, LogArea defaultLogArea, Collection<LogAreaBundle> logAreaBundles, boolean enableAreaWildcard) {
        YAJLLogger logger = new YAJLLogger(projectDir, out, defaultLogArea, enableAreaWildcard);
        for (LogAreaBundle logAreaBundle : logAreaBundles) {
            logger.logAreaManager.registerAreaBundle(logAreaBundle);
        }

        return logger;
    }
    public static YAJLLogger withArea(String projectDir, PrintStream out, LogArea defaultLogArea, LogAreaBundle logAreaBundle, boolean enableAreaWildcard) {
        YAJLLogger logger = new YAJLLogger(projectDir, out, defaultLogArea, enableAreaWildcard);
        logger.logAreaManager.registerAreaBundle(logAreaBundle);
        return logger;
    }

    @Deprecated(forRemoval = true)
    public com.toxicstoxm.YAJSI.api.logging.Logger getYAJSILogImpl() {
        return settingsManager.getLogger();
    }

    // factory methods

    public static YAJLLogger builder() {
        return new YAJLLogger();
    }

    public YAJLLogger setSettingsManager(YAJSISettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        return this;
    }

    public YAJLLogger configureYajsiLog(boolean showSettingsLog, LogMeta logMeta) {
        if (initialized) {
            setShowSettingsLog(showSettingsLog);
            setYajsiLogMeta(logMeta);
            settingsManager.getMessages().forEach(message -> {
                if (showSettingsLog) log(message, yajsiLogMeta.level, yajsiLogMeta.area);
            });
            settingsManager.setLoggingImplementation(message -> {if (showSettingsLog) log(message, logMeta.level, logMeta.area);});
        }
        return this;
    }

    public YAJLLogger buildWithAreas(String projectDir, PrintStream out, LogArea defaultLogArea, Collection<LogAreaBundle> logAreaBundles, boolean enableAreaWildcard) {
        build(projectDir, out, defaultLogArea, enableAreaWildcard);
        for (LogAreaBundle logAreaBundle : logAreaBundles) {
            logAreaManager.registerAreaBundle(logAreaBundle);
        }
        return this;
    }
    public YAJLLogger buildWithArea(String projectDir, PrintStream out, LogArea defaultLogArea, LogAreaBundle logAreaBundle, boolean enableAreaWildcard) {
        build(projectDir, out, defaultLogArea, enableAreaWildcard).registerLogAreaBundle(logAreaBundle);
        return this;
    }

    public YAJLLogger setEnableAreaWildcard(boolean enableAreaWildcard) {
        if (enableAreaWildcard) logAreaManager.registerArea(new YAJLLogArea("ALL"));
        else logAreaManager.unregisterArea(new YAJLLogArea("ALL"));
        return this;
    }

    public YAJLLogger build(String projectDir, PrintStream out, LogArea defaultLogArea, boolean enableAreaWildcard) {
        initialized = true;
        init(projectDir, out, defaultLogArea, enableAreaWildcard);
        return this;
    }

    // log are registering and unregistering logic

    public void registerLogAreaBundle(LogAreaBundle logAreaBundle) {
        logAreaManager.registerAreaBundle(logAreaBundle);
    }
    public void unregisterLogAreaBundle(LogAreaBundle logAreaBundle) {
        logAreaManager.unregisterAreaBundle(logAreaBundle);
    }

    @Override
    public void setDefaultLogArea(@NonNull LogArea logArea) {
        defaultLogArea = logArea;
    }

    @Override
    public LogArea getDefaultLogArea() {
        return defaultLogArea;
    }

    private boolean hasDefaultLogArea() {
        return defaultLogArea != null;
    }

    @Override
    public void fatal(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) fatal(message, defaultLogArea);

    }

    @Override
    public void error(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) error(message, defaultLogArea);

    }

    @Override
    public void warn(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) warn(message, defaultLogArea);

    }

    @Override
    public void info(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) info(message, defaultLogArea);

    }

    @Override
    public void debug(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) debug(message, defaultLogArea);

    }

    @Override
    public void verbose(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) verbose(message, defaultLogArea);

    }

    @Override
    public void stacktrace(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) stacktrace(message, defaultLogArea);

    }

    @Override
    public void log(String message, LogLevel level, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        level,
                        area,
                        message
                )
        );
    }

    @Override
    public void fatal(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new YAJLLogLevels.Fatal(),
                        area,
                        message
                )
        );
    }

    @Override
    public void error(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new YAJLLogLevels.Error(),
                        area,
                        message
                )
        );
    }

    @Override
    public void warn(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new YAJLLogLevels.Warn(),
                        area,
                        message
                )
        );
    }

    @Override
    public void info(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new YAJLLogLevels.Info(),
                        area,
                        message
                )
        );
    }

    @Override
    public void debug(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new YAJLLogLevels.Debug(),
                        area,
                        message
                )
        );
    }

    @Override
    public void verbose(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new YAJLLogLevels.Verbose(),
                        area,
                        message
                )
        );
    }

    @Override
    public void stacktrace(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new YAJLLogLevels.Stacktrace(),
                        area,
                        message
                )
        );
    }

    private void assembleLogMessage(LogMessageBluePrint bluePrint) {
        LogLevel logLevel = bluePrint.logLevel;
        LogArea logArea = bluePrint.logArea;
        String message = bluePrint.message;

        if (logArea == null || logArea.getColor() == null) logArea = defaultLogArea;

        if (logLevel.isEnabled() && logAreaManager.isAreaEnabled(logArea)) {
            for (String messageLine : message.split("\n")) {
                log(
                        YAJLMessage.builder()
                                .text(
                                        elementSpacer.getSpacingFor("timestamp", "[" + getTimestamp() + "]")
                                )
                                .color(
                                        EnableTrace.getInstance().get() &&
                                                EnableColorCoding.getInstance().get(),
                                        TraceColor.getInstance().get()
                                )
                                .text(
                                        EnableTrace.getInstance().get(),
                                        elementSpacer.getSpacingFor("trace", "[" + getTrace() + "]")
                                )
                                .reset(EnableTrace.getInstance().get())
                                .color(
                                        EnableColorCoding.getInstance().get(),
                                        logLevel.getColor()
                                )

                                .text(
                                        elementSpacer.getSpacingFor("level", "[" + logLevel.getText() + "]")
                                )
                                .reset()
                                .color(
                                        EnableAreas.getInstance().get() &&
                                                EnableColorCoding.getInstance().get(),
                                        logArea.getColor()
                                )
                                .text(
                                        EnableAreas.getInstance().get(),
                                        elementSpacer.getSpacingFor("area", "[" + logArea.getName() + "]" + Separator.getInstance().get())
                                )
                                .reset(EnableAreas.getInstance().get())
                                .color(
                                        EnableColorCoding.getInstance().get(),
                                        computeColor(logLevel, logArea)
                                )
                                .text(EnableNewlineSupport.getInstance().get() ? messageLine.replace("\n","") : message)
                                .reset()
                                .build()
                );
                if (!EnableNewlineSupport.getInstance().get()) break;
            }
        }
    }

    private Color computeColor(LogLevel level, LogArea area) {
        String mode = ColorCodingMode.getInstance().get();
        try {
            return switch (mode) {
                case "MIX" -> ColorConverter.mixColors(level.getColor(), area.getColor());
                case "AREA" -> area.getColor();
                case "LEVEL" -> level.getColor();
                case "STATIC" -> ColorConverter.getColorFromHex(ColorCodingStaticColor.getInstance().get());
                default -> hasDefaultLogArea() ? defaultLogArea.getColor() : new Color(-1, -1, -1);
            };
        } catch (IllegalArgumentException e) {
            log("Invalid mode '" + mode + "' Supported modes: 'MIX', 'AREA', 'LEVEL', 'STATIC'");
        }
        return new Color(0, 0, 0, 0);
    }

    private String getTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private boolean isLoggerDisabled() {
        return !EnableLogger.getInstance().get();
    }

    private String getTrace() {
        StackTraceElement[] currentStackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement traceElement : currentStackTrace) {
            String traceElementString = traceElement.toString();
            if (traceElementString.contains("com.toxicstoxm.LEDSuite") && !traceElementString.contains("logger")) return formatTrace(traceElementString);
        }
        return formatTrace(currentStackTrace[0].toString());
    }

    private String formatTrace(String trace) {
        return Arrays.stream(trace.split("\\(")).toList().getLast().replace("(", "").replace(")", "").replace(".java", "").strip();
    }

    private boolean isLogAreaEnabled() {
        List<String> enabledAreas = ShownAreas.getInstance().get();
        return true;
    }

    @Override
    public void log(String message) {
        if (!initialized) throw new RuntimeException(Arrays.stream(getClass().getName().split("\\.")).toList().getLast() + " wasn't initialized properly!");
        if (isLoggerDisabled()) return;
        out.println(message);
        out.flush();
    }
}
