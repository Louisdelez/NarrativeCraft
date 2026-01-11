/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

import java.util.Objects;

/**
 * Version-agnostic resource identifier.
 * Pure Java type that replaces Identifier (1.21.x) / ResourceLocation (1.20.x).
 *
 * Use IIdBridge to convert to/from the actual MC type.
 */
public final class NcId {

    private final String namespace;
    private final String path;

    public NcId(String namespace, String path) {
        this.namespace = Objects.requireNonNull(namespace, "namespace cannot be null");
        this.path = Objects.requireNonNull(path, "path cannot be null");
    }

    /**
     * Parse a resource location string (e.g., "narrativecraft:textures/logo.png").
     * If no namespace is provided, defaults to "minecraft".
     */
    public static NcId parse(String location) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("location cannot be null or empty");
        }

        int colonIndex = location.indexOf(':');
        if (colonIndex == -1) {
            return new NcId("minecraft", location);
        }

        String namespace = location.substring(0, colonIndex);
        String path = location.substring(colonIndex + 1);
        return new NcId(namespace, path);
    }

    /**
     * Create a NcId with the given namespace and path.
     */
    public static NcId of(String namespace, String path) {
        return new NcId(namespace, path);
    }

    public String namespace() {
        return namespace;
    }

    public String path() {
        return path;
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NcId ncId = (NcId) obj;
        return namespace.equals(ncId.namespace) && path.equals(ncId.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }
}
