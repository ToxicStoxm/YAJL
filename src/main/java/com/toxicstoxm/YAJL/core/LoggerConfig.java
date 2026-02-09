package com.toxicstoxm.YAJL.core;

import com.toxicstoxm.YAJL.core.level.LogLevel;
import com.toxicstoxm.YAJL.core.level.LogLevels;
import com.toxicstoxm.YAJSI.YAMLSetting;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.List;

@Getter
@Builder(buildMethodName = "done", toBuilder = true)
public class LoggerConfig {
    @Contract(value = " -> new", pure = true)
    public static @NotNull LoggerConfig getDefaults() {
        LoggerConfig conf = LoggerConfig.builder().done();
        conf.logFilter = new LogFilter(List.copyOf(conf.logAreaFilterPatterns), conf.filterPatternsAsBlacklist);
        return conf;
    }

    @Builder.Default
    @YAMLSetting.Ignore
    private List<PrintStream> outputs = List.of(System.out);

    @Builder.Default
    private boolean internalLog = true;

    @Builder.Default
    @YAMLSetting.Ignore
    private PrintStream internalLogOutput = System.err;

    @Builder.Default
    @YAMLSetting.Ignore
    private LogLevel defaultLogLevel = LogLevels.INFO;

    @Builder.Default
    @YAMLSetting(name = "Minimum-Log-Level", comments = {
            "Specifies the minimum log level to be recorded.",
            "Higher values reduce log verbosity, showing only critical messages (e.g., warnings, errors, and fatal logs).",
            "Lower values increase verbosity, including debug and verbose logs for detailed output.",
            "Available log levels and their values:",
            " - Fatal      =  3  (Only critical failures)",
            " - Error      =  2  (Serious issues that require attention)",
            " - Warning    =  1  (Potential issues that should be monitored)",
            " - Info       =  0  (General operational messages)",
            " - Debug      = -1  (Detailed debug information)",
            " - Verbose    = -2  (Highly detailed logs for troubleshooting)",
            " - Stacktrace = -3  (Includes full stack traces for debugging)",
            "Example: A value of -1 records Debug, Info, Warning, Error, and Fatal logs."
    })
    private int minimumLogLevel = 0;

    @Builder.Default
    @YAMLSetting(name = "Enable-Color-Coding", comments = {
            "If true, log messages will be color-coded for better readability."
    })
    private boolean enableColorCoding = true;

    @Builder.Default
    @YAMLSetting(name = "Mute-Logger", comments = {
            "If true, logging will be completely disabled.",
            "No log messages will be recorded or displayed."
    })
    private boolean muteLogger = false;

    @Builder.Default
    @YAMLSetting(name = "Stacktrace-Length-Limit", comments = {
            "Defines the maximum number of lines to be printed for a stack trace.",
            "A lower value keeps logs concise, while a higher value provides more debugging details."
    })
    private int stackTraceLengthLimit = 20;

    @Builder.Default
    @YAMLSetting(name = "Log-Message-Layout", comments = {
            "Defines the format for log messages using placeholders.",
            "Supported placeholders:",
            " - {color:hex=#FFFFFF}    : Custom text color (for console logs).",
            " - {time:format=HH:mm:ss} : Timestamp format.",
            " - {prefix}               : Logger instance prefix (defaults to class name).",
            " - {levelColor}           : Log level's predefined color.",
            " - {level}                : Log level name (e.g., DEBUG, INFO, ERROR).",
            " - {message}              : The actual log message.",
            " - {trace:line,class,method} : Displays source of the log (line number, class, method).",
            "     Example: {trace:line,class,method} → 42:MyClass:myMethod",
            "     You can customize the separator with: {trace:line,class,method,separator=-}",
            "     Example output: 42-MyClass-myMethod"
    })
    private String logMessageLayout = "{color:hex=#545454}[{time:format=HH:mm:ss}] [{prefix}]{levelColor} [{level}]: {message}";

    @Builder.Default
    @YAMLSetting(name = "Blacklist-Matches", comments = {
            "If set to true, the 'Log-Area-Filter-Patterns' list will be treated as a blacklist instead of a whitelist.",
            " - Whitelist mode: Only loggers matching the patterns are enabled.",
            " - Blacklist mode: Loggers matching the patterns are disabled, and all others remain enabled."
    })
    private boolean filterPatternsAsBlacklist = false;

    @Builder.Default
    @YAMLSetting(name = "Filter-Patterns", comments = {
            "Defines which logger instances are enabled based on their identifier.",
            "Supported filtering methods:",
            " - Exact match: 'com.example.Main' enables only that logger.",
            " - Wildcard (*) match: 'com.*' enables all loggers starting with 'com.'.",
            " - Regex match: Use '^<regex>$' for advanced pattern matching."
    })
    private List<String> logAreaFilterPatterns = List.of("*");

    @Builder.Default
    @YAMLSetting.Ignore
    private LogFilter logFilter = new LogFilter(List.of("*"), false);

    @Builder.Default
    @YAMLSetting(name = "EnableLogFiles", comments = {
            "If true, logs will be written to files according to the settings below.",
            "If false, logging will be disabled entirely."
    })
    private boolean enableLogFiles = false;

    @Builder.Default
    @YAMLSetting(name = "Log-File-Limit", comments = {
            "Defines how many log files are stored.",
            "When the number of log files exceeds this limit, the older files will be deleted."
            })
    private int logFileLimit = 5;

    @Builder.Default
    @YAMLSetting(name = "Compressed-File-Size-Limit", comments = {
            "If a logfile still exceeds this limit after compression, it will be deleted.",
            "This check is NOT done on already existing files!",
            "Value in Kilobytes"
    })
    private int compressedFileSizeLimit = 50;

    @Builder.Default
    @YAMLSetting(name = "Compress-Old-Log-Files", comments = {
            "If true, old log files are automatically compressed (.zip or .gz) to save space."
    })
    private boolean compressOldLogFiles = true;

    @Builder.Default
    @YAMLSetting(name = "Log-Directory", comments = {
            "Specifies the directory where log files will be stored."
    })
    private String logDirectory = "./";

    @Builder.Default
    @YAMLSetting(name = "Log-File-Name-Pattern", comments = {
            "Specifies the base name pattern for log files.",
            "The {date} placeholder is required to generate unique log file names based on the current date.",
            "If the {date} placeholder is omitted, only two log file names are possible: the specified base name and a compressed version of that base name (only if compression is enabled).",
            "Omitting the {date} placeholder will effectively suppress the file limit, as there will only ever be these two log files (one uncompressed and one compressed, if compression is enabled),",
            "which means the file count or size limit may never be reached."
    })
    private String logFileNamePattern = "log_{date}";
}
