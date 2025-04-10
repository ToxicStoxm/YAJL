package com.toxicstoxm.YAJL;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Log filter which can be used to filter the loggers based on their prefix / area.
 */
public class LogFilter {
    private final List<Pattern> logAreaPatterns;

    @Builder
    public LogFilter(@NotNull List<String> logAreaFilters) {
        this.logAreaPatterns = logAreaFilters.stream()
                .map(LogFilter::convertWildcardToRegex)
                .map(Pattern::compile)
                .collect(Collectors.toList());
    }

    public void addFilterPattern(String pattern) {
        logAreaPatterns.add(
                Pattern.compile(convertWildcardToRegex(pattern))
        );
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
    public boolean isLogAreaAllowed(String logArea) {
        boolean isAllowed = logAreaPatterns.stream().anyMatch(pattern -> pattern.matcher(logArea).matches());
        return YAJLManager.getInstance().config.getLogAreaFilterConfig().isFilterPatternsAsBlacklist() != isAllowed;
    }
}
