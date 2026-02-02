package com.toxicstoxm.YAJL;

public interface LayoutToken {
    void append(
            StringBuilder out,
            LogEnvironment logEnv,
            RenderContext context
    );
}

