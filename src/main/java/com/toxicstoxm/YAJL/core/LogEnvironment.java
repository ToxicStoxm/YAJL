package com.toxicstoxm.YAJL.core;

import com.toxicstoxm.YAJL.core.level.LogLevel;

public record LogEnvironment(
        LogLevel level,
        String message,
        String prefix,
        Lazy<CallerInfo> callerInfo
) {}
