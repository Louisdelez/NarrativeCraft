/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Main version adapter interface for abstracting MC version differences.
 * Implementations are loaded via ServiceLoader.
 *
 * Pure Java interface - no Minecraft dependencies.
 */
public interface IVersionAdapter {

    /**
     * @return The major MC version (e.g., "1.19", "1.20", "1.21")
     */
    String getMcMajor();

    /**
     * @return The exact MC version (e.g., "1.19.4", "1.20.6", "1.21.11")
     */
    String getMcVersion();

    /**
     * Check if a specific feature is supported in this MC version.
     *
     * @param featureId Feature identifier
     * @return true if supported
     */
    boolean supportsFeature(String featureId);

    /**
     * @return GUI rendering compatibility layer
     */
    IGuiRenderCompat getGuiRenderCompat();

    /**
     * @return Camera compatibility layer
     */
    ICameraCompat getCameraCompat();

    /**
     * @return Audio compatibility layer
     */
    IAudioCompat getAudioCompat();

    /**
     * @return Color/ARGB compatibility layer
     */
    IColorCompat getColorCompat();

    /**
     * @return Resource location compatibility layer for Identifier/ResourceLocation
     */
    IResourceCompat getResourceCompat();

    /**
     * @return Utility compatibility layer for version-specific utility methods
     */
    IUtilCompat getUtilCompat();

    /**
     * @return ID bridge for converting between NcId and MC-specific resource location types
     */
    IIdBridge getIdBridge();

    /**
     * @return Input compatibility layer for keyboard/mouse event handling
     */
    IInputCompat getInputCompat();

    /**
     * @return true if running on NeoForge, false for Fabric
     */
    boolean isNeoForge();

    /**
     * @return Required Java version for this MC version
     */
    int getRequiredJavaVersion();

    /**
     * Get a render type for the specified channel.
     * The returned Object is a MC-version-specific RenderType that callers
     * must cast appropriately in MC-dependent code.
     *
     * @param channel The render channel to get
     * @return The RenderType object for the channel (cast in caller)
     */
    Object getRenderType(RenderChannel channel);
}
