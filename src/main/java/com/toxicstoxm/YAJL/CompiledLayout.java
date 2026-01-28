package com.toxicstoxm.YAJL;

import java.util.List;

public final class CompiledLayout {
    final String layout;
    final List<LayoutToken> tokens;

    CompiledLayout(String layout, List<LayoutToken> tokens) {
        this.layout = layout;
        this.tokens = tokens;
    }
}
