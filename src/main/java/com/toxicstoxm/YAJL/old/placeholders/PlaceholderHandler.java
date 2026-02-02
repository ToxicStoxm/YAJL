package com.toxicstoxm.YAJL.old.placeholders;

import com.toxicstoxm.YAJL.LogEnvironment;

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
     * @param args a map of additional placeholder properties
     * @return the processed placeholder value as a {@code String}
     */
    String process(LogEnvironment env, Map<String, String> args);
}

