package com.toxicstoxm.YAJL.core;

import com.toxicstoxm.YAJL.old.placeholders.PlaceholderHandler;

import java.util.Map;

public final class PlaceholderToken implements LayoutToken {
    private final String key;
    private final Map<String, String> staticArgs;

    public PlaceholderToken(String key, Map<String, String> staticArgs) {
        this.key = key;
        this.staticArgs = staticArgs;
    }

    @Override
    public void append(StringBuilder out, LogEnvironment env, RenderContext context
    ) {
        PlaceholderHandler handler = Logger.placeholderHandlers.get(key);

        if (handler == null) {
            out.append('{').append(key).append('}');
            return;
        }

        out.append(handler.process(env, staticArgs));
    }
}
