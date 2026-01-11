/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Bridge interface for converting between NcId and the MC-specific resource location type.
 *
 * In MC 1.21.x: converts to/from net.minecraft.resources.Identifier
 * In MC 1.20.x: converts to/from net.minecraft.resources.ResourceLocation
 */
public interface IIdBridge {

    /**
     * Convert a NcId to the MC-specific resource location type.
     *
     * @param ncId The version-agnostic identifier
     * @return The MC-specific type (Identifier or ResourceLocation)
     */
    Object toMc(NcId ncId);

    /**
     * Convert a MC-specific resource location to NcId.
     *
     * @param mcId The MC-specific identifier (Identifier or ResourceLocation)
     * @return The version-agnostic NcId
     */
    NcId fromMc(Object mcId);

    /**
     * Create a MC-specific resource location directly from namespace and path.
     * Convenience method that combines NcId.of() and toMc().
     *
     * @param namespace The namespace (e.g., "narrativecraft")
     * @param path The path (e.g., "textures/logo.png")
     * @return The MC-specific type
     */
    default Object create(String namespace, String path) {
        return toMc(NcId.of(namespace, path));
    }

    /**
     * Parse a location string and return the MC-specific type.
     *
     * @param location The full location string (e.g., "narrativecraft:textures/logo.png")
     * @return The MC-specific type
     */
    default Object parse(String location) {
        return toMc(NcId.parse(location));
    }
}
