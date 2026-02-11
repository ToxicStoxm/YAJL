package com.toxicstoxm.YAJL.core;

import com.toxicstoxm.YAJL.core.level.LogLevel;
import com.toxicstoxm.YAJL.core.level.LogLevels;
import com.toxicstoxm.YAJL.layout.CompiledLayout;
import com.toxicstoxm.YAJL.layout.LayoutToken;
import com.toxicstoxm.YAJL.util.CachingSupplier;
import com.toxicstoxm.YAJL.util.tools.ColorTools;
import com.toxicstoxm.YAJL.util.tools.TraceTools;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class Logger {
    private final String logArea;
    private final String logPrefix;

    protected Logger(String area, String prefix) {
        this.logArea = area;
        this.logPrefix = prefix;
    }

    public void log(@NotNull String message, Object... args) {
        log(LoggerManager.getSettings().getDefaultLogLevel(), message, args);
    }

    public boolean shouldSkipLog(LogLevel level) {
        LoggerConfig settings = LoggerManager.getSettings();

        return settings.isMuteLogger() ||
                settings.getLogFilter().isFiltered(logArea) ||
                settings.getMinimumLogLevel() < level.getLevel();
    }

    public void log(@NotNull LogLevel level, @NotNull String message, Object... args) {
        if (shouldSkipLog(level)) return;

        if (args.length > 0 && message.contains("{}")) {
            message = formatMessage(message, args);
        }

        if (message.contains("\n")) {
            for (String subMessage : message.split("\n")) {
                if (subMessage.isEmpty()) continue;
                log(level, subMessage);
            }
            return;
        }

        CompiledLayout layout = LoggerManager.getCompiledLayout();
        RenderContext colored = new RenderContext(true);
        RenderContext plain = new RenderContext(false);

        LogEnvironment env = new LogEnvironment(level, message, logPrefix, new CachingSupplier<>(TraceTools::getCaller));

        String finalMessage = renderLayout(layout.getTokens(), env, colored);
        if (layout.isColored()) {
            finalMessage += ColorTools.ANSI_RESET;
        }

        for (PrintStream output : LoggerManager.getSettings().getOutputs()) {
            output.println(finalMessage);
        }

        if (LoggerManager.getSettings().isEnableLogFiles()) {
            final String plainMessage = renderLayout(layout.getTokens(), env, plain);

            LoggerManager.writeLogFile(plainMessage);
        }
    }

    private static @NotNull String formatMessage(@NotNull String message, Object @NotNull [] args) {
        if (args.length == 0 || message.indexOf('{') == -1) {
            return message;
        }

        StringBuilder sb = new StringBuilder(message.length() + 16 * args.length);

        int argIndex = 0;
        int i = 0;
        int len = message.length();

        while (i < len) {
            char c = message.charAt(i);

            // Escape handling
            if (c == '\\') {
                if (i + 2 < len && message.charAt(i + 1) == '{' && message.charAt(i + 2) == '}') {
                    // Escaped placeholder → emit "{}"
                    sb.append("{}");
                    i += 3;
                    continue;
                }

                // Just a normal backslash
                sb.append(c);
                i++;
                continue;
            }

            // Placeholder
            if (c == '{' && i + 1 < len && message.charAt(i + 1) == '}') {
                if (argIndex < args.length) {
                    Object arg = args[argIndex++];
                    if (arg instanceof Supplier supplier) {
                        arg = supplier.get();
                    }
                    sb.append(arg == null ? "null" : arg);
                } else {
                    sb.append("{}");
                }
                i += 2;
                continue;
            }

            // Normal character
            sb.append(c);
            i++;
        }

        return sb.toString();
    }

    public static @NotNull String renderLayout(@NotNull List<LayoutToken> tokens, LogEnvironment env, RenderContext context) {
        StringBuilder sb = new StringBuilder();

        for (LayoutToken token : tokens) {
            token.append(sb, env, context);
        }

        return sb.toString();
    }

    public void stacktrace(String message) {
        log(LogLevels.STACKTRACE, message);
    }
    public void stacktrace(String message, Object... args) {
        log(LogLevels.STACKTRACE, message, args);
    }

    public void verbose(String message) {
        log(LogLevels.VERBOSE, message);
    }
    public void verbose(String message, Object... args) {
        log(LogLevels.VERBOSE, message, args);
    }

    public void debug(String message) {
        log(LogLevels.DEBUG, message);
    }
    public void debug(String message, Object... args) {
        log(LogLevels.DEBUG, message, args);
    }

    public void info(String message) {
        log(LogLevels.INFO, message);
    }
    public void info(String message, Object... args) {
        log(LogLevels.INFO, message, args);
    }

    public void warn(String message) {
        log(LogLevels.WARN, message);
    }
    public void warn(String message, Object... args) {
        log(LogLevels.WARN, message, args);
    }

    public void error(String message) {
        log(LogLevels.ERROR, message);
    }
    public void error(String message, Object... args) {
        log(LogLevels.ERROR, message, args);
    }

    public void fatal(String message) {
        log(LogLevels.FATAL, message);
    }
    public void fatal(String message, Object... args) {
        log(LogLevels.FATAL, message, args);
    }
}
