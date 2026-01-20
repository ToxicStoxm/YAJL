package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJSI.ConfigType;
import com.toxicstoxm.YAJSI.SettingsBundle;
import com.toxicstoxm.YAJSI.YAMLSetting;
import com.toxicstoxm.YAJSI.upgrading.ConfigVersion;

import java.io.File;

public class LoggerConfigBundle extends SettingsBundle {
    public LoggerConfigBundle(File f) {
        super(new ConfigVersion(1, 0, 0), f, ConfigType.READONLY);
    }

    @YAMLSetting(name = "Logger")
    LoggerConfig loggerConfig;
}
