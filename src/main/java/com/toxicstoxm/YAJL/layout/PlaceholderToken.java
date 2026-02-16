package com.toxicstoxm.YAJL.layout;

import com.toxicstoxm.YAJL.core.LogEnvironment;
import com.toxicstoxm.YAJL.core.LoggerManager;
import com.toxicstoxm.YAJL.core.RenderContext;
import com.toxicstoxm.YAJL.util.tools.ColorTools;
import com.toxicstoxm.YAJL.util.tools.TraceTools;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public final class PlaceholderToken implements LayoutToken {
    private final String key;
    private final Map<String, String> staticArgs;

    public PlaceholderToken(String key, Map<String, String> staticArgs) {
        this.key = key;
        this.staticArgs = staticArgs;
    }

    @Override
    public void append(StringBuilder out, LogEnvironment env, RenderContext context
    ) {
        PlaceholderHandler handler = placeholderHandlers.get(key);

        if (handler == null) {
            out.append('{').append(key).append('}');
            return;
        }

        out.append(handler.process(env, staticArgs, context));
    }

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
    private static final Map<String, PlaceholderHandler> placeholderHandlers = new HashMap<>();

    static {
        // Initialize placeholder handlers

        // ==========================
        // Time Placeholder
        // ==========================
        // Retrieves the current time formatted according to the given format.
        // Default format: "HH:mm:ss"
        placeholderHandlers.put("time", (_, args, _) -> {
            String format = args.getOrDefault("format", "HH:mm:ss");
            return LocalTime.now().format(DateTimeFormatter.ofPattern(format));
        });

        // ==========================
        // Log Level Placeholder
        // ==========================
        // Retrieves the log level name from arguments or falls back to the default log level.
        placeholderHandlers.put("level", (env, _, _) -> env.level().getName());

        // ==========================
        // Log Level Color Placeholder
        // ==========================
        // Returns ANSI color representation of the log level color if color coding is enabled.
        placeholderHandlers.put("levelColor", (env, _, context) -> {
            if (LoggerManager.getSettings().isEnableColorCoding() && context.color()) {
                return ColorTools.toAnsi(env.level().getColor());
            } else {
                return "";
            }
        });

        // ==========================
        // Log Message Placeholder
        // ==========================
        // Retrieves the actual log message or defaults to an empty string.
        placeholderHandlers.put("message", (env, _, _) -> env.message());

        // ==========================
        // Logger Prefix (Log Area) Placeholder
        // ==========================
        // Retrieves the logger prefix or defaults to "YAJL".
        placeholderHandlers.put("prefix", (env, _, _) -> env.prefix());

        // ==========================
        // Hex Color Placeholder
        // ==========================
        // Converts a hex color code to its ANSI equivalent if color coding is enabled.
        // Default color: White (#FFFFFF)
        placeholderHandlers.put("color", (_, args, context) -> {
            String color = args.get("hex");
            if (LoggerManager.getSettings().isEnableColorCoding() && color != null && context.color()) {
                return ColorTools.toAnsi(Color.decode(color));
            } else {
                return "";
            }
        });

        // ==========================
        // Stacktrace Placeholder
        // ==========================
        // Constructs a stack trace element representation based on the provided arguments.
        // Supports `class`, `method`, and `line`, or falls back to a full trace format.
        placeholderHandlers.put("trace", (env, args, _) -> {
            final String separator = args.getOrDefault("separator", ":");

            // Remove "separator" from the ordered keys
            java.util.List<String> fields = null;

            for (String key : args.keySet()) {
                if (!key.equals("separator")) {
                    if (fields == null) fields = new ArrayList<>(3);
                    fields.add(key);
                }
            }

            // Default order if nothing specified
            return TraceTools.formatCallerOrdered(
                    env.callerInfo().get(),
                    Objects.requireNonNullElseGet(fields, () -> List.of("class", "method", "line")),
                    separator
            );
        });

        // ==========================
        // Logger Prefix Color Placeholder
        // ==========================
        // Generates a random ANSI color for the logger prefix if color coding is enabled.
        placeholderHandlers.put("prefixColor", (env, _, context) -> {
            if (LoggerManager.getSettings().isEnableColorCoding() && context.color()) {
                return ColorTools.toAnsi(ColorTools.randomColor(env.prefix()));
            } else {
                return "";
            }
        });

        // ==========================
        // Mixed Log Level & Logger Prefix Color Placeholder
        // ==========================
        // Mixes the log level color and logger prefix color into a blended ANSI color if color coding is enabled.
        placeholderHandlers.put("mixLevelAndAreaColor", (env, _, context) -> {
            if (LoggerManager.getSettings().isEnableColorCoding() && context.color()) {
                return ColorTools.toAnsi(
                        ColorTools.mixColors(
                                ColorTools.randomColor(env.prefix()),
                                env.level().getColor()
                        )
                );
            } else {
                return "";
            }
        });
    }
}
