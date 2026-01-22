package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.old.placeholders.PlaceholderHandler;
import com.toxicstoxm.YAJL.old.placeholders.StringPlaceholder;

import java.util.HashMap;
import java.util.Map;

public final class PlaceholderToken implements LayoutToken {

    private final String key;
    private final Map<String, String> staticArgs;

    public PlaceholderToken(String key, Map<String, String> staticArgs) {
        this.key = key;
        this.staticArgs = staticArgs;
    }

    @Override
    public void append(StringBuilder out, Map<String, StringPlaceholder> runtimeArgs) {
        PlaceholderHandler handler = Logger.placeholderHandlers.get(key);

        if (handler == null) {
            out.append('{').append(key).append('}');
            return;
        }

        Map<String, StringPlaceholder> args;

        if (!staticArgs.isEmpty()) {
            args = new HashMap<>(runtimeArgs);
            staticArgs.forEach((k, v) -> args.put(k, () -> v));
        } else {
            args = runtimeArgs;
        }

        out.append(handler.process(args));
    }
}
