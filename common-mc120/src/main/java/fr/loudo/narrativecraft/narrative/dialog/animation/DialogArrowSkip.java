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

package fr.loudo.narrativecraft.narrative.dialog.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.compat.api.RenderChannel;
import fr.loudo.narrativecraft.gui.ICustomGuiRender;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.util.Easing;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * MC 1.20.x version of DialogArrowSkip.
 * Key differences from 1.21.x:
 * - VertexConsumer API: Uses .vertex().color().uv().overlayCoords().uv2().normal().endVertex()
 *   instead of .addVertex().setColor().setLight()
 * - GuiGraphics.pose(): Returns PoseStack instead of Matrix3x2fStack
 *   In 1.21.x, guiGraphics.pose() returns Matrix3x2fStack with pushMatrix()/popMatrix()/translate(float,float)
 *   In 1.20.x, guiGraphics.pose() returns PoseStack with pushPose()/popPose()/translate(double,double,double)
 */
public class DialogArrowSkip {

    private final double translateTime = 0.4;
    private final DialogRenderer dialogRenderer;

    private float width, height, offset, xTranslatePoint;
    private int color, currentTick, totalTick;

    private boolean isRunning = false;

    public DialogArrowSkip(
            DialogRenderer dialogRenderer, float width, float height, float xTranslatePoint, float offset, int color) {
        this.dialogRenderer = dialogRenderer;
        this.width = width;
        this.height = height;
        this.xTranslatePoint = xTranslatePoint;
        this.offset = offset;
        this.color = NarrativeCraftMod.getColorCompat().color((int) (0.8 * 255), color);
        totalTick = (int) (translateTime * 20.0);
    }

    public void start() {
        if (isRunning) return;
        currentTick = 0;
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
    }

    public void tick() {
        if (currentTick < totalTick && isRunning) {
            currentTick++;
        }
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTick) {
        if (!isRunning) return;
        double translateX = -dialogRenderer.getPaddingX() * 2 + offset;
        double opacity;
        int originalColor = color;
        if (currentTick < totalTick) {
            double t = Mth.clamp((currentTick + partialTick) / totalTick, 0.0, 1.0);
            t = Easing.SMOOTH.interpolate(t);
            translateX = Mth.lerp(t, translateX + xTranslatePoint, translateX);
            opacity = Mth.lerp(t, 0.0, 0.8);
            originalColor = NarrativeCraftMod.getColorCompat().color((int) (opacity * 255.0), color);
        }
        poseStack.translate(translateX, 0, 0);
        draw(poseStack, bufferSource, originalColor);
    }

    /**
     * Helper method for 1.20.x VertexConsumer API.
     * 1.20.x uses: .vertex().color().uv().overlayCoords().uv2().normal().endVertex()
     * 1.21.x uses: .addVertex().setColor().setLight()
     */
    private void addColoredVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int color, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(color)
                .uv(0, 0)
                .overlayCoords(0, 10)
                .uv2(light)
                .normal(0, 0, 1)
                .endVertex();
    }

    private void draw(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int color) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(NarrativeCraftMod.getRenderType(RenderChannel.DIALOG_BACKGROUND));
        Matrix4f matrix4f = poseStack.last().pose();

        float xStart = dialogRenderer.getTotalWidth() - width - offset;
        float xEnd = dialogRenderer.getTotalWidth() + width - offset;

        int light = LightTexture.FULL_BRIGHT;

        // 1.20.x API: Use vertex().color().uv().overlayCoords().uv2().normal().endVertex()
        // instead of addVertex().setColor().setLight()
        addColoredVertex(vertexConsumer, matrix4f, xStart, -height, 0.01f, color, light);
        addColoredVertex(vertexConsumer, matrix4f, xStart, height, 0.01f, color, light);
        addColoredVertex(vertexConsumer, matrix4f, xEnd, 0, 0.01f, color, light);
        addColoredVertex(vertexConsumer, matrix4f, xStart, -height, 0.01f, color, light);
    }

    public void render(GuiGraphics guiGraphics, float partialTick) {
        // 1.20.x API: guiGraphics.pose() returns PoseStack, not Matrix3x2fStack
        // Use pushPose()/popPose() instead of pushMatrix()/popMatrix()
        // Use translate(double, double, double) instead of translate(float, float)
        PoseStack poseStack = guiGraphics.pose();

        double translateX = 0;
        double opacity = isRunning ? 1.0 : 0.0;
        poseStack.pushPose();

        if (currentTick < totalTick) {
            double t = Mth.clamp((currentTick + partialTick) / totalTick, 0.0, 1.0);
            t = Easing.SMOOTH.interpolate(t);
            translateX = Mth.lerp(t, xTranslatePoint, translateX);
            opacity = Mth.lerp(t, 0.0, 1.0);
        }

        poseStack.translate(translateX + offset, 0, 0);

        ((ICustomGuiRender) guiGraphics)
                .narrativecraft$drawDialogSkip(width, height, NarrativeCraftMod.getColorCompat().color((int) (opacity * 255), color));

        poseStack.popPose();
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getOffset() {
        return offset;
    }

    public int getColor() {
        return color;
    }
}
