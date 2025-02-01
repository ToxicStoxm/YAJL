package com.toxicstoxm.YAJL.config;

import com.toxicstoxm.YAJSI.api.settings.YAMLSetting;
import lombok.*;
import org.jetbrains.annotations.NotNull;

@Builder
@Getter
@Setter(onParam_ = @NotNull)
@NoArgsConstructor
@AllArgsConstructor
public class LogFileConfig {

    @Builder.Default
    @YAMLSetting(name = "Enable", comments = {
            "If true, logs will be written to files according to the settings below.",
            "If false, logging will be disabled entirely."
    })
    private boolean enable = false;

    @Builder.Default
    @YAMLSetting(name = "Limitation-Mode", comments = {
            "Defines how log file storage is limited.",
            "Supported modes:",
            " - none  : No limit, logs are never deleted.",
            " - files : Limits the number of log files. When exceeded, oldest files are deleted."
    })
    private String limitationMode = "files";

    @Builder.Default
    @YAMLSetting(name = "Limitation-Data", comments = {
            "Defines the limit based on the selected 'Limitation-Mode':",
            " - If mode is 'files', this sets the maximum number of log files before rotation occurs."
    })
    private int limitationNumber = 5;

    @Builder.Default
    @YAMLSetting(name = "Compress-Old-Log-Files", comments = {
            "If true, old log files are automatically compressed (.zip or .gz) to save space.",
            "Only applies when logs are rotated (i.e., when limitation mode is 'size' or 'files')."
    })
    private boolean compressOldLogFiles = true;

    @Builder.Default
    @YAMLSetting(name = "Log-Directory", comments = {
            "Specifies the directory where log files will be stored.",
            "Default: 'logs' (relative to application directory)."
    })
    private String logDirectory = "logs";

    @Builder.Default
    @YAMLSetting(name = "Log-File-Name", comments = {
            "Specifies the base name pattern for log files.",
            "The {date} placeholder is required to generate unique log file names based on the current date.",
            "If the {date} placeholder is omitted, only two log file names are possible: the specified base name and a compressed version of that base name (only if compression is enabled).",
            "Omitting the {date} placeholder will effectively suppress the file limit, as there will only ever be these two log files (one uncompressed and one compressed, if compression is enabled),",
            "which means the file count or size limit may never be reached."
    })
    private String logFileName = "log_{date}";

}
