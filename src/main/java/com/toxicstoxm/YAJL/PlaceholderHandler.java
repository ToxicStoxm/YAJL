package com.toxicstoxm.YAJL;

import java.util.Map;

@FunctionalInterface
public interface PlaceholderHandler {
    String process(Map<String, String> args);
}

