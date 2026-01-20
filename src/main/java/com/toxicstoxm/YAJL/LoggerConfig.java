package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJSI.YAMLSetting;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

@Getter
@Setter
@Builder(buildMethodName = "done", toBuilder = true)
public class LoggerConfig {
    @Contract(value = " -> new", pure = true)
    public static @NotNull LoggerConfig getDefaults() {
        return LoggerConfig.builder().done();
    }

    private int test;

    @Builder.Default
    @YAMLSetting.Ignore
    private PrintStream output = System.out;
}
