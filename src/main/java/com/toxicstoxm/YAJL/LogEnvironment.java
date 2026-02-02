package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.level.LogLevel;

import java.util.function.Supplier;

public record LogEnvironment(
        LogLevel level,
        String message,
        String prefix,
        Supplier<String> trace,
        Supplier<String> traceClass,
        Supplier<String> traceMethod,
        Supplier<String> traceLineNumber
) {}
