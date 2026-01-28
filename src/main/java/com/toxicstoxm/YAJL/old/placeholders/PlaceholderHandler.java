package com.toxicstoxm.YAJL.old.placeholders;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Handles a specific placeholder and returns the corresponding replacement value
 * based on the given context.
 */
@FunctionalInterface
public interface PlaceholderHandler {
    /**
     * Processes the placeholder using the provided arguments and returns its replacement value.
     *
     * @param logEnvironment a map of additional placeholder properties
     * @return the processed placeholder value as a {@code String}
     */
    String process(Map<String, Supplier<String>> logEnvironment);
}

