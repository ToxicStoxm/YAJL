package com.toxicstoxm.YAJL;

import com.toxicstoxm.YAJL.old.placeholders.StringPlaceholder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class TextToken implements LayoutToken {

    private final String text;

    public TextToken(String text) {
        this.text = text;
    }

    @Override
    public void append(@NotNull StringBuilder out, Map<String, StringPlaceholder> runtimeArgs) {
        out.append(text);
    }
}
