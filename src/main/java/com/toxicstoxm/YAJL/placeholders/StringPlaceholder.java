package com.toxicstoxm.YAJL.placeholders;

/**
 * Represents a placeholder that provides a dynamically computed string value.
 */
@FunctionalInterface
public interface StringPlaceholder {
    /**
     * Retrieves the computed string value of the placeholder.
     *
     * @return the placeholder's value as a {@code String}
     */
    String getData();
}
