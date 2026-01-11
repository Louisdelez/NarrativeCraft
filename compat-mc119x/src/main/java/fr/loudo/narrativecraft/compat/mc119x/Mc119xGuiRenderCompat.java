/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc119x;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.loudo.narrativecraft.compat.api.IGuiRenderCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * GUI rendering compatibility implementation for Minecraft 1.19.x.
 * MC 1.19.4 does NOT have GuiGraphics - uses PoseStack + direct render calls.
 * All fill/draw methods use the old-style Tesselator/BufferBuilder approach.
 */
public class Mc119xGuiRenderCompat implements IGuiRenderCompat {

    private static final Logger LOGGER = LoggerFactory.getLogger("NarrativeCraft");
    private final Set<String> warnedFeatures = new HashSet<>();

    @Override
    public void fill(Object graphics, int x1, int y1, int x2, int y2, int color) {
        // In 1.19.4, 'graphics' is actually a PoseStack
        PoseStack poseStack = (PoseStack) graphics;
        fillInternal(poseStack, x1, y1, x2, y2, color);
    }

    @Override
    public void fillGradient(Object graphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        PoseStack poseStack = (PoseStack) graphics;
        fillGradientInternal(poseStack.last().pose(), x1, y1, x2, y2, colorFrom, colorTo);
    }

    @Override
    public void fillFloat(Object graphics, float x1, float y1, float x2, float y2, int color) {
        PoseStack poseStack = (PoseStack) graphics;
        fillInternal(poseStack, (int) x1, (int) y1, (int) x2, (int) y2, color);
    }

    @Override
    public void drawString(Object graphics, Object font, String text, int x, int y, int color) {
        PoseStack poseStack = (PoseStack) graphics;
        Font f = (Font) font;
        f.drawShadow(poseStack, text, x, y, color);
    }

    @Override
    public void drawStringNoShadow(Object graphics, Object font, String text, int x, int y, int color) {
        PoseStack poseStack = (PoseStack) graphics;
        Font f = (Font) font;
        f.draw(poseStack, text, x, y, color);
    }

    @Override
    public void drawCenteredString(Object graphics, Object font, String text, int centerX, int y, int color) {
        PoseStack poseStack = (PoseStack) graphics;
        Font f = (Font) font;
        int width = f.width(text);
        f.drawShadow(poseStack, text, centerX - width / 2.0f, y, color);
    }

    @Override
    public void drawStringFloat(Object graphics, Object font, String text, float x, float y, int color, boolean shadow) {
        PoseStack poseStack = (PoseStack) graphics;
        Font f = (Font) font;
        if (shadow) {
            f.drawShadow(poseStack, text, x, y, color);
        } else {
            f.draw(poseStack, text, x, y, color);
        }
    }

    @Override
    public void enableScissor(Object graphics, int x1, int y1, int x2, int y2) {
        // 1.19.4 uses Screen.enableScissor directly
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        int scissorX = (int) (x1 * scale);
        int scissorY = (int) ((Minecraft.getInstance().getWindow().getGuiScaledHeight() - y2) * scale);
        int scissorW = (int) ((x2 - x1) * scale);
        int scissorH = (int) ((y2 - y1) * scale);
        RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
    }

    @Override
    public void disableScissor(Object graphics) {
        RenderSystem.disableScissor();
    }

    @Override
    public void blitTexture(Object graphics, String textureId, int x, int y, float u, float v,
                            int width, int height, int textureWidth, int textureHeight) {
        PoseStack poseStack = (PoseStack) graphics;
        ResourceLocation texture = new ResourceLocation(textureId);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        blitInternal(poseStack, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    @Override
    public void pushPose(Object graphics) {
        PoseStack poseStack = (PoseStack) graphics;
        poseStack.pushPose();
    }

    @Override
    public void popPose(Object graphics) {
        PoseStack poseStack = (PoseStack) graphics;
        poseStack.popPose();
    }

    @Override
    public void translate(Object graphics, double x, double y, double z) {
        PoseStack poseStack = (PoseStack) graphics;
        poseStack.translate(x, y, z);
    }

    @Override
    public void scale(Object graphics, float x, float y, float z) {
        PoseStack poseStack = (PoseStack) graphics;
        poseStack.scale(x, y, z);
    }

    @Override
    public void drawDialogSkipArrow(Object graphics, float centerX, float centerY, float width, float height, int color) {
        PoseStack poseStack = (PoseStack) graphics;
        int cx = (int) centerX;
        int cy = (int) centerY;
        int w = (int) width;
        int h = (int) height;

        // Simple right-pointing arrow using rectangles
        fillInternal(poseStack, cx - w, cy - h / 2, cx, cy + h / 2, color);
        fillInternal(poseStack, cx, cy - h / 4, cx + w / 2, cy + h / 4, color);
    }

    @Override
    public boolean supportsCustomRenderPipelines() {
        return false;
    }

    @Override
    public void warnUnsupportedFeature(String featureName) {
        if (!warnedFeatures.contains(featureName)) {
            warnedFeatures.add(featureName);
            LOGGER.warn("[NarrativeCraft] Feature '{}' is not available on MC 1.19.x - using fallback", featureName);
        }
    }

    // Internal helper methods for 1.19.4 rendering

    private void fillInternal(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        // Ensure x1 < x2, y1 < y2
        if (x1 > x2) { int tmp = x1; x1 = x2; x2 = tmp; }
        if (y1 > y2) { int tmp = y1; y1 = y2; y2 = tmp; }

        float a = (float) (color >> 24 & 255) / 255.0f;
        float r = (float) (color >> 16 & 255) / 255.0f;
        float g = (float) (color >> 8 & 255) / 255.0f;
        float b = (float) (color & 255) / 255.0f;

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y2, 0).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x2, y2, 0).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x2, y1, 0).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x1, y1, 0).color(r, g, b, a).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    private void fillGradientInternal(Matrix4f matrix, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        float a1 = (float) (colorFrom >> 24 & 255) / 255.0f;
        float r1 = (float) (colorFrom >> 16 & 255) / 255.0f;
        float g1 = (float) (colorFrom >> 8 & 255) / 255.0f;
        float b1 = (float) (colorFrom & 255) / 255.0f;

        float a2 = (float) (colorTo >> 24 & 255) / 255.0f;
        float r2 = (float) (colorTo >> 16 & 255) / 255.0f;
        float g2 = (float) (colorTo >> 8 & 255) / 255.0f;
        float b2 = (float) (colorTo & 255) / 255.0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x2, y1, 0).color(r1, g1, b1, a1).endVertex();
        bufferBuilder.vertex(matrix, x1, y1, 0).color(r1, g1, b1, a1).endVertex();
        bufferBuilder.vertex(matrix, x1, y2, 0).color(r2, g2, b2, a2).endVertex();
        bufferBuilder.vertex(matrix, x2, y2, 0).color(r2, g2, b2, a2).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    private void blitInternal(PoseStack poseStack, int x, int y, float u, float v,
                              int width, int height, int textureWidth, int textureHeight) {
        Matrix4f matrix = poseStack.last().pose();
        float u0 = u / textureWidth;
        float u1 = (u + width) / textureWidth;
        float v0 = v / textureHeight;
        float v1 = (v + height) / textureHeight;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x, y + height, 0).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, 0).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, 0).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}
