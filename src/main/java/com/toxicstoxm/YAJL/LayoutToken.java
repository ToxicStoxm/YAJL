package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.old.placeholders.StringPlaceholder;

import java.util.Map;

public interface LayoutToken {
    void append(
            StringBuilder out,
            Map<String, StringPlaceholder> runtimeArgs
    );
}

