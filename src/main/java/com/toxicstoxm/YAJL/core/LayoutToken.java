package com.toxicstoxm.YAJL.core;

public interface LayoutToken {
    void append(
            StringBuilder out,
            LogEnvironment logEnv,
            RenderContext context
    );
}

