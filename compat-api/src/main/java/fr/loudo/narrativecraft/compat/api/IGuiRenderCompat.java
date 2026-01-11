/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * GUI rendering compatibility layer for version-specific graphics APIs.
 *
 * Pure Java interface - uses Object for MC-specific types.
 * Implementations cast to the appropriate MC version types.
 *
 * The 'graphics' parameter is the version-specific graphics context:
 * - 1.19.x: MatrixStack + BufferSource (wrapped)
 * - 1.20.x Fabric: DrawContext
 * - 1.20.x+ NeoForge: GuiGraphics
 */
public interface IGuiRenderCompat {

    // ===== Basic Shapes =====

    /**
     * Fill a rectangle with a solid color.
     * Used by: many UI screens for backgrounds
     */
    void fill(Object graphics, int x1, int y1, int x2, int y2, int color);

    /**
     * Fill a rectangle with a gradient.
     * Used by: background overlays
     */
    void fillGradient(Object graphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo);

    /**
     * Fill a rectangle with float coordinates and solid color.
     * Used by: dialog backgrounds, smooth animations
     */
    void fillFloat(Object graphics, float x1, float y1, float x2, float y2, int color);

    // ===== Text Rendering =====
    // font parameter is net.minecraft.client.gui.Font

    /**
     * Draw text with shadow at integer position.
     * Used by: standard UI text
     */
    void drawString(Object graphics, Object font, String text, int x, int y, int color);

    /**
     * Draw text without shadow at integer position.
     * Used by: clean text rendering
     */
    void drawStringNoShadow(Object graphics, Object font, String text, int x, int y, int color);

    /**
     * Draw centered text with shadow.
     * Used by: titles, centered labels
     */
    void drawCenteredString(Object graphics, Object font, String text, int centerX, int y, int color);

    /**
     * Draw text at float position (for smooth animations).
     * Used by: dialog scroll text, waving effects
     */
    void drawStringFloat(Object graphics, Object font, String text, float x, float y, int color, boolean shadow);

    // ===== Scissoring =====

    void enableScissor(Object graphics, int x1, int y1, int x2, int y2);

    void disableScissor(Object graphics);

    // ===== Texture/Sprite Rendering =====

    /**
     * Blit a texture region (version-agnostic).
     * In 1.21.x: uses RenderPipelines.GUI_TEXTURED internally
     * In 1.20.x: uses standard blit
     *
     * @param graphics Version-specific graphics context
     * @param textureId Texture path as String (e.g., "minecraft:textures/gui/...")
     * @param x Destination X
     * @param y Destination Y
     * @param u Texture U coordinate
     * @param v Texture V coordinate
     * @param width Width to draw
     * @param height Height to draw
     * @param textureWidth Full texture width
     * @param textureHeight Full texture height
     */
    void blitTexture(Object graphics, String textureId, int x, int y, float u, float v,
                     int width, int height, int textureWidth, int textureHeight);

    // ===== Matrix Stack Access =====

    void pushPose(Object graphics);

    void popPose(Object graphics);

    void translate(Object graphics, double x, double y, double z);

    void scale(Object graphics, float x, float y, float z);

    // ===== Dialog-specific rendering =====

    /**
     * Draw the dialog skip arrow indicator.
     * Used by: DialogArrowSkip for showing "press to skip"
     *
     * @param graphics Graphics context
     * @param centerX Center X position
     * @param centerY Center Y position
     * @param width Arrow width
     * @param height Arrow height
     * @param color ARGB color
     */
    void drawDialogSkipArrow(Object graphics, float centerX, float centerY, float width, float height, int color);

    /**
     * Check if custom render pipelines are supported.
     * Returns false on 1.20.x, true on 1.21.x
     */
    default boolean supportsCustomRenderPipelines() {
        return false;
    }

    /**
     * Log a warning when a feature is not available on this version.
     * Implementations should log once per feature to avoid spam.
     */
    default void warnUnsupportedFeature(String featureName) {
        // Default no-op, implementations should log appropriately
    }

    // ===== Color Utilities =====

    default int toARGB(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    default int toRGB(int argb) {
        return argb & 0xFFFFFF;
    }
}
