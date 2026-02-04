package com.toxicstoxm.YAJL.core.tools;

import com.toxicstoxm.YAJL.core.CallerInfo;
import org.jetbrains.annotations.NotNull;

import java.lang.StackWalker.Option;
import java.util.Set;

public final class TraceTools {
    private static final StackWalker WALKER =
            StackWalker.getInstance(Set.of(Option.RETAIN_CLASS_REFERENCE));

    private static final String[] IGNORED_PREFIXES = {
            "com.toxicstoxm.YAJL.core",
            "java.",
            "sun.",
            "jdk.",
            "org.junit.",
            "org.gradle.",
            "org.opentest4j."
    };

    private static boolean isIgnored(String className) {
        for (String prefix : IGNORED_PREFIXES) {
            if (className.startsWith(prefix)) return true;
        }
        return false;
    }

    public static CallerInfo getCaller() {
        return WALKER.walk(stream ->
                stream
                        .filter(f -> !isIgnored(f.getClassName()))
                        .findFirst()
                        .map(f -> {
                            String className = f.getClassName();
                            int idx = className.lastIndexOf('.');
                            String simple = (idx >= 0)
                                    ? className.substring(idx + 1)
                                    : className;

                            return new CallerInfo(
                                    className,
                                    simple,
                                    f.getMethodName(),
                                    f.getLineNumber()
                            );
                        })
                        .orElse(null)
        );
    }

    public static int getCallerLineNumber() {
        CallerInfo c = getCaller();
        return c != null ? c.lineNumber() : -1;
    }

    public static String getCallerMethodName() {
        CallerInfo c = getCaller();
        return c != null ? c.methodName() : "Unknown";
    }

    public static String getCallerClassName() {
        CallerInfo c = getCaller();
        return c != null ? c.simpleClassName() : "Unknown";
    }

    public static @NotNull String formatCaller(CallerInfo c, boolean cls, boolean method, boolean line, String separator) {
        if (c == null) return "Unknown";

        StringBuilder sb = new StringBuilder(32);

        if (cls) sb.append(c.simpleClassName());
        if (method) {
            if (!sb.isEmpty()) sb.append(separator);
            sb.append(c.methodName());
        }
        if (line) {
            if (!sb.isEmpty()) sb.append(separator);
            sb.append(c.lineNumber());
        }

        return sb.toString();
    }

    public static @NotNull String formatCallerOrdered(
            CallerInfo c,
            @NotNull Iterable<String> fields,
            @NotNull String separator
    ) {
        if (c == null) return "Unknown";

        StringBuilder sb = new StringBuilder(32);

        for (String field : fields) {
            if (!sb.isEmpty()) sb.append(separator);

            switch (field) {
                case "class"  -> sb.append(c.simpleClassName());
                case "method" -> sb.append(c.methodName());
                case "line"   -> sb.append(c.lineNumber());
            }
        }

        return sb.toString();
    }

}
