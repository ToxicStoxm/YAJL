package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.old.placeholders.PlaceholderHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class PlaceholderToken implements LayoutToken {

    private final String key;
    private final Map<String, String> staticArgs;

    public PlaceholderToken(String key, Map<String, String> staticArgs) {
        this.key = key;
        this.staticArgs = staticArgs;
    }

    @Override
    public void append(StringBuilder out, Map<String, Supplier<String>> logEnvironment
    ) {
        PlaceholderHandler handler = Logger.placeholderHandlers.get(key);

        if (handler == null) {
            out.append('{').append(key).append('}');
            return;
        }

        Map<String, Supplier<String>> args;

        if (!staticArgs.isEmpty()) {
            args = new HashMap<>(logEnvironment);
            staticArgs.forEach((k, v) -> args.put(k, () -> v));
        } else {
            args = logEnvironment;
        }

        out.append(handler.process(args));
    }
}
