package com.toxicstoxm.YAJL.core;

import com.toxicstoxm.YAJL.core.level.LogLevel;
import com.toxicstoxm.YAJL.util.CachingSupplier;
import com.toxicstoxm.YAJL.util.tools.TraceTools;

public record LogEnvironment(
        LogLevel level,
        String message,
        String prefix,
        CachingSupplier<TraceTools.CallerInfo> callerInfo
) {}
