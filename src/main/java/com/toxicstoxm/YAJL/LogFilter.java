package com.toxicstoxm.YAJL;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Log filter which can be used to filter the loggers based on their prefix / area.
 */
public class LogFilter {
    private final HashMap<String, Boolean> cache = new HashMap<>();
    private final List<Pattern> logAreaPatterns;
    private final List<String> logAreaFilters = new ArrayList<>();

    @Builder
    public LogFilter(@NotNull List<String> logAreaFilters) {
        this.logAreaFilters.addAll(logAreaFilters);
        this.logAreaPatterns = logAreaFilters.stream()
                .map(LogFilter::convertWildcardToRegex)
                .map(Pattern::compile)
                .collect(Collectors.toList());
    }

    public void addFilterPattern(String pattern) {
        if (!logAreaFilters.contains(pattern)) {
            clearCache();
            logAreaPatterns.add(
                    Pattern.compile(convertWildcardToRegex(pattern))
            );
            logAreaFilters.add(pattern);
        }
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
        if (cache.containsKey(logArea)) {
            System.out.println("From cache!");
        }
        cache.computeIfAbsent(logArea, area -> logAreaPatterns.stream().anyMatch(pattern -> pattern.matcher(area).matches()));
        return cache.get(logArea) != LoggerManager.getSettings().isFilterPatternsAsBlacklist();
    }

    protected void clearCache() {
        cache.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LogFilter logFilter = (LogFilter) o;
        return Objects.equals(logAreaPatterns, logFilter.logAreaPatterns);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(logAreaPatterns);
    }
}
