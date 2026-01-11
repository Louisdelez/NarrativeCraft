/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

import java.util.ServiceLoader;

/**
 * Utility class for loading version adapters via ServiceLoader.
 * Provides a cached singleton instance of the IVersionAdapter for the current MC version.
 */
public final class VersionAdapterLoader {

    private static volatile IVersionAdapter cachedAdapter;

    private VersionAdapterLoader() {
        // Utility class
    }

    /**
     * Gets the version adapter for the current Minecraft version.
     * Uses ServiceLoader to discover and load implementations.
     * The result is cached for subsequent calls.
     *
     * @return The IVersionAdapter implementation
     * @throws IllegalStateException if no adapter is found
     */
    public static IVersionAdapter getAdapter() {
        if (cachedAdapter == null) {
            synchronized (VersionAdapterLoader.class) {
                if (cachedAdapter == null) {
                    ServiceLoader<IVersionAdapter> loader = ServiceLoader.load(IVersionAdapter.class);
                    cachedAdapter = loader.findFirst()
                            .orElseThrow(() -> new IllegalStateException(
                                    "No IVersionAdapter implementation found! " +
                                    "Ensure a compat module (e.g., compat-mc121x or compat-mc120x) is on the classpath."));
                }
            }
        }
        return cachedAdapter;
    }

    /**
     * Clears the cached adapter. Useful for testing.
     */
    public static void clearCache() {
        synchronized (VersionAdapterLoader.class) {
            cachedAdapter = null;
        }
    }
}
