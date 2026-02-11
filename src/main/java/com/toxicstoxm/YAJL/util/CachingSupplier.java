package com.toxicstoxm.YAJL.util;

import java.util.function.Supplier;

public final class CachingSupplier<T> implements Supplier<T> {
    private T value;
    private boolean resolved;
    private final Supplier<T> supplier;

    public CachingSupplier(Supplier<T> supplier) {
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
