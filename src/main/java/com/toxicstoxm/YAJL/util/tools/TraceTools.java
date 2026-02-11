package com.toxicstoxm.YAJL.util.tools;

import org.jetbrains.annotations.NotNull;

import java.lang.StackWalker.Option;
import java.util.Set;

public final class TraceTools {
    public record CallerInfo(
            String className,
            String simpleClassName,
            String methodName,
            int lineNumber
    ) {}

    private static final StackWalker WALKER =
            StackWalker.getInstance(Set.of(Option.RETAIN_CLASS_REFERENCE));

    private static final String[] IGNORED_PREFIXES = {
            "com.toxicstoxm.YAJL",
            "java.",
            "sun.",
            "jdk.",
            "org.junit.",
            "org.gradle.",
            "org.opentest4j."
    };

    public static String[] ignoredPrefixes() {
        return IGNORED_PREFIXES;
    }

    private static boolean isIgnored(String className) {
        for (String prefix : ignoredPrefixes()) {
            if (className.startsWith(prefix)) return true;
        }
        return false;
    }

    public static @NotNull CallerInfo getCaller() {
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
                        }).orElse(new CallerInfo("Unknown", "Unknown", "Unknown", -1))
        );
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
