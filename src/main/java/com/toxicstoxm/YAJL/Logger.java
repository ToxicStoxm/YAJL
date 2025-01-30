package com.toxicstoxm.YAJL;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Builder
public class Logger implements com.toxicstoxm.YAJSI.api.logging.Logger {

    @Builder.Default
    private String logPrefix = "";

    private static Map<String, PlaceholderHandler> placeholderHandlers = new HashMap<>();

    static {
        placeholderHandlers.put("time", args -> {
            String format = args.getOrDefault("format", () -> "HH:mm:ss").getData();
            return java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern(format));
        });
        placeholderHandlers.put("level", args -> args.getOrDefault("level", () -> YAJLManager.getInstance().config.getDefaultLogLevel().getName()).getData());
        placeholderHandlers.put("levelColor", args -> {
            if (YAJLManager.getInstance().config.isEnableColorCoding()) {
                return args.getOrDefault("levelColor", () -> ColorTools.toAnsi(YAJLManager.getInstance().config.getDefaultLogLevel().getColor())).getData();

            } else {
                return "";
            }
        });
        placeholderHandlers.put("message", args -> args.getOrDefault("message", () -> "").getData());
        placeholderHandlers.put("prefix", args -> args.getOrDefault("prefix", () -> "YAJL").getData());
        placeholderHandlers.put("color", args -> {
            if (YAJLManager.getInstance().config.isEnableColorCoding()) {
                return ColorTools.toAnsi(Color.decode(args.getOrDefault("hex", () -> "#FFFFFF").getData()));
            } else {
                return "";
            }
        });
        placeholderHandlers.put("trace", args -> {
            StringBuilder finalTrace = new StringBuilder();
            if (args.containsKey("class")) {
                finalTrace.append(args.getOrDefault("traceClass", () -> "Unknown").getData());
            }
            if (args.containsKey("method") || args.containsKey("function")) {
                finalTrace.append(":").append(args.getOrDefault("traceMethod", () -> "Unknown").getData());
            }
            if (args.containsKey("lineNumber") || args.containsKey("line")) {
                finalTrace.append(":").append(args.getOrDefault("traceLineNumber", () -> "0").getData());
            }

            return finalTrace.toString();
        });
    }

    /**
     * Automatically sets the log prefix to the classes name this function is called from.
     * @return a new, configured logger instance, ready for use
     */
    public static Logger autoConfigureLogger() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        String prefix = Arrays.stream(stackTraceElements[2].getClassName().split("\\.")).toList().getLast();

        return Logger.builder()
                .logPrefix(prefix == null || prefix.isBlank() ? "" : prefix)
                .build();
    }

    private Logger(@Nullable String prefix) {
        if (prefix != null) this.logPrefix = prefix;
    }

    public void stacktrace(String message) {
        log(LogLevels.STACKTRACE, message);
    }

    public void stacktrace(Exception exception) {
        if (exception == null) {
            stacktrace("Error, failed to print stacktrace: Exception is null!");
            return;
        }
        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        for (int i = 0; i < Math.min(stackTraceElements.length, YAJLManager.getInstance().config.getStackTraceLengthLimit()); i++) {
            StackTraceElement traceElement = stackTraceElements[i];
            if (traceElement != null) stacktrace("{}", traceElement);
        }
        if (stackTraceElements.length > YAJLManager.getInstance().config.getStackTraceLengthLimit()) {
            stacktrace("... <{} lines>", stackTraceElements.length - YAJLManager.getInstance().config.getStackTraceLengthLimit());
        }
    }

    public void stacktrace(String message, Object... objects) {
        log(LogLevels.STACKTRACE, message, objects);
    }

    public void verbose(String message) {
        log(LogLevels.VERBOSE, message);
    }

    public void verbose(String message, Object... objects) {
        log(LogLevels.VERBOSE, message, objects);
    }

    public void debug(String message) {
        log(LogLevels.DEBUG, message);
    }

    public void debug(Object object) {
        log(LogLevels.DEBUG, object == null ? "null" : "{}", object);
    }

    public void debug(String message, Object... objects) {
        log(LogLevels.DEBUG, message, objects);
    }

    public void info(String message) {
        log(LogLevels.INFO, message);
    }

    public void info(String message, Object... objects) {
        log(LogLevels.INFO, message, objects);
    }

    public void warn(String message) {
        log(LogLevels.WARN, message);
    }

    public void warn(String message, Object... objects) {
        log(LogLevels.WARN, message, objects);
    }

    public void error(String message) {
        log(LogLevels.ERROR, message);
    }

    public void error(String message, Exception exception) {
        log(LogLevels.ERROR, message);
        logException(LogLevels.ERROR, exception);
    }

    public void error(Exception exception) {
       logException(LogLevels.ERROR, exception);
    }

    public void error(String message, Object... objects) {
        log(LogLevels.ERROR, message, objects);
    }

    public void error(String message, Exception exception, Object... objects) {
        log(LogLevels.ERROR, message, objects);
        logException(LogLevels.ERROR, exception);
    }

    public void logException(LogLevel logLevel, Exception exception) {
        if (exception == null) {
            log(logLevel, "Error message: null");
            return;
        }
        log(logLevel, "Error message: {}", exception.getMessage());
        stacktrace(exception);
    }

    public void fatal(String message, Exception exception) {
        log(LogLevels.FATAL, message);
        logException(LogLevels.FATAL, exception);
    }

    public void fatal(Exception exception) {
        logException(LogLevels.FATAL, exception);
    }

    public void fatal(String message, Object... objects) {
        log(LogLevels.FATAL, message, objects);
    }

    public void fatal(String message, Exception exception, Object... objects) {
        log(LogLevels.FATAL, message, objects);
        logException(LogLevels.FATAL, exception);
    }


    public void log(@NotNull LogLevel logLevel, String message) {
        if (YAJLManager.getInstance().config.isMuteLogger()) return;

        String messageLayout = YAJLManager.getInstance().config.getLogMessageLayout();

        Map<String, StringPlaceholder> args = new HashMap<>();

        args.put("level", logLevel::getName);
        args.put("levelColor", () -> ColorTools.toAnsi(logLevel.getColor()));
        args.put("message", () -> message);
        args.put("prefix", () -> logPrefix);
        args.put("trace", () -> TraceTools.getCallerTraceFormatted(true, true, true));
        args.put("traceMethod", () -> TraceTools.getCallerTraceFormatted(false, true, false));
        args.put("traceClass", () -> TraceTools.getCallerTraceFormatted(true, false, false));
        args.put("traceLineNumber", () -> TraceTools.getCallerTraceFormatted(false, false, true));

        System.out.println(processLogMessage(messageLayout, args) + ColorTools.resetAnsi());
    }

    public String processLogMessage(String layout, Map<String, StringPlaceholder> args) {
        Pattern pattern = Pattern.compile("\\{(\\w+)(?::([^}]*))?}"); // Matches {placeholder:arg1,arg2,...}
        Matcher matcher = pattern.matcher(layout);

        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            String rawArgs = matcher.group(2);

            Map<String, StringPlaceholder> argMap = new HashMap<>(args);
            if (rawArgs != null) {
                for (String arg : rawArgs.split(",")) {
                    String[] kv = arg.split("=");
                    argMap.put(kv[0], () -> kv.length > 1 ? kv[1] : "");
                }
            }

            PlaceholderHandler handler = placeholderHandlers.get(key);
            String replacement = handler != null ? handler.process(argMap) : matcher.group(0); // Leave untouched if no handler
            matcher.appendReplacement(result, Objects.equals(key, "message") ? Matcher.quoteReplacement(replacement) : replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }


    public void log(@NotNull LogLevel logLevel, String message, Object... objects) {
        if (YAJLManager.getInstance().config.isMuteLogger()) return;
        log(logLevel, format(message, objects));
    }

    public String format(String message, Object... objects) {
        if (objects != null) {
            for (Object object : objects) {
                if (!message.contains(getPlaceholderLiteral())) {
                    throw new IllegalArgumentException("Message does not contain enough placeholders!");
                }

                if (object instanceof LogMessagePlaceholder placeholder) {

                    Object o = placeholder.getObject();

                    // Escape the replacement value to avoid Illegal group reference
                    message = message.replaceFirst(getPlaceholderRegex(), o == null ? "null" : Matcher.quoteReplacement(o.toString()));

                } else {
                    // Escape the replacement value to avoid Illegal group reference
                    message = message.replaceFirst(getPlaceholderRegex(), Matcher.quoteReplacement(object.toString()));
                }

            }
        }
        return message;
    }

    private @NotNull String getPlaceholderRegex() {
        String rawPlaceholder = "{}";
        return Pattern.quote(rawPlaceholder); // Escape for regex
    }

    private @NotNull String getPlaceholderLiteral() {
        return "{}";
    }


    @Override
    public void log(String s) {
        log(YAJLManager.getInstance().config.getDefaultLogLevel(), s);
    }
}
