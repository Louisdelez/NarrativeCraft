/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc120x;

import fr.loudo.narrativecraft.compat.api.IGuiRenderCompat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * GUI rendering compatibility implementation for Minecraft 1.20.x.
 * Uses the GuiGraphics API introduced in MC 1.20.
 * Note: Some 1.21.x features (custom render pipelines) are not available.
 */
public class Mc120xGuiRenderCompat implements IGuiRenderCompat {

    private static final Logger LOGGER = LoggerFactory.getLogger("NarrativeCraft");
    private final Set<String> warnedFeatures = new HashSet<>();

    @Override
    public void fill(Object graphics, int x1, int y1, int x2, int y2, int color) {
        ((GuiGraphics) graphics).fill(x1, y1, x2, y2, color);
    }

    @Override
    public void fillGradient(Object graphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        ((GuiGraphics) graphics).fillGradient(x1, y1, x2, y2, colorFrom, colorTo);
    }

    @Override
    public void fillFloat(Object graphics, float x1, float y1, float x2, float y2, int color) {
        // MC 1.20.x doesn't have float fill, convert to int
        ((GuiGraphics) graphics).fill((int) x1, (int) y1, (int) x2, (int) y2, color);
    }

    @Override
    public void drawString(Object graphics, Object font, String text, int x, int y, int color) {
        ((GuiGraphics) graphics).drawString((Font) font, text, x, y, color);
    }

    @Override
    public void drawStringNoShadow(Object graphics, Object font, String text, int x, int y, int color) {
        ((GuiGraphics) graphics).drawString((Font) font, text, x, y, color, false);
    }

    @Override
    public void drawCenteredString(Object graphics, Object font, String text, int centerX, int y, int color) {
        ((GuiGraphics) graphics).drawCenteredString((Font) font, text, centerX, y, color);
    }

    @Override
    public void drawStringFloat(Object graphics, Object font, String text, float x, float y, int color, boolean shadow) {
        // MC 1.20.x doesn't have native float positioning, convert to int
        ((GuiGraphics) graphics).drawString((Font) font, text, (int) x, (int) y, color, shadow);
    }

    @Override
    public void enableScissor(Object graphics, int x1, int y1, int x2, int y2) {
        ((GuiGraphics) graphics).enableScissor(x1, y1, x2, y2);
    }

    @Override
    public void disableScissor(Object graphics) {
        ((GuiGraphics) graphics).disableScissor();
    }

    @Override
    public void blitTexture(Object graphics, String textureId, int x, int y, float u, float v,
                            int width, int height, int textureWidth, int textureHeight) {
        GuiGraphics gui = (GuiGraphics) graphics;
        // MC 1.20.x uses new ResourceLocation() constructor
        ResourceLocation texture = new ResourceLocation(textureId);
        // MC 1.20.x blit signature: blit(ResourceLocation, x, y, u, v, width, height, textureWidth, textureHeight)
        gui.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    @Override
    public void pushPose(Object graphics) {
        // MC 1.20.x GuiGraphics uses PoseStack internally via pose()
        ((GuiGraphics) graphics).pose().pushPose();
    }

    @Override
    public void popPose(Object graphics) {
        ((GuiGraphics) graphics).pose().popPose();
    }

    @Override
    public void translate(Object graphics, double x, double y, double z) {
        ((GuiGraphics) graphics).pose().translate(x, y, z);
    }

    @Override
    public void scale(Object graphics, float x, float y, float z) {
        ((GuiGraphics) graphics).pose().scale(x, y, z);
    }

    @Override
    public void drawDialogSkipArrow(Object graphics, float centerX, float centerY, float width, float height, int color) {
        // Draw simple arrow using filled rectangles (no custom render pipeline in 1.20.x)
        GuiGraphics gui = (GuiGraphics) graphics;
        int cx = (int) centerX;
        int cy = (int) centerY;
        int w = (int) width;
        int h = (int) height;

        // Simple right-pointing arrow using rectangles
        gui.fill(cx - w, cy - h / 2, cx, cy + h / 2, color);
        gui.fill(cx, cy - h / 4, cx + w / 2, cy + h / 4, color);
    }

    @Override
    public boolean supportsCustomRenderPipelines() {
        return false; // MC 1.20.x doesn't have custom render pipelines
    }

    @Override
    public void warnUnsupportedFeature(String featureName) {
        if (!warnedFeatures.contains(featureName)) {
            warnedFeatures.add(featureName);
            LOGGER.warn("[NarrativeCraft] Feature '{}' is not available on MC 1.20.x - using fallback", featureName);
        }
    }
}
