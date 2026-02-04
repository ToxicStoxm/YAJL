package com.toxicstoxm.YAJL.core;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public final class CompiledLayout {
    @Getter
    private final String baseLayout;
    private final ParsedLayout parsedLayout;

    CompiledLayout(String baseLayout, ParsedLayout parsedLayout) {
        this.baseLayout = baseLayout;
        this.parsedLayout = parsedLayout;
    }

    public @Unmodifiable @NotNull List<LayoutToken> getTokens() {
        return parsedLayout.tokens();
    }

    public boolean isColored() {
        return parsedLayout.color();
    }
}
