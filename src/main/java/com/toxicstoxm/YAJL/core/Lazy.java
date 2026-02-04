package com.toxicstoxm.YAJL.core;

import java.util.function.Supplier;

public final class Lazy<T> {
    private T value;
    private boolean resolved;
    private final Supplier<T> supplier;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (!resolved) {
            value = supplier.get();
            resolved = true;
        }
        return value;
    }
}
