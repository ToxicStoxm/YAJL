package com.toxicstoxm.YAJL.placeholders;

import java.util.Map;

@FunctionalInterface
public interface PlaceholderHandler {
    String process(Map<String, StringPlaceholder> args);
}

