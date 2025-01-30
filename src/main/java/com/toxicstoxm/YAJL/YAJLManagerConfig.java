package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJSI.api.settings.YAMLConfiguration;
import com.toxicstoxm.YAJSI.api.settings.YAMLSetting;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Builder
@Getter
@Setter(onParam_ = @NotNull)
@YAMLConfiguration
@NoArgsConstructor
@AllArgsConstructor
public class YAJLManagerConfig {

    @Builder.Default
    @YAMLSetting.Ignore
    private LogLevel defaultLogLevel = LogLevels.INFO;
    @Builder.Default
    @YAMLSetting(name = "Enable-Color-Coding", comments = "Should the log be color coded?")
    private boolean enableColorCoding = true;
    @Builder.Default
    @YAMLSetting(name = "Mute-Logger", comments = "This mutes the logger entirely and prohibits all output")
    private boolean muteLogger = false;
    @Builder.Default
    @YAMLSetting(name = "Enable-YAML-Config", comments = "If false, this config file will be ignored!")
    private boolean enableYAMLConfig = true;
    @Builder.Default
    @YAMLSetting(name = "Bridge-YAJSI", comments = "If true, YAJL will automatically provide YAJSI with a logger instance.")
    private boolean bridgeYAJSI = true;
    @Builder.Default
    @YAMLSetting(name = "Stacktrace-Length-Limit", comments = "Set a limit to how many lines of a stacktrace are printed.")
    private int stackTraceLengthLimit = 20;
    @Builder.Default
    @YAMLSetting(name = "Log-Message-Layout", comments = {"This message layout will be used for constructing log messages.",
            "Supported placeholders are:",
            " - color, usage example: {color:hex=#FFFFFF}",
            " - time, usage example: {time:format=HH:mm:ss}",
            " - prefix, by default this will display the class this logger instance was initialized in, but can be customized.",
            " - levelColor, this is the predefined color of this messages log level",
            " - level, the messages log level, example: DEBUG, INFO, ERROR, etc.",
            " - message, the actual log message",
            " - trace, mainly for debugging, can be customized to display the class, method and even line number this log message comes from,",
            "   usage example: {trace:line,class,method}, result -> lineNumber:className:methodName",
            "   per default ':' is used as separator but it can be customized by adding separator=someString to the parameters like this: {trace:line,class,method, separator=-}",
            "   this will result in a trace like this: lineNumber-className-methodName"
    })
    private String logMessageLayout = "{color:hex=#545454}[{time:format=HH:mm:ss}] [{prefix}]{levelColor} [{level}]: {message}";
    @Builder.Default
    @YAMLSetting(name = "Log-Area-Filter-Patterns", comments = {"Per default every logger instance is enabled. You can filter log instances by specifying which ones should be activated.",
            "Using absolute identifier (default: classpath, for example com.example.Main): this means providing the exact identifier of the logger you want to enable.",
            "Using wildcard character '*': If you want to enable all loggers whose identifier starts with 'com.' just use 'com.*', the * is a wildcard and accepts anything.",
            "Using regex: You can use advanced regex in the format of '^<regex>$' to specify which loggers should be activated."
    })
    private List<String> logAreaFilterPatterns = List.of("*");

    @Builder.Default
    @YAMLSetting.Ignore
    private LogFilter logFilter = new LogFilter(List.of("*"));
}
