package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJSI.api.settings.YAMLConfiguration;
import lombok.*;
import org.jetbrains.annotations.NotNull;

@Builder
@Getter
@Setter(onParam_ = @NotNull)
@YAMLConfiguration
@NoArgsConstructor
@AllArgsConstructor
public class YAJLManagerConfig {

    @Builder.Default
    private LogLevel defaultLogLevel = LogLevels.INFO;
    @Builder.Default
    private boolean enableColorCoding = true;
    @Builder.Default
    private boolean muteLogger = false;
    @Builder.Default
    private boolean enableYAMLConfig = true;
    @Builder.Default
    private boolean bridgeYAJSI = true;
    @Builder.Default
    private int stackTraceLengthLimit = 20;
    @Builder.Default
    private String logMessageLayout = "{color:hex=#545454}[{time:format=HH:mm:ss}] [{prefix}]{levelColor} [{level}]: {message}";
}
