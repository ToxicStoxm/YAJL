package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJSI.api.settings.SettingsManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
        if (config.isEnableYAMLConfig()) {
            SettingsManager.getInstance().registerYAMLConfiguration(config);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (!config.isMuteLogger()) {
                    System.out.println("[YAJL] Processing shutdown handles");
                }
                SettingsManager.getInstance().save();
                if (!config.isMuteLogger()) {
                    System.out.println("[YAJL] Shutting down, Goodbye!");
                }
            }));
        }

        setBridgeYAJSI(config.isBridgeYAJSI());
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
        if (bridgeYAJSI) {
            SettingsManager.getInstance().configure(
                    SettingsManager.SettingsManagerConfig.builder()
                            .logger(Logger.builder().logPrefix("YAJSI").build())
                            .build()
            );
        }
    }
}
