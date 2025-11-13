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
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.util.Easing;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
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
        this.color = FastColor.ABGR32.color((int) (0.8 * 255), color);
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

    public void render(PoseStack poseStack, VertexConsumer consumer, float partialTick) {
        if (!isRunning) return;
        double translateX = -dialogRenderer.getPaddingX() * 2 + offset;
        double opacity;
        int originalColor = color;
        if (currentTick < totalTick) {
            double t = Mth.clamp((currentTick + partialTick) / totalTick, 0.0, 1.0);
            t = Easing.SMOOTH.interpolate(t);
            translateX = Mth.lerp(t, translateX + xTranslatePoint, translateX);
            opacity = Mth.lerp(t, 0.0, 0.8);
            originalColor = FastColor.ABGR32.color((int) (opacity * 255.0), color);
        }
        poseStack.translate(translateX, 0, 0);
        draw(poseStack, consumer, originalColor);
    }

    private void draw(PoseStack poseStack, VertexConsumer consumer, int color) {
        Matrix4f matrix4f = poseStack.last().pose();

        float xStart = dialogRenderer.getTotalWidth() - width - offset;
        float xEnd = dialogRenderer.getTotalWidth() + width - offset;

        consumer.vertex(matrix4f, xStart, -height, 0.01f).color(color).endVertex();
        consumer.vertex(matrix4f, xStart, height, 0.01f).color(color).endVertex();
        consumer.vertex(matrix4f, xEnd, 0, 0.01f).color(color).endVertex();
        consumer.vertex(matrix4f, xStart, -height, 0.01f).color(color).endVertex();
    }

    public void render(GuiGraphics guiGraphics, float partialTick) {
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

        poseStack.translate((float) translateX + offset, 0, 0);

        VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f matrix4f = poseStack.last().pose();

        consumer.vertex(matrix4f, -width, -height, 0.01f)
                .color(FastColor.ABGR32.color((int) (opacity * 255), color))
                .endVertex();
        consumer.vertex(matrix4f, -width, height, 0.01f)
                .color(FastColor.ABGR32.color((int) (opacity * 255), color))
                .endVertex();
        consumer.vertex(matrix4f, width, 0, 0.01f)
                .color(FastColor.ABGR32.color((int) (opacity * 255), color))
                .endVertex();
        consumer.vertex(matrix4f, -width, -height, 0.01f)
                .color(FastColor.ABGR32.color((int) (opacity * 255), color))
                .endVertex();

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
