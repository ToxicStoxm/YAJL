package com.toxicstoxm.YAJL.core;

import com.toxicstoxm.YAJSI.ConfigType;
import com.toxicstoxm.YAJSI.SettingsBundle;
import com.toxicstoxm.YAJSI.YAMLSetting;
import com.toxicstoxm.YAJSI.upgrading.ConfigVersion;
import lombok.Getter;

import java.io.File;

@Getter
public class LoggerConfigBundle extends SettingsBundle {
    public LoggerConfigBundle(File f) {
        super(new ConfigVersion(1, 0, 1), f, ConfigType.READONLY);
    }

    @YAMLSetting(name = "Logger")
    private LoggerConfig loggerConfig;
}
