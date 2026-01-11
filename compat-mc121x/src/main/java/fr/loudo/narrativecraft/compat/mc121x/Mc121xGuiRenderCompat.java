/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc121x;

import fr.loudo.narrativecraft.compat.api.IGuiRenderCompat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * GUI rendering compatibility implementation for Minecraft 1.21.x.
 * Uses the GuiGraphics API and RenderPipelines available in MC 1.21+.
 */
public class Mc121xGuiRenderCompat implements IGuiRenderCompat {

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
        // For MC 1.21.x, we can use the standard fill with int conversion
        // The actual float rendering is handled via the ICustomGuiRender mixin in loader modules
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
        // For MC 1.21.x, delegate to standard drawString with int conversion
        // Float positioning is handled via mixin in loader modules
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
        Identifier texture = Identifier.parse(textureId);
        gui.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    @Override
    public void pushPose(Object graphics) {
        // MC 1.21.x uses Matrix3x2fStack (JOML) which has pushMatrix()
        ((GuiGraphics) graphics).pose().pushMatrix();
    }

    @Override
    public void popPose(Object graphics) {
        // MC 1.21.x uses Matrix3x2fStack (JOML) which has popMatrix()
        ((GuiGraphics) graphics).pose().popMatrix();
    }

    @Override
    public void translate(Object graphics, double x, double y, double z) {
        // Matrix3x2fStack only supports 2D translation
        ((GuiGraphics) graphics).pose().translate((float) x, (float) y);
    }

    @Override
    public void scale(Object graphics, float x, float y, float z) {
        // Matrix3x2fStack only supports 2D uniform scaling - use average of x/y
        ((GuiGraphics) graphics).pose().scale(x, y);
    }

    @Override
    public void drawDialogSkipArrow(Object graphics, float centerX, float centerY, float width, float height, int color) {
        // For MC 1.21.x, this is handled via mixin and Fill2dGui/SkipArrow2dGui in loader modules
        // This fallback uses simple fill to draw an arrow shape
        GuiGraphics gui = (GuiGraphics) graphics;
        int cx = (int) centerX;
        int cy = (int) centerY;
        int w = (int) width;
        int h = (int) height;

        // Draw simple arrow pointing right using filled rectangles
        gui.fill(cx - w, cy - h / 2, cx, cy + h / 2, color);
        gui.fill(cx, cy - h / 4, cx + w / 2, cy + h / 4, color);
    }

    @Override
    public boolean supportsCustomRenderPipelines() {
        return true; // MC 1.21.x supports custom render pipelines
    }

    @Override
    public void warnUnsupportedFeature(String featureName) {
        if (!warnedFeatures.contains(featureName)) {
            warnedFeatures.add(featureName);
            LOGGER.warn("[NarrativeCraft] Feature '{}' may have limited support on MC 1.21.x", featureName);
        }
    }
}
