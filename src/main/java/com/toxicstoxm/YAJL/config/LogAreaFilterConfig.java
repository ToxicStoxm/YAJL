package com.toxicstoxm.YAJL.config;

import com.toxicstoxm.YAJL.LogFilter;
import com.toxicstoxm.YAJSI.api.settings.YAMLSetting;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * YAJL LogAreaFilter configuration class.
 *
 * @implNote YAJSI compatible
 */
@Builder
@Getter
@Setter(onParam_ = @NotNull)
@NoArgsConstructor
@AllArgsConstructor
public class LogAreaFilterConfig {

    @Builder.Default
    @YAMLSetting(name = "Log-Area-Filter-Patterns-As-Blacklist", comments = {
            "If set to true, the 'Log-Area-Filter-Patterns' list will be treated as a blacklist instead of a whitelist.",
            " - Whitelist mode: Only loggers matching the patterns are enabled.",
            " - Blacklist mode: Loggers matching the patterns are disabled, and all others remain enabled."
    })
    private boolean filterPatternsAsBlacklist = false;

    @Builder.Default
    @YAMLSetting(name = "Log-Area-Filter-Patterns", comments = {
            "Defines which logger instances are enabled based on their identifier.",
            "Supported filtering methods:",
            " - Exact match: 'com.example.Main' enables only that logger.",
            " - Wildcard (*) match: 'com.*' enables all loggers starting with 'com.'.",
            " - Regex match: Use '^<regex>$' for advanced pattern matching."
    })
    private List<String> logAreaFilterPatterns = List.of("*");

    @Builder.Default
    @YAMLSetting.Ignore
    private LogFilter logFilter = new LogFilter(List.of("*"));
}
