package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.areas.*;
import com.toxicstoxm.YAJL.colors.ColorConverter;
import com.toxicstoxm.YAJL.colors.YAJLMessage;
import com.toxicstoxm.YAJL.levels.YAJLLogLevels;
import com.toxicstoxm.YAJL.levels.LogLevel;
import com.toxicstoxm.YAJSI.api.settings.YAJSISettingsManager;
import lombok.NonNull;

import java.awt.*;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.toxicstoxm.YAJL.YAJLSettingsBundle.*;

public class YAJLLogger implements Logger {

    public record LogMessageBluePrint(LogLevel logLevel, LogArea logArea, String message) {}

    public LogAreaManager logAreaManager;
    public Spacer elementSpacer;
    public YAJSISettingsManager settingsManager;

    private LogArea defaultLogArea;
    private final PrintStream out;

    private static class TestBundle implements LogAreaBundle {

    }

    public static void main(String[] args) {
        System.out.println("YAJL (Yet another Java logger) is a library and can't be used as a standalone!");
        System.out.println("Running test:");
        YAJLLogger logger = YAJLLogger.withArea(System.getProperty("user.home"), System.out, new YAJLLogArea(new Color(0, 120, 255)), new TestBundle(), true);
        logger.debug("If you see this congrats!");
        System.out.println("Test was successful!");
    }

    public YAJLLogger(String projectDir, PrintStream out, LogArea defaultLogArea, boolean enableAreaWildcard) {
        this.out = out;
        this.defaultLogArea = defaultLogArea;

        settingsManager = YAJSISettingsManager.withConfigFile(
                new YAJSISettingsManager.ConfigFile(
                        projectDir + "/yajl-config.yaml",
                        getClass().getClassLoader().getResource("yajl-config.yaml")
                ),
                YAJLSettingsBundle.class
        );

        logAreaManager = new YAJLLogAreaManger();
        if (enableAreaWildcard) logAreaManager.registerArea(new YAJLLogArea("ALL"));
        logAreaManager.enableAreasByName(ShownAreas.getInstance().get());
        elementSpacer = new YAJLSpacer();
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

    public void setEnableAreaWildcard(boolean enableAreaWildcard) {
        if (enableAreaWildcard) logAreaManager.registerArea(new YAJLLogArea("ALL"));
        else logAreaManager.unregisterArea(new YAJLLogArea("ALL"));
    }

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
    public void fatal(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new YAJLLogLevels.Fatal(
                                EnableFatalLevel.getInstance().get(),
                                FatalText.getInstance().get(),
                                ColorConverter.getColorFromHex(FatalColor.getInstance().get())
                        ),
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
                        new YAJLLogLevels.Error(
                                EnableErrorLevel.getInstance().get(),
                                ErrorText.getInstance().get(),
                                ColorConverter.getColorFromHex(ErrorColor.getInstance().get())
                        ),
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
                        new YAJLLogLevels.Warn(
                                EnableWarnLevel.getInstance().get(),
                                WarnText.getInstance().get(),
                                ColorConverter.getColorFromHex(WarnColor.getInstance().get())
                        ),
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
                        new YAJLLogLevels.Info(
                                EnableInfoLevel.getInstance().get(),
                                InfoText.getInstance().get(),
                                ColorConverter.getColorFromHex(InfoColor.getInstance().get())
                        ),
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
                        new YAJLLogLevels.Debug(
                                EnableDebugLevel.getInstance().get(),
                                DebugText.getInstance().get(),
                                ColorConverter.getColorFromHex(DebugColor.getInstance().get())
                        ),
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
                        new YAJLLogLevels.Verbose(
                                EnableVerboseLevel.getInstance().get(),
                                VerboseText.getInstance().get(),
                                ColorConverter.getColorFromHex(VerboseColor.getInstance().get())
                        ),
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
                        new YAJLLogLevels.Stacktrace(
                                EnableStacktraceLevel.getInstance().get(),
                                StacktraceText.getInstance().get(),
                                ColorConverter.getColorFromHex(StacktraceColor.getInstance().get())
                        ),
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
            log(
                    YAJLMessage.builder()
                            .text(
                                    elementSpacer.getSpacingFor("timestamp","[" + getTimestamp() + "]")
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
                            .text(message)
                            .reset()
                            .build()
            );
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
        if (isLoggerDisabled()) return;
        out.println(message);
        out.flush();
    }
}
