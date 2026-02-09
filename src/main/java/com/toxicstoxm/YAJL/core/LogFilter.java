package com.toxicstoxm.YAJL.core;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Log filter which can be used to filter the loggers based on their prefix / area.
 */
public class LogFilter {
    private final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();
    private final List<Pattern> logAreaPatterns = new ArrayList<>();
    private final boolean blacklist;

    @Builder
    public LogFilter(@NotNull List<String> logAreaFilters, boolean blacklist) {
        this.blacklist = blacklist;
        this.logAreaPatterns.addAll(logAreaFilters.stream()
                .map(LogFilter::convertWildcardToRegex)
                .map(Pattern::compile)
                .toList());
    }

    /**
     * Converts user wildcard patterns into regex.
     */
    private static @NotNull String convertWildcardToRegex(@NotNull String wildcardPattern) {
        return "^" + wildcardPattern.replace(".", "\\.").replace("*", ".*") + "$";
    }

    /**
     * Checks if a given log area is allowed based on filters.
     */
    public boolean isFiltered(String logArea) {
        boolean isMatch = cache.computeIfAbsent(
                logArea,
                area -> logAreaPatterns.stream().anyMatch(p -> p.matcher(area).matches())
        );

        return isMatch == blacklist;
    }
}
