package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.level.LogLevel;
import com.toxicstoxm.YAJL.level.LogLevels;
import com.toxicstoxm.YAJL.old.placeholders.PlaceholderHandler;
import com.toxicstoxm.YAJL.old.placeholders.StringPlaceholder;
import com.toxicstoxm.YAJL.tools.ColorTools;
import com.toxicstoxm.YAJL.tools.TraceTools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;


import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {
    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{(\\w+)(?::([^}]*))?}");

    /**
     * A map of registered placeholder handlers used to associate placeholder strings with their corresponding replacement values.
     * <p>
     * This map is used to manage dynamic placeholders in log messages, where each placeholder string (e.g., "time", "level") is
     * mapped to a {@link PlaceholderHandler} that defines how to generate or retrieve the replacement for the placeholder.
     * </p>
     * <p>
     * The placeholder handlers are responsible for processing the placeholders and returning the appropriate value based on
     * the current context (e.g., time, log level, stack trace).
     * </p>
     */
    protected static Map<String, PlaceholderHandler> placeholderHandlers = new HashMap<>();

    static {
        // Initialize placeholder handlers

        // ==========================
        // Time Placeholder
        // ==========================
        // Retrieves the current time formatted according to the given format.
        // Default format: "HH:mm:ss"
        placeholderHandlers.put("time", args -> {
            String format = args.getOrDefault("format", () -> "HH:mm:ss").getData();
            return java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern(format));
        });

        // ==========================
        // Log Level Placeholder
        // ==========================
        // Retrieves the log level name from arguments or falls back to the default log level.
        placeholderHandlers.put("level", args ->
                args.getOrDefault("level", () -> LoggerManager.getSettings().getDefaultLogLevel().getName()).getData()
        );

        // ==========================
        // Log Level Color Placeholder
        // ==========================
        // Returns ANSI color representation of the log level color if color coding is enabled.
        placeholderHandlers.put("levelColor", args -> {
            if (LoggerManager.getSettings().isEnableColorCoding()) {
                return args.getOrDefault("levelColor", () ->
                        ColorTools.toAnsi(LoggerManager.getSettings().getDefaultLogLevel().getColor())
                ).getData();
            } else {
                return "";
            }
        });

        // ==========================
        // Log Message Placeholder
        // ==========================
        // Retrieves the actual log message or defaults to an empty string.
        placeholderHandlers.put("message", args ->
                args.getOrDefault("message", () -> "").getData()
        );

        // ==========================
        // Logger Prefix (Log Area) Placeholder
        // ==========================
        // Retrieves the logger prefix or defaults to "YAJL".
        placeholderHandlers.put("prefix", args ->
                args.getOrDefault("prefix", () -> "YAJL").getData()
        );

        // ==========================
        // Hex Color Placeholder
        // ==========================
        // Converts a hex color code to its ANSI equivalent if color coding is enabled.
        // Default color: White (#FFFFFF)
        placeholderHandlers.put("color", args -> {
            if (LoggerManager.getSettings().isEnableColorCoding()) {
                return ColorTools.toAnsi(Color.decode(args.getOrDefault("hex", () -> "#FFFFFF").getData()));
            } else {
                return "";
            }
        });

        // ==========================
        // Stacktrace Placeholder
        // ==========================
        // Constructs a stack trace element representation based on the provided arguments.
        // Supports `class`, `method`, and `line`, or falls back to a full trace format.
        placeholderHandlers.put("trace", args -> {
            StringBuilder finalTrace = new StringBuilder();
            String separator = Matcher.quoteReplacement(args.getOrDefault("separator", () -> ":").getData());

            if (args.containsKey("class") || args.containsKey("method") || args.containsKey("line")) {
                for (String key : args.keySet()) {
                    switch (key) {
                        case "class" -> {
                            if (!finalTrace.isEmpty()) finalTrace.append(separator);
                            finalTrace.append(args.getOrDefault("traceClass", () -> "Unknown").getData().replaceAll("\\$", "->"));
                        }
                        case "method" -> {
                            if (!finalTrace.isEmpty()) finalTrace.append(separator);
                            finalTrace.append(args.getOrDefault("traceMethod", () -> "Unknown").getData().replaceAll("\\$", "->"));
                        }
                        case "line" -> {
                            if (!finalTrace.isEmpty()) finalTrace.append(separator);
                            finalTrace.append(args.getOrDefault("traceLineNumber", () -> "0").getData().replaceAll("\\$", "->"));
                        }
                    }
                }
            } else {
                // Default to full stack trace format: class:method:line
                finalTrace.append(args.getOrDefault("traceClass", () -> "Unknown").getData().replaceAll("\\$", "->"));
                finalTrace.append(separator).append(args.getOrDefault("traceMethod", () -> "Unknown").getData().replaceAll("\\$", "->"));
                finalTrace.append(separator).append(args.getOrDefault("traceLineNumber", () -> "0").getData().replaceAll("\\$", "->"));
            }

            return finalTrace.toString();
        });

        // ==========================
        // Logger Prefix Color Placeholder
        // ==========================
        // Generates a random ANSI color for the logger prefix if color coding is enabled.
        placeholderHandlers.put("prefixColor", args -> {
            if (LoggerManager.getSettings().isEnableColorCoding()) {
                return ColorTools.toAnsi(ColorTools.randomColor(args.getOrDefault("prefix", () -> "YAJL").getData()));
            } else {
                return "";
            }
        });

        // ==========================
        // Mixed Log Level & Logger Prefix Color Placeholder
        // ==========================
        // Mixes the log level color and logger prefix color into a blended ANSI color if color coding is enabled.
        placeholderHandlers.put("mixLevelAndAreaColor", args -> {
            if (LoggerManager.getSettings().isEnableColorCoding()) {
                return ColorTools.toAnsi(
                        ColorTools.mixColors(
                                ColorTools.randomColor(args.getOrDefault("prefix", () -> "YAJL").getData()),
                                ColorTools.fromAnsi(args.getOrDefault("levelColor", () ->
                                        ColorTools.toAnsi(LoggerManager.getSettings().getDefaultLogLevel().getColor())
                                ).getData())
                        )
                );
            } else {
                return "";
            }
        });
    }

    public static List<LayoutToken> layoutTokens = new ArrayList<>();
    public static int layoutHash = 0;

    public static @NotNull @Unmodifiable List<LayoutToken> parseLayout(String layout) {
        Matcher m = PLACEHOLDER_PATTERN.matcher(layout);
        List<LayoutToken> tokens = new ArrayList<>();

        int lastEnd = 0;

        while (m.find()) {
            if (m.start() > lastEnd) {
                tokens.add(new TextToken(layout.substring(lastEnd, m.start())));
            }

            String key = m.group(1);
            String rawArgs = m.group(2);

            Map<String, String> staticArgs = new HashMap<>();
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

        return List.copyOf(tokens);
    }

    public final String logArea;
    public final String logPrefix;

    protected Logger(@NotNull Class<?> clazz) {
        logPrefix = clazz.getSimpleName();
        logArea = clazz.getName();
    }

    protected Logger(@NotNull String area) {
        this.logArea = area;
        this.logPrefix = area;
    }

    public void log(@NotNull String message) {
        log(LoggerManager.getSettings().getDefaultLogLevel(), message);
    }

    public void log(@NotNull LogLevel level, @NotNull String message) {
        if (shouldSkipLog(level)) return;

        if (message.contains("\n")) {
            for (String subMessage : message.split("\n")) {
                log(level, subMessage);
            }
            return;
        }

        String layout = LoggerManager.getSettings().getLogMessageLayout();

        if (layoutHash != layout.hashCode()) {
            layoutTokens = parseLayout(layout);
            layoutHash = layout.hashCode();
        }

        Map<String, StringPlaceholder> args = new HashMap<>();
        args.put("level", level::getName);
        args.put("levelColor", () -> ColorTools.toAnsi(level.getColor()));
        args.put("message", () -> message);
        args.put("prefix", () -> logPrefix);
        args.put("trace", () -> TraceTools.getCallerTraceFormatted(true, true, true));
        args.put("traceClass", () -> TraceTools.getCallerTraceFormatted(true, false, false));
        args.put("traceMethod", () -> TraceTools.getCallerTraceFormatted(false, true, false));
        args.put("traceLineNumber", () -> TraceTools.getCallerTraceFormatted(false, false, true));

        String finalMessage = renderLayout(layoutTokens, args) + ColorTools.ANSI_RESET;

        LoggerManager.getSettings().getOutput().println(finalMessage);

        //LoggerManager.getSettings().getOutput().println("[" + logPrefix + "] [" + level.getName() + "]: " + message);
    }

    public static @NotNull String renderLayout(@NotNull List<LayoutToken> tokens, Map<String, StringPlaceholder> runtimeArgs) {
        StringBuilder sb = new StringBuilder();

        for (LayoutToken token : tokens) {
            token.append(sb, runtimeArgs);
        }

        return sb.toString();
    }

    public void stacktrace(String message) {}
    public void stacktrace(String message, Object... args) {}

    public void verbose(String message) {}
    public void verbose(String message, Object... args) {}

    public void debug(String message) {}
    public void debug(String message, Object... args) {}

    public void info(String message) {
        log(LogLevels.INFO, message);
    }
    public void info(String message, Object... args) {}

    public void warn(String message) {}
    public void warn(String message, Object... args) {}

    public void error(String message) {}
    public void error(String message, Object... args) {}

    public void fatal(String message) {}
    public void fatal(String message, Object... args) {}


    public boolean shouldSkipLog(LogLevel level) {
        LoggerConfig settings = LoggerManager.getSettings();

        return settings.isMuteLogger() ||
                settings.getLogFilter().isFiltered(logArea) ||
                settings.getMinimumLogLevel() < level.getLevel();
    }
}
