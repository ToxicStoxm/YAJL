package com.toxicstoxm.YAJL;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

public final class TextToken implements LayoutToken {

    private final String text;

    public TextToken(String text) {
        this.text = text;
    }

    @Override
    public void append(@NotNull StringBuilder out, Map<String, Supplier<String>> logEnvironment) {
        out.append(text);
    }
}
