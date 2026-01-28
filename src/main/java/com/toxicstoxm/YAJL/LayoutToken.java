package com.toxicstoxm.YAJL;

import java.util.Map;
import java.util.function.Supplier;

public interface LayoutToken {
    void append(
            StringBuilder out,
            Map<String, Supplier<String>> logEnvironment
    );
}

