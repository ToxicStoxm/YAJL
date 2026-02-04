package com.toxicstoxm.YAJL.core;

public record CallerInfo(
        String className,
        String simpleClassName,
        String methodName,
        int lineNumber
) {}
