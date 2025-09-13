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

import net.minecraft.world.phys.Vec2;

public class DialogData {
    private Vec2 offset;
    private float width, paddingX, paddingY, scale, letterSpacing, gap, noiseShakeSpeed, noiseShakeStrength;
    private int backgroundColor, textColor;
    private boolean noSkip, dialogAutoSkip;
    private double autoSkipSeconds;

    public DialogData(
            Vec2 offset,
            float width,
            float paddingX,
            float paddingY,
            float scale,
            float letterSpacing,
            float gap,
            int backgroundColor,
            int textColor,
            float noiseShakeSpeed,
            float noiseShakeStrength,
            boolean noSkip,
            boolean dialogAutoSkip,
            double autoSkipSeconds) {
        this.offset = offset;
        this.width = width;
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.scale = scale;
        this.letterSpacing = letterSpacing;
        this.gap = gap;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.noiseShakeSpeed = noiseShakeSpeed;
        this.noiseShakeStrength = noiseShakeStrength;
        this.noSkip = noSkip;
        this.dialogAutoSkip = dialogAutoSkip;
        this.autoSkipSeconds = autoSkipSeconds;
    }

    public Vec2 getOffset() {
        return offset;
    }

    public void setOffset(Vec2 offset) {
        this.offset = offset;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
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

    public float getNoiseShakeSpeed() {
        return noiseShakeSpeed;
    }

    public void setNoiseShakeSpeed(float noiseShakeSpeed) {
        this.noiseShakeSpeed = noiseShakeSpeed;
    }

    public float getNoiseShakeStrength() {
        return noiseShakeStrength;
    }

    public void setNoiseShakeStrength(float noiseShakeStrength) {
        this.noiseShakeStrength = noiseShakeStrength;
    }

    public boolean isNoSkip() {
        return noSkip;
    }

    public void setNoSkip(boolean noSkip) {
        this.noSkip = noSkip;
    }

    public boolean isDialogAutoSkip() {
        return dialogAutoSkip;
    }

    public void setDialogAutoSkip(boolean dialogAutoSkip) {
        this.dialogAutoSkip = dialogAutoSkip;
    }

    public double getAutoSkipSeconds() {
        return autoSkipSeconds;
    }

    public void setAutoSkipSeconds(double autoSkipSeconds) {
        this.autoSkipSeconds = autoSkipSeconds;
    }
}
