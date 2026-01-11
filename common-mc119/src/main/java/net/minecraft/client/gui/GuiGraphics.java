/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.minecraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.List;

/**
 * MC 1.19.x GuiGraphics shim.
 * This class provides a compatibility layer that mimics the GuiGraphics API
 * from MC 1.20+ while using PoseStack and GuiComponent internally.
 *
 * NOTE: This is a basic shim to allow code written for MC 1.20+ to compile
 * on MC 1.19.4. Not all methods are fully implemented - just the ones
 * needed for NarrativeCraft.
 */
public class GuiGraphics {
    private final Minecraft minecraft;
    private final PoseStack poseStack;

    public GuiGraphics(Minecraft minecraft, PoseStack poseStack) {
        this.minecraft = minecraft;
        this.poseStack = poseStack;
    }

    public PoseStack pose() {
        return poseStack;
    }

    public int guiWidth() {
        return minecraft.getWindow().getGuiScaledWidth();
    }

    public int guiHeight() {
        return minecraft.getWindow().getGuiScaledHeight();
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        innerFill(poseStack.last().pose(), x1, y1, x2, y2, color);
    }

    private void innerFill(Matrix4f matrix, int x1, int y1, int x2, int y2, int color) {
        int temp;
        if (x1 < x2) {
            temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 < y2) {
            temp = y1;
            y1 = y2;
            y2 = temp;
        }

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).color(r, g, b, a).endVertex();
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
    }

    public void drawString(Font font, String text, int x, int y, int color) {
        font.draw(poseStack, text, (float)x, (float)y, color);
    }

    public void drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            font.drawShadow(poseStack, text, (float)x, (float)y, color);
        } else {
            font.draw(poseStack, text, (float)x, (float)y, color);
        }
    }

    public void drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            font.drawShadow(poseStack, text, (float)x, (float)y, color);
        } else {
            font.draw(poseStack, text, (float)x, (float)y, color);
        }
    }

    public void drawCenteredString(Font font, String text, int x, int y, int color) {
        font.drawShadow(poseStack, text, (float)(x - font.width(text) / 2), (float)y, color);
    }

    public void drawCenteredString(Font font, Component text, int x, int y, int color) {
        font.drawShadow(poseStack, text, (float)(x - font.width(text) / 2), (float)y, color);
    }

    public void blit(ResourceLocation texture, int x, int y, int blitOffset, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = poseStack.last().pose();

        float u0 = (uOffset) / (float) textureWidth;
        float u1 = (uOffset + width) / (float) textureWidth;
        float v0 = (vOffset) / (float) textureHeight;
        float v1 = (vOffset + height) / (float) textureHeight;

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, (float)x, (float)(y + height), (float)blitOffset).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrix, (float)(x + width), (float)(y + height), (float)blitOffset).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrix, (float)(x + width), (float)y, (float)blitOffset).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrix, (float)x, (float)y, (float)blitOffset).uv(u0, v0).endVertex();
        Tesselator.getInstance().end();
    }

    public void blit(ResourceLocation texture, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        blit(texture, x, y, 0, uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

    public void fillGradient(int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        fillGradient(x1, y1, x2, y2, 0, colorFrom, colorTo);
    }

    public void fillGradient(int x1, int y1, int x2, int y2, int z, int colorFrom, int colorTo) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        fillGradient(poseStack.last().pose(), builder, x1, y1, x2, y2, z, colorFrom, colorTo);
        tesselator.end();

        RenderSystem.disableBlend();
    }

    private void fillGradient(Matrix4f matrix, BufferBuilder builder, int x1, int y1, int x2, int y2, int z, int colorFrom, int colorTo) {
        float a1 = (float)(colorFrom >> 24 & 255) / 255.0F;
        float r1 = (float)(colorFrom >> 16 & 255) / 255.0F;
        float g1 = (float)(colorFrom >> 8 & 255) / 255.0F;
        float b1 = (float)(colorFrom & 255) / 255.0F;

        float a2 = (float)(colorTo >> 24 & 255) / 255.0F;
        float r2 = (float)(colorTo >> 16 & 255) / 255.0F;
        float g2 = (float)(colorTo >> 8 & 255) / 255.0F;
        float b2 = (float)(colorTo & 255) / 255.0F;

        builder.vertex(matrix, (float)x2, (float)y1, (float)z).color(r1, g1, b1, a1).endVertex();
        builder.vertex(matrix, (float)x1, (float)y1, (float)z).color(r1, g1, b1, a1).endVertex();
        builder.vertex(matrix, (float)x1, (float)y2, (float)z).color(r2, g2, b2, a2).endVertex();
        builder.vertex(matrix, (float)x2, (float)y2, (float)z).color(r2, g2, b2, a2).endVertex();
    }

    public void renderTooltip(Font font, List<? extends ClientTooltipComponent> components, int x, int y) {
        // Simplified tooltip rendering - just draws text
        int lineY = y;
        for (ClientTooltipComponent component : components) {
            // In 1.19.4, tooltip rendering is more complex - this is a stub
            lineY += 10;
        }
    }

    public void renderTooltip(Font font, Component text, int x, int y) {
        // Simple tooltip - just draw the text
        drawString(font, text, x, y, 0xFFFFFFFF, true);
    }

    public void enableScissor(int x1, int y1, int x2, int y2) {
        double scale = minecraft.getWindow().getGuiScale();
        int windowHeight = minecraft.getWindow().getHeight();
        RenderSystem.enableScissor(
                (int)(x1 * scale),
                (int)(windowHeight - y2 * scale),
                (int)((x2 - x1) * scale),
                (int)((y2 - y1) * scale)
        );
    }

    public void disableScissor() {
        RenderSystem.disableScissor();
    }
}
