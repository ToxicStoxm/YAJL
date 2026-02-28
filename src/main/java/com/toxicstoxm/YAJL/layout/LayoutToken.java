package com.toxicstoxm.YAJL.layout;

import com.toxicstoxm.YAJL.core.LogEnvironment;
import com.toxicstoxm.YAJL.core.RenderContext;

public interface LayoutToken {
    void append(
            StringBuilder out,
            LogEnvironment logEnv,
            RenderContext context
    );
}

