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

package fr.loudo.narrativecraft.narrative.dialog;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class DialogRenderer {

    protected final Minecraft minecraft;
    protected final double dialogTransitionTime = 0.5;
    protected final double dialogAppearTime = 0.2;
    protected float width, oldWidth, height, oldHeight, paddingX, paddingY, scale, letterSpacing, gap;
    protected int backgroundColor, textColor, currentTick, totalTick;
    protected boolean noSkip, dialogStarting, dialogStopping;

    public DialogRenderer(
            float width,
            float height,
            float paddingX,
            float paddingY,
            float scale,
            float letterSpacing,
            float gap,
            int backgroundColor,
            int textColor,
            boolean noSkip) {
        this.width = width;
        this.height = height;
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.scale = scale;
        this.letterSpacing = letterSpacing;
        this.gap = gap;
        this.backgroundColor = ARGB.color(255, backgroundColor);
        this.textColor = textColor;
        this.noSkip = noSkip;
        minecraft = Minecraft.getInstance();
    }

    public void tick() {
        if (currentTick <= totalTick) {
            currentTick++;
        }
    }

    public void render(PoseStack poseStack, float partialTick) {}

    public void render(GuiGraphics guiGraphics, float partialTick) {}

    public void update() {
        dialogStarting = false;
        dialogStopping = false;
        totalTick = (int) (dialogTransitionTime * 20.0);
        currentTick = 0;
    }

    public void start() {
        dialogStarting = true;
        dialogStopping = false;
        totalTick = (int) (dialogAppearTime * 20.0);
        currentTick = 0;
    }

    public void stop() {
        dialogStarting = false;
        dialogStopping = true;
        totalTick = (int) (dialogAppearTime * 20.0);
        currentTick = 0;
    }

    public boolean isAnimating() {
        return currentTick < totalTick;
    }

    public float getInterpolatedWidth(float partialTick) {
        return (float) Mth.lerp(t(partialTick), oldWidth, width);
    }

    public float getInterpolatedHeight(float partialTick) {
        return (float) Mth.lerp(t(partialTick), oldHeight, height);
    }

    protected double t(float partialTick) {
        return Math.clamp((currentTick + partialTick) / totalTick, 0.0, 1.0);
    }

    public double getDialogTransitionTime() {
        return dialogTransitionTime;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getPaddingX() {
        return paddingX;
    }

    public void setPaddingX(float paddingX) {
        this.paddingX = paddingX;
    }

    public float getPaddingY() {
        return paddingY;
    }

    public void setPaddingY(float paddingY) {
        this.paddingY = paddingY;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getLetterSpacing() {
        return letterSpacing;
    }

    public void setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
    }

    public float getGap() {
        return gap;
    }

    public void setGap(float gap) {
        this.gap = gap;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public boolean isNoSkip() {
        return noSkip;
    }

    public void setNoSkip(boolean noSkip) {
        this.noSkip = noSkip;
    }

    public boolean isDialogStarting() {
        return dialogStarting;
    }

    public void setDialogStarting(boolean dialogStarting) {
        this.dialogStarting = dialogStarting;
    }

    public boolean isDialogStopping() {
        return dialogStopping;
    }

    public void setDialogStopping(boolean dialogStopping) {
        this.dialogStopping = dialogStopping;
    }
}
