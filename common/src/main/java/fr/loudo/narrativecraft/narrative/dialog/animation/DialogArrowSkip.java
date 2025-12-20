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
import fr.loudo.narrativecraft.client.NarrativeCraftModClient;
import fr.loudo.narrativecraft.gui.ICustomGuiRender;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.util.Easing;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;

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
        this.color = ARGB.color((int) (0.8 * 255), color);
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
            originalColor = ARGB.color((int) (opacity * 255.0), color);
        }
        poseStack.translate(translateX, 0, 0);
        draw(poseStack, bufferSource, originalColor);
    }

    private void draw(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int color) {
        VertexConsumer vertexConsumer =
                bufferSource.getBuffer(NarrativeCraftModClient.getInstance().dialogBackgroundRenderType());
        Matrix4f matrix4f = poseStack.last().pose();

        float xStart = dialogRenderer.getTotalWidth() - width - offset;
        float xEnd = dialogRenderer.getTotalWidth() + width - offset;

        vertexConsumer
                .addVertex(matrix4f, xStart, -height, 0.01f)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer
                .addVertex(matrix4f, xStart, height, 0.01f)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, xEnd, 0, 0.01f).setColor(color).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer
                .addVertex(matrix4f, xStart, -height, 0.01f)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
    }

    public void render(GuiGraphics guiGraphics, float partialTick) {
        Matrix3x2fStack poseStack = guiGraphics.pose();

        double translateX = 0;
        double opacity = isRunning ? 1.0 : 0.0;
        poseStack.pushMatrix();

        if (currentTick < totalTick) {
            double t = Mth.clamp((currentTick + partialTick) / totalTick, 0.0, 1.0);
            t = Easing.SMOOTH.interpolate(t);
            translateX = Mth.lerp(t, xTranslatePoint, translateX);
            opacity = Mth.lerp(t, 0.0, 1.0);
        }

        poseStack.translate((float) translateX + offset, 0);

        ((ICustomGuiRender) guiGraphics)
                .narrativecraft$drawDialogSkip(width, height, ARGB.color((int) (opacity * 255), color));

        poseStack.popMatrix();
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
