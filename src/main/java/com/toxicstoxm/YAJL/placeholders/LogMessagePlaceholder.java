package com.toxicstoxm.YAJL.placeholders;

/**
 * A {@code LogMessagePlaceholder} ensures that placeholder values are only computed
 * if the log message is actually printed. This helps prevent unnecessary CPU usage.
 * <p>
 * This should be used when computing or retrieving a placeholder value is expensive.
 * </p>
 */
@FunctionalInterface
public interface LogMessagePlaceholder {
    /**
     * Computes or retrieves the placeholder value.
     *
     * @return the computed or retrieved object
     */
    Object getObject();
}
