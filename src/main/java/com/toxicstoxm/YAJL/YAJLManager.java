package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJSI.api.settings.SettingsManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class YAJLManager implements YAJLManagerSettings {

    protected YAJLManagerConfig config = YAJLManagerConfig.builder().build();

    private YAJLManager() {}
    
    private static YAJLManager instance = null;

    public static YAJLManager getInstance() {
        if (instance == null) {
            instance = new YAJLManager();
            instance.init();
        }
        return instance;
    }
    
    private void init() {
        if (config.isBridgeYAJSI()) {
            SettingsManager.getInstance().configure(
                    SettingsManager.SettingsManagerConfig.builder()
                            .logger(Logger.builder().logPrefix("YAJSI").build())
                            .build()
            );
        }

        if (config.isEnableYAMLConfig()) {
            SettingsManager.getInstance().registerYAMLConfiguration(config);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                SettingsManager.getInstance().save();
                if (!config.isMuteLogger()) {
                    System.out.println("[YAJL] Shutting down!");
                }
            }));
        }
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull YAJLManagerSettings configure() {
        return getInstance();
    }

    public void setDefaultLogLevel(LogLevel defaultLogLevel) {
        config.setDefaultLogLevel(defaultLogLevel);
    }

    public void setEnableColorCoding(boolean enableColorCoding) {
        config.setEnableYAMLConfig(enableColorCoding);
    }

    public void setMuteLogger(boolean muteLogger) {
        config.setMuteLogger(muteLogger);
    }

    public void setEnableYAMLConfig(boolean enableYAMLConfig) {
        config.setEnableYAMLConfig(enableYAMLConfig);
    }

    public void setBridgeYAJSI(boolean bridgeYAJSI) {
        config.setBridgeYAJSI(bridgeYAJSI);
    }
}
