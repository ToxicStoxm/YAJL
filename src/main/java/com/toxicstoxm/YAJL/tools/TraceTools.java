package com.toxicstoxm.YAJL.tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for retrieving caller information from the stack trace.
 * <p>
 * This class helps in identifying the method, class, and line number of the
 * caller, while filtering out internal logger calls and Java system classes.
 * </p>
 */
public class TraceTools {

    private static final String LOGGER_PACKAGE = "com.toxicstoxm.YAJL";  // Adjust based on your package

    /**
     * Retrieves the first non-logger stack trace element, filtering out internal
     * logger and system calls.
     *
     * @return The first valid {@code StackTraceElement} or {@code null} if none is found.
     */
    private static @Nullable StackTraceElement getCallerTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();

            if (!className.startsWith("java.") &&
                    !className.startsWith("sun.") &&
                    !className.startsWith(LOGGER_PACKAGE)) {
                return element;  // Found the first valid caller
            }
        }
        return null;  // No valid caller found
    }

    /**
     * Retrieves only the line number of the caller.
     *
     * @return The caller's line number, or {@code -1} if unavailable.
     */
    public static int getCallerLineNumber() {
        StackTraceElement element = getCallerTrace();
        return (element != null) ? element.getLineNumber() : -1;
    }

    /**
     * Retrieves only the method name of the caller.
     *
     * @return The caller's method name, or {@code "Unknown"} if unavailable.
     */
    public static @NotNull String getCallerMethodName() {
        StackTraceElement element = getCallerTrace();
        return (element != null) ? element.getMethodName() : "Unknown";
    }

    /**
     * Retrieves only the simple class name of the caller.
     *
     * @return The caller's class name without package prefix, or {@code "Unknown"} if unavailable.
     */
    public static @NotNull String getCallerClassName() {
        StackTraceElement element = getCallerTrace();
        if (element != null) {
            String className = element.getClassName();
            return className.substring(className.lastIndexOf('.') + 1);  // Extract simple class name
        }
        return "Unknown";
    }

    /**
     * Retrieves the caller's trace in a customizable format.
     * <p>
     * Allows including the class name, method name, and line number in the trace output.
     * </p>
     *
     * @param includeClass  Whether to include the class name.
     * @param includeMethod Whether to include the method name.
     * @param includeLine   Whether to include the line number.
     * @return A formatted string containing the requested caller trace information.
     */
    public static @NotNull String getCallerTraceFormatted(boolean includeClass, boolean includeMethod, boolean includeLine) {
        StackTraceElement element = getCallerTrace();
        if (element == null) {
            return "Unknown";
        }

        StringBuilder trace = new StringBuilder();
        if (includeClass) {
            trace.append(getCallerClassName());
        }
        if (includeMethod) {
            if (!trace.isEmpty()) trace.append(".");
            trace.append(getCallerMethodName());
        }
        if (includeLine) {
            if (!trace.isEmpty()) trace.append(":");
            trace.append(getCallerLineNumber());
        }

        return trace.toString();
    }
}
