package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.level.LogLevel;
import com.toxicstoxm.YAJL.level.LogLevels;
import com.toxicstoxm.YAJL.placeholders.LogMessagePlaceholder;
import com.toxicstoxm.YAJL.placeholders.PlaceholderHandler;
import com.toxicstoxm.YAJL.placeholders.StringPlaceholder;
import com.toxicstoxm.YAJL.tools.ColorTools;
import com.toxicstoxm.YAJL.tools.StringTools;
import com.toxicstoxm.YAJL.tools.TraceTools;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main logging class for YAJL (Yet Another Java Logger).
 * <p>
 * This class provides functionality for logging messages at various log levels,
 * including automatic configuration and advanced logging features such as dynamic
 * message formatting, placeholder handling, and stack trace logging.
 * </p>
 *
 * @see #autoConfigureLogger() for automatic logger configuration.
 *
 * @author ToxicStoxm
 */
@Builder
public class Logger implements com.toxicstoxm.YAJSI.api.logging.Logger {

    @Builder.Default
    private String logPrefix = "";

    @Builder.Default
    private String logArea = "";

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
    private static Map<String, PlaceholderHandler> placeholderHandlers = new HashMap<>();

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
                args.getOrDefault("level", () -> YAJLManager.getInstance().config.getDefaultLogLevel().getName()).getData()
        );

        // ==========================
        // Log Level Color Placeholder
        // ==========================
        // Returns ANSI color representation of the log level color if color coding is enabled.
        placeholderHandlers.put("levelColor", args -> {
            if (YAJLManager.getInstance().config.isEnableColorCoding()) {
                return args.getOrDefault("levelColor", () ->
                        ColorTools.toAnsi(YAJLManager.getInstance().config.getDefaultLogLevel().getColor())
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
            if (YAJLManager.getInstance().config.isEnableColorCoding()) {
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
            if (YAJLManager.getInstance().config.isEnableColorCoding()) {
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
            if (YAJLManager.getInstance().config.isEnableColorCoding()) {
                return ColorTools.toAnsi(
                        ColorTools.mixColors(
                                ColorTools.randomColor(args.getOrDefault("prefix", () -> "YAJL").getData()),
                                ColorTools.fromAnsi(args.getOrDefault("levelColor", () ->
                                        ColorTools.toAnsi(YAJLManager.getInstance().config.getDefaultLogLevel().getColor())
                                ).getData())
                        )
                );
            } else {
                return "";
            }
        });
    }

    /**
     * Creates and returns a new logger instance with the log prefix automatically set
     * to the simple name of the class that invoked this method.
     * This method determines the caller class dynamically from the stack trace.
     *
     * @return a new, pre-configured logger instance with the appropriate log prefix and area
     */
    public static @NotNull Logger autoConfigureLogger() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String callerClassName = stackTraceElements[2].getClassName();

        String prefix = Arrays.stream(callerClassName.split("\\.")).toList().getLast();

        Logger newLogger = Logger.builder()
                .logPrefix(prefix == null || prefix.isBlank() ? "" : prefix)
                .logArea(callerClassName)
                .build();

        newLogger.debug("Initialized new logger: {} --> using autoconfigure", newLogger);

        return newLogger;
    }

    /**
     * Logs a message with STACKTRACE level.
     * @param message the log message
     */
    public void stacktrace(String message) {
        log(LogLevels.STACKTRACE, message);
    }

    /**
     * Logs a formatted stack trace of the specified Throwable.
     * @param throwable the exception whose stack trace will be logged
     */
    public void stacktrace(Throwable throwable) {
        if (throwable == null) {
            stacktrace("Error, failed to print stacktrace: Exception is null!");
            return;
        }
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        for (int i = 0; i < Math.min(stackTraceElements.length, YAJLManager.getInstance().config.getStackTraceLengthLimit()); i++) {
            StackTraceElement traceElement = stackTraceElements[i];
            if (traceElement != null) stacktrace("{}", traceElement);
        }
        if (stackTraceElements.length > YAJLManager.getInstance().config.getStackTraceLengthLimit()) {
            stacktrace("... <{} lines>", stackTraceElements.length - YAJLManager.getInstance().config.getStackTraceLengthLimit());
        }
    }

    /**
     * Formats the specified message format with the provided params and logs it to console with the {@link LogLevels#STACKTRACE} log level.
     * @param message log message format
     * @param objects params
     */
    public void stacktrace(String message, Object... objects) {
        log(LogLevels.STACKTRACE, message, objects);
    }

    /**
     * Logs a message with VERBOSE level.
     * @param message the log message
     */
    public void verbose(String message) {
        log(LogLevels.VERBOSE, message);
    }

    /**
     * Logs a Throwable with VERBOSE level.
     * @param throwable the exception to log
     */
    public void verbose(Throwable throwable) {
        logException(LogLevels.VERBOSE, throwable);
    }

    /**
     * Logs a message and an exception with VERBOSE level.
     * @param message the log message
     * @param throwable the exception to log
     */
    public void verbose(String message, Throwable throwable) {
        log(LogLevels.VERBOSE, message);
        logException(LogLevels.VERBOSE, throwable);
    }

    /**
     * Logs a formatted message with VERBOSE level, similar to printf-style formatting.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param objects the arguments to be formatted into the message
     */
    public void verbose(String message, Object... objects) {
        log(LogLevels.VERBOSE, message, objects);
    }

    /**
     * Logs a formatted message and an exception with VERBOSE level.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param throwable the exception to log
     * @param objects the arguments to be formatted into the message
     */
    public void verbose(String message, Throwable throwable, Object... objects) {
        log(LogLevels.VERBOSE, message, objects);
        logException(LogLevels.VERBOSE, throwable);
    }

    /**
     * Logs a message with DEBUG level.
     * @param message the log message
     */
    public void debug(String message) {
        log(LogLevels.DEBUG, message);
    }

    /**
     * Logs a Throwable with VERBOSE level.
     * @param throwable the exception to log
     */
    public void debug(Throwable throwable) {
        logException(LogLevels.DEBUG, throwable);
    }

    /**
     * Logs any Object with VERBOSE level.
     * @implNote uses {@link StringTools#computeToString(Object)} to serialize objects
     * @param object the exception to log
     */
    public void debug(Object object) {
        log(LogLevels.DEBUG, object == null ? "null" : "{}", object);
    }

    /**
     * Logs a message and an exception with DEBUG level.
     * @param message the log message
     * @param throwable the exception to log
     */
    public void debug(String message, Throwable throwable) {
        log(LogLevels.DEBUG, message);
        logException(LogLevels.DEBUG, throwable);
    }

    /**
     * Logs a formatted message with DEBUG level, similar to printf-style formatting.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param objects the arguments to be formatted into the message
     */
    public void debug(String message, Object... objects) {
        log(LogLevels.DEBUG, message, objects);
    }

    /**
     * Logs a formatted message and an exception with DEBUG level.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param throwable the exception to log
     * @param objects the arguments to be formatted into the message
     */
    public void debug(String message, Throwable throwable, Object... objects) {
        log(LogLevels.DEBUG, message, objects);
        logException(LogLevels.DEBUG, throwable);
    }

    /**
     * Logs a message with INFO level.
     * @param message the log message
     */
    public void info(String message) {
        log(LogLevels.INFO, message);
    }

    /**
     * Logs a Throwable with INFO level.
     * @param throwable the exception to log
     */
    public void info(Throwable throwable) {
        logException(LogLevels.INFO, throwable);
    }

    /**
     * Logs a message and an exception with INFO level.
     * @param message the log message
     * @param throwable the exception to log
     */
    public void info(String message, Throwable throwable) {
        log(LogLevels.INFO, message);
        logException(LogLevels.INFO, throwable);
    }

    /**
     * Logs a formatted message with INFO level, similar to printf-style formatting.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param objects the arguments to be formatted into the message
     */
    public void info(String message, Object... objects) {
        log(LogLevels.INFO, message, objects);
    }

    /**
     * Logs a formatted message and an exception with INFO level.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param throwable the exception to log
     * @param objects the arguments to be formatted into the message
     */
    public void info(String message, Throwable throwable, Object... objects) {
        log(LogLevels.INFO, message, objects);
        logException(LogLevels.INFO, throwable);
    }

    /**
     * Logs a message with WARN level.
     * @param message the log message
     */
    public void warn(String message) {
        log(LogLevels.WARN, message);
    }

    /**
     * Logs a Throwable with WARN level.
     * @param throwable the exception to log
     */
    public void warn(Throwable throwable) {
        logException(LogLevels.WARN, throwable);
    }

    /**
     * Logs a message and an exception with WARN level.
     * @param message the log message
     * @param throwable the exception to log
     */
    public void warn(String message, Throwable throwable) {
        log(LogLevels.WARN, message);
        logException(LogLevels.WARN, throwable);
    }

    /**
     * Logs a formatted message with WARN level, similar to printf-style formatting.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param objects the arguments to be formatted into the message
     */
    public void warn(String message, Object... objects) {
        log(LogLevels.WARN, message, objects);
    }

    /**
     * Logs a formatted message and an exception with WARN level.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param throwable the exception to log
     * @param objects the arguments to be formatted into the message
     */
    public void warn(String message, Throwable throwable, Object... objects) {
        log(LogLevels.WARN, message, objects);
        logException(LogLevels.WARN, throwable);
    }

    /**
     * Logs a message with ERROR level.
     * @param message the log message
     */
    public void error(String message) {
        log(LogLevels.ERROR, message);
    }

    /**
     * Logs a Throwable with ERROR level.
     * @param throwable the exception to log
     */
    public void error(Throwable throwable) {
        logException(LogLevels.ERROR, throwable);
    }

    /**
     * Logs a message and an exception with ERROR level.
     * @param message the log message
     * @param throwable the exception to log
     */
    public void error(String message, Throwable throwable) {
        log(LogLevels.ERROR, message);
        logException(LogLevels.ERROR, throwable);
    }

    /**
     * Logs a formatted message with ERROR level, similar to printf-style formatting.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param objects the arguments to be formatted into the message
     */
    public void error(String message, Object... objects) {
        log(LogLevels.ERROR, message, objects);
    }

    /**
     * Logs a formatted message and an exception with ERROR level.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param throwable the exception to log
     * @param objects the arguments to be formatted into the message
     */
    public void error(String message, Throwable throwable, Object... objects) {
        log(LogLevels.ERROR, message, objects);
        logException(LogLevels.ERROR, throwable);
    }

    /**
     * Logs a message with FATAL level.
     * @param message the log message
     */
    public void fatal(String message) {
        log(LogLevels.FATAL, message);
    }

    /**
     * Logs a Throwable with FATAL level.
     * @param throwable the exception to log
     */
    public void fatal(Throwable throwable) {
        logException(LogLevels.FATAL, throwable);
    }

    /**
     * Logs a message and an exception with Fatal level.
     * @param message the log message
     * @param throwable the exception to log
     */
    public void fatal(String message, Throwable throwable) {
        log(LogLevels.FATAL, message);
        logException(LogLevels.FATAL, throwable);
    }

    /**
     * Logs a formatted message with FATAL level, similar to printf-style formatting.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param objects the arguments to be formatted into the message
     */
    public void fatal(String message, Object... objects) {
        log(LogLevels.FATAL, message, objects);
    }

    /**
     * Logs a formatted message and an exception with FATAL level.
     * The message format string may contain placeholders ({}) that will be replaced by the provided parameters.
     * @param message the log message format string
     * @param throwable the exception to log
     * @param objects the arguments to be formatted into the message
     */
    public void fatal(String message, Throwable throwable, Object... objects) {
        log(LogLevels.FATAL, message, objects);
        logException(LogLevels.FATAL, throwable);
    }

    /**
     * Logs an exception with the specified log level, including its stack trace, cause, and suppressed exceptions.
     * This method ensures detailed exception logging while also including debugging metadata.
     *
     * @param logLevel   the severity level at which to log the exception
     * @param throwable  the exception to log; if {@code null}, a warning message is logged instead
     */
    public void logException(LogLevel logLevel, Throwable throwable) {
        if (throwable == null) {
            log(logLevel, "Error: Exception is null.");
            return;
        }

        // Include debugging metadata: thread name and timestamp
        log(LogLevels.DEBUG, "Thread: {}, Timestamp: {}", Thread.currentThread().getName(), System.currentTimeMillis());

        // Log the exception's class name and message
        log(logLevel, "Exception [{}]: {}", throwable.getClass().getName(), throwable.getMessage());

        // Log the full stack trace of the exception
        stacktrace(throwable);

        // Handle and log nested (caused by) exceptions
        Throwable cause = throwable.getCause();
        while (cause != null) {
            log(logLevel, "Caused by [{}]: {}", cause.getClass().getName(), cause.getMessage());
            stacktrace(cause);
            cause = cause.getCause();
        }

        // Handle and log suppressed exceptions
        Throwable[] suppressed = throwable.getSuppressed();
        if (suppressed.length > 0) {
            log(logLevel, "Suppressed exceptions:");
            for (Throwable sup : suppressed) {
                log(logLevel, " - [{}]: {}", sup.getClass().getName(), sup.getMessage());
                stacktrace(sup);
            }
        }
    }

    /**
     * Logs a formatted message with the specified log level.
     * If logging is muted or the log area is filtered out, this method exits without logging.
     *
     * @param logLevel the severity level at which to log the message
     * @param message  the log message template, which may contain placeholders ({})
     * @param objects  the arguments to be formatted into the message
     */
    public void log(@NotNull LogLevel logLevel, String message, Object... objects) {
        if (YAJLManager.getInstance().config.isMuteLogger() ||
                !YAJLManager.getInstance().config.getLogAreaFilterConfig().getLogFilter().isLogAreaAllowed(logArea)) {
            return;
        }

        log(logLevel, format(message, objects));
    }

    /**
     * Formats a message by replacing placeholders ({}) with provided arguments.
     * <p>
     * If there are more arguments than placeholders, extra arguments are ignored.
     * If there are fewer arguments than placeholders, processing stops early
     * and remaining placeholders will be treated as literals.
     *
     * @param message the log message template containing placeholders ({})
     * @param objects the arguments to replace placeholders with
     * @return the formatted message with placeholders replaced by their corresponding values
     */
    public String format(String message, Object... objects) {
        if (objects != null) {
            for (Object object : objects) {
                // Ensure the message has a placeholder available for replacement
                if (!message.contains(getPlaceholderLiteral())) {
                    break; // Stop processing if there are no more placeholders
                }

                // Determine whether the object is a LogMessagePlaceholder or a regular object
                if (object instanceof LogMessagePlaceholder placeholder) {
                    Object o = placeholder.getObject();

                    // Replace first placeholder with the computed string representation of the object
                    message = message.replaceFirst(
                            getPlaceholderRegex(),
                            o == null ? "null" : Matcher.quoteReplacement(StringTools.computeToString(o))
                    );
                } else {
                    // Replace first placeholder with the computed string representation of a regular object
                    message = message.replaceFirst(
                            getPlaceholderRegex(),
                            Matcher.quoteReplacement(StringTools.computeToString(object))
                    );
                }
            }
        }
        return message;
    }

    /**
     * Logs a message with the specified log level, applying filtering, formatting, and output handling.
     * <p>
     * This method:
     * - Respects logger muting and log area filtering.
     * - Skips messages below the configured minimum log level.
     * - Handles multi-line messages by splitting and logging each line separately.
     * - Constructs a formatted log message using a configurable layout.
     * - Outputs the log message to the console and the log file.
     * </p>
     *
     * @param logLevel the severity level at which to log the message
     * @param message  the log message, which may contain multiple lines
     */
    public void log(@NotNull LogLevel logLevel, String message) {
        // Check if logging is muted or if the log area is not allowed
        if (YAJLManager.getInstance().config.isMuteLogger() ||
                !YAJLManager.getInstance().config.getLogAreaFilterConfig().getLogFilter().isLogAreaAllowed(logArea)) {
            return;
        }

        // Skip logging if the message's level is below the configured minimum log level
        if (logLevel.getLevel() < YAJLManager.getInstance().config.getMinimumLogLevel()) {
            return;
        }

        // If the message contains multiple lines, log each line separately
        if (message.contains("\n")) {
            for (String subMessage : message.split("\n")) {
                log(logLevel, subMessage);
            }
            return;
        }

        // Retrieve the log message layout from the configuration
        String messageLayout = YAJLManager.getInstance().config.getLogMessageLayout();

        // Prepare placeholder values for the log message
        Map<String, StringPlaceholder> args = new HashMap<>();
        args.put("level", logLevel::getName);
        args.put("levelColor", () -> ColorTools.toAnsi(logLevel.getColor()));
        args.put("message", () -> message);
        args.put("prefix", () -> logPrefix);
        args.put("trace", () -> TraceTools.getCallerTraceFormatted(true, true, true));
        args.put("traceClass", () -> TraceTools.getCallerTraceFormatted(true, false, false));
        args.put("traceMethod", () -> TraceTools.getCallerTraceFormatted(false, true, false));
        args.put("traceLineNumber", () -> TraceTools.getCallerTraceFormatted(false, false, true));

        // Process the log message by replacing placeholders with actual values
        String finalLogMessage = processLogMessage(messageLayout, args) + ColorTools.resetAnsi();

        // Output the formatted log message to the log stream
        YAJLManager.getInstance().config.getLogStream().println(finalLogMessage);

        // Write the formatted log message to the log file
        YAJLManager.getInstance().logFileHandler.writeLogMessage(finalLogMessage);
    }

    /**
     * Processes a log message by replacing placeholders with corresponding values.
     * <p>
     * This method:
     * - Identifies placeholders in the provided layout using regex.
     * - Extracts placeholder keys and optional arguments.
     * - Uses predefined handlers to replace placeholders with formatted values.
     * - Ensures safe replacement by escaping special characters.
     * - Leaves placeholders untouched if no matching handler is found.
     * </p>
     *
     * @param layout the log message template containing placeholders in the format {@code {key:arg1,arg2,...}}
     * @param args   a map of placeholders and their corresponding values or functions to retrieve them
     * @return the processed log message with placeholders replaced
     */
    public String processLogMessage(String layout, Map<String, StringPlaceholder> args) {
        // Regex pattern to match placeholders in the format {key:arg1,arg2,...}
        Pattern pattern = Pattern.compile("\\{(\\w+)(?::([^}]*))?}");
        Matcher matcher = pattern.matcher(layout);

        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1); // Extract placeholder key
            String rawArgs = matcher.group(2); // Extract optional arguments

            // Use LinkedHashMap to maintain insertion order of arguments
            Map<String, StringPlaceholder> argMap = new LinkedHashMap<>(args);

            // Parse and store additional arguments if provided
            if (rawArgs != null) {
                for (String arg : rawArgs.split(",")) {
                    String[] kv = arg.split("=", 2);
                    argMap.put(kv[0], () -> kv.length > 1 ? kv[1] : "");
                }
            }

            // Retrieve the corresponding placeholder handler
            PlaceholderHandler handler = placeholderHandlers.get(key);

            // Process placeholder replacement; if no handler exists, leave placeholder unchanged
            String replacement = handler != null ? handler.process(argMap) : matcher.group(0);

            // Escape special characters to avoid issues with regex replacement
            if (replacement.contains("$") || replacement.contains("\\")) {
                replacement = replacement.replace("\\", "\\\\").replace("$", "\\$");
            }

            // Ensure safe replacement for "message" placeholders or unknown handlers
            if (Objects.equals(key, "message") || handler == null) {
                replacement = Matcher.quoteReplacement(replacement);
            }

            // Append the processed replacement to the result
            try {
                matcher.appendReplacement(result, replacement);
            } catch (Exception ignored) {
                // Suppress any exception that may occur during replacement
            }
        }

        // Append any remaining text from the original layout
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Returns the regex pattern for detecting placeholder literals in log messages.
     * <p>
     * The default placeholder is "{}". This method ensures that it is safely escaped
     * for use in regex-based replacement operations.
     * </p>
     *
     * @return a regex-safe version of the placeholder pattern
     */
    private @NotNull String getPlaceholderRegex() {
        String rawPlaceholder = "{}";
        return Pattern.quote(rawPlaceholder); // Escape for regex
    }

    /**
     * Returns the literal placeholder used in log messages.
     * <p>
     * The placeholder "{}" is used as a substitution marker for dynamic values.
     * </p>
     *
     * @return the placeholder string "{}"
     */
    private @NotNull String getPlaceholderLiteral() {
        return "{}";
    }

    /**
     * Logs a message using the default log level from the YAJL configuration.
     * <p>
     * This method is used to quickly log messages without specifying a log level.
     * The default log level is determined by the YAJLManager configuration.
     * </p>
     *
     * @param s the message to log
     */
    @Override
    public void log(String s) {
        log(YAJLManager.getInstance().config.getDefaultLogLevel(), s);
    }

    /**
     * Returns a string representation of the logger instance.
     * <p>
     * The format includes the logger's prefix (name) and log area (ID).
     * </p>
     *
     * @return a formatted string containing the logger name and ID
     */
    @Override
    public String toString() {
        return "[Name='" + logPrefix + "', ID='" + logArea + "']";
    }
}
