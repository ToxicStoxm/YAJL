package com.toxicstoxm.YAJL;

import java.util.List;

public interface YAJLManagerSettings {

    void setDefaultLogLevel(LogLevel logLevel);

    void setEnableColorCoding(boolean enableColorCoding);

    void setMuteLogger(boolean muteLogger);

    void setEnableYAMLConfig(boolean enableYAMLConfig);

    void setBridgeYAJSI(boolean bridgeYAJSI);

    void setLogAreaFilterPatterns(List<String> logAreaFilterPatterns);

    void addLogAreaFilterPattern(String logAreaFilterPattern);

    void addLogAreaFilterPatterns(String... logAreaFilterPatterns);

}
