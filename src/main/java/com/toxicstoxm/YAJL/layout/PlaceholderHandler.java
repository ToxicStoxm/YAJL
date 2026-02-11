package com.toxicstoxm.YAJL.layout;

import com.toxicstoxm.YAJL.core.LogEnvironment;
import com.toxicstoxm.YAJL.core.RenderContext;

import java.util.Map;

/**
 * Handles a specific placeholder and returns the corresponding replacement value
 * based on the given context.
 */
@FunctionalInterface
public interface PlaceholderHandler {
    /**
     * Processes the placeholder using the provided arguments and returns its replacement value.
     *
     * @param staticArgs a map of additional placeholder properties
     * @return the processed placeholder value as a {@code String}
     */
    String process(LogEnvironment env, Map<String, String> staticArgs, RenderContext context);
}

