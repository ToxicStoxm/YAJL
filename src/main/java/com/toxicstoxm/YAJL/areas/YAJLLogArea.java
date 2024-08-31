package com.toxicstoxm.YAJL.areas;

import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class YAJLLogArea implements LogArea {
    private final String name;
    @Setter
    private Color color;
    @Setter
    private List<String> parents;
    @Setter
    private boolean enabled = false;

    public YAJLLogArea() {
        this.name = null;
        this.color = null;
        this.parents = null;
    }

    public YAJLLogArea(@NonNull String name) {
        this.name = name;
        this.color = null;
        this.parents = null;
    }

    public YAJLLogArea(@NonNull Color color) {
        this.name = null;
        this.color = color;
        this.parents = null;
    }

    public YAJLLogArea(@NonNull Collection<String> parents) {
        this.name = null;
        this.color = null;
        this.parents = parents.stream().toList();
    }

    public YAJLLogArea(@NonNull String name, @NonNull Color color) {
        this.name = name;
        this.color = color;
        this.parents = null;
    }

    public YAJLLogArea(@NonNull String name, @NonNull Collection<String> parents) {
        this.name = name;
        this.color = null;
        this.parents = parents.stream().toList();
    }

    public YAJLLogArea(@NonNull Color color, @NonNull Collection<String> parents) {
        this.name = null;
        this.color = color;
        this.parents = parents.stream().toList();
    }

    public YAJLLogArea(@NonNull String name, @NonNull Color color, @NonNull Collection<String> parents) {
        this.name = name;
        this.color = color;
        this.parents = parents.stream().toList();
    }

    public void addParent(String parent) {
        parents.add(parent);
    }
    public boolean isChildOf(@NonNull String parent) {
        return parents.contains(parent);
    }

    public boolean isChildOf(@NonNull Collection<String> parents) {
        return new HashSet<>(this.parents).containsAll(parents);
    }

    public boolean isChild() {
        return parents != null && !parents.isEmpty();
    }

    public boolean hasColor() {
        return color != null;
    }

    @Override
    public String getName() {
        return name == null ? LogArea.super.getName() : name;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public @Nullable List<String> getParents() {
        return parents;
    }

    @Override
    public Color getColor() {
        return color;
    }
}
