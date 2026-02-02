package com.toxicstoxm.YAJL;

import org.jetbrains.annotations.NotNull;

public final class TextToken implements LayoutToken {

    private final String text;

    public TextToken(String text) {
        this.text = text;
    }

    @Override
    public void append(@NotNull StringBuilder out, LogEnvironment env, RenderContext context) {
        out.append(text);
    }
}
