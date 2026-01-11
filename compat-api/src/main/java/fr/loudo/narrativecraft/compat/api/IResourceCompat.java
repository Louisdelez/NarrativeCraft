/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Resource location compatibility interface for abstracting Identifier differences.
 * MC 1.21.x Yarn uses net.minecraft.resources.Identifier,
 * MC 1.20.x Yarn uses net.minecraft.util.Identifier.
 *
 * Pure Java interface - no Minecraft dependencies.
 */
public interface IResourceCompat {

    /**
     * Create a resource location from namespace and path.
     * Equivalent to Identifier.fromNamespaceAndPath() or ResourceLocation.of()
     *
     * @param namespace The namespace (e.g., "narrativecraft", "minecraft")
     * @param path      The path (e.g., "textures/logo.png")
     * @return The resource location object (version-specific)
     */
    Object create(String namespace, String path);

    /**
     * Create a resource location from a combined string.
     * Equivalent to Identifier.of() or ResourceLocation.parse()
     *
     * @param location The full location string (e.g., "narrativecraft:textures/logo.png")
     * @return The resource location object (version-specific)
     */
    Object parse(String location);

    /**
     * Get the namespace from a resource location.
     *
     * @param resourceLocation The resource location object
     * @return The namespace string
     */
    String getNamespace(Object resourceLocation);

    /**
     * Get the path from a resource location.
     *
     * @param resourceLocation The resource location object
     * @return The path string
     */
    String getPath(Object resourceLocation);
}
