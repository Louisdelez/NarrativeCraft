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
import fr.loudo.narrativecraft.narrative.dialog.animation.DialogArrowSkip;
import fr.loudo.narrativecraft.narrative.dialog.animation.DialogScrollText;
import fr.loudo.narrativecraft.util.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class DialogRenderer {

    protected final Minecraft minecraft;
    protected final double dialogTransitionTime = 0.5;
    protected final double dialogAppearTime = 0.2;
    protected final DialogScrollText dialogScrollText;
    protected final DialogArrowSkip dialogArrowSkip;
    protected String text;
    protected float width,
            totalWidth,
            oldWidth,
            height,
            totalHeight,
            oldHeight,
            paddingX,
            paddingY,
            oldScale,
            scale,
            letterSpacing,
            gap;
    protected int backgroundColor, textColor, currentTick, totalTick, totalTickAutoSkip;
    protected boolean noSkip, dialogStarting, dialogStopping, dialogAutoSkip, dialogAutoSkipping;
    protected Runnable runDialogStopped, runDialogAutoSkipped;

    public DialogRenderer(
            String text,
            float width,
            float paddingX,
            float paddingY,
            float scale,
            float letterSpacing,
            float gap,
            int backgroundColor,
            int textColor) {
        this.text = text;
        this.width = width;
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.scale = scale;
        this.letterSpacing = letterSpacing;
        this.gap = gap;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        minecraft = Minecraft.getInstance();
        dialogScrollText = new DialogScrollText(this, minecraft);
        dialogArrowSkip = new DialogArrowSkip(this, 2.5F, 2.5F, -5f, -10f, -1);
        initMeasures();
        dialogScrollText.reset();
    }

    public DialogRenderer(String text, DialogData dialogData) {
        this(
                text,
                dialogData.getWidth(),
                dialogData.getPaddingX(),
                dialogData.getPaddingY(),
                dialogData.getScale(),
                dialogData.getLetterSpacing(),
                dialogData.getGap(),
                dialogData.getBackgroundColor(),
                dialogData.getTextColor());
    }

    private void initMeasures() {
        height = minecraft.font.lineHeight * dialogScrollText.getLines().size();
        totalHeight = height + (dialogScrollText.getLines().size() - 1) * gap + 2 * paddingY;
        float widthLongestLine = (minecraft.font.width(dialogScrollText.getLongerTextLine())
                + (dialogScrollText.getLongerTextLine().length() - 1) * letterSpacing);
        totalWidth = (widthLongestLine / 2) + 2 * paddingX;
    }

    public void tick() {
        if (currentTick < totalTick || dialogAutoSkip && currentTick < totalTickAutoSkip) {
            currentTick++;
        }
        if (dialogStopping) {
            if (currentTick >= totalTick) {
                runDialogStopped.run();
            }
        } else if (dialogAutoSkip && dialogAutoSkipping && currentTick >= totalTickAutoSkip) {
            runDialogAutoSkipped.run();
        }
        dialogScrollText.tick();
        dialogArrowSkip.tick();
    }

    public void render(PoseStack poseStack, float partialTick) {}

    public void render(GuiGraphics guiGraphics, float partialTick) {}

    public void update() {
        dialogStarting = false;
        dialogStopping = false;
        totalTick = (int) (dialogTransitionTime * 20.0);
        currentTick = 0;
        dialogArrowSkip.stop();
        dialogAutoSkipping = false;
    }

    public void start() {
        dialogStarting = true;
        dialogStopping = false;
        totalTick = (int) (dialogAppearTime * 20.0);
        currentTick = 0;
        dialogArrowSkip.stop();
        dialogAutoSkipping = false;
    }

    public void stop() {
        dialogStarting = false;
        dialogStopping = true;
        totalTick = (int) (dialogAppearTime * 20.0);
        currentTick = 0;
        dialogArrowSkip.stop();
        dialogAutoSkipping = false;
    }

    public void autoSkipAt(double seconds) {
        if (seconds == 0.0) return;
        dialogAutoSkip = true;
        totalTickAutoSkip = (int) (seconds * 20.0);
    }

    public void stopAutoSkip() {
        dialogAutoSkip = false;
        dialogAutoSkipping = false;
        totalTickAutoSkip = 0;
    }

    public boolean isAnimating() {
        return currentTick < totalTick;
    }

    public DialogScrollText getDialogScrollText() {
        return dialogScrollText;
    }

    public float getInterpolatedWidth(float partialTick) {
        return (float) Mth.lerp(Easing.SMOOTH.interpolate(t(partialTick)), oldWidth, totalWidth);
    }

    public float getInterpolatedHeight(float partialTick) {
        return (float) Mth.lerp(Easing.SMOOTH.interpolate(t(partialTick)), oldHeight, totalHeight);
    }

    protected double t(float partialTick) {
        return Math.clamp((currentTick + partialTick) / totalTick, 0.0, 1.0);
    }

    public double getDialogTransitionTime() {
        return dialogTransitionTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        dialogScrollText.setText(text);
        initMeasures();
        dialogScrollText.reset();
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
        dialogScrollText.setText(text);
        initMeasures();
        dialogScrollText.reset();
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
        initMeasures();
    }

    public float getTotalHeight() {
        return totalHeight;
    }

    public float getTotalWidth() {
        return totalWidth;
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

    public Runnable getRunDialogStopped() {
        return runDialogStopped;
    }

    public void setRunDialogStopped(Runnable runDialogStopped) {
        this.runDialogStopped = runDialogStopped;
    }

    public void setRunDialogAutoSkipped(Runnable runDialogAutoSkipped) {
        this.runDialogAutoSkipped = runDialogAutoSkipped;
    }

    public int getTotalTickAutoSkip() {
        return totalTickAutoSkip;
    }

    public boolean isDialogAutoSkip() {
        return dialogAutoSkip;
    }
}
