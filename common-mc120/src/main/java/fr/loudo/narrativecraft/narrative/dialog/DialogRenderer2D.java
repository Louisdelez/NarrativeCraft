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
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.dialog.animation.DialogArrowSkip;
import fr.loudo.narrativecraft.util.Easing;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * MC 1.20.x version of DialogRenderer2D.
 * Key differences:
 * - Uses PoseStack instead of Matrix3x2fStack
 * - PoseStack.pushPose()/popPose() instead of Matrix3x2fStack.pushMatrix()/popMatrix()
 * - PoseStack.translate(double, double, double) instead of Matrix3x2fStack.translate(float, float)
 * - PoseStack.scale(float, float, float) instead of Matrix3x2fStack.scale(float, float)
 */
public class DialogRenderer2D extends DialogRenderer {

    private int widthBox, heightBox, offsetHeight;

    public DialogRenderer2D(
            String text,
            int widthText,
            int widthBox,
            int heightBox,
            int offsetHeight,
            float letterSpacing,
            float gap,
            int backgroundColor,
            int textColor) {
        super(text.trim(), widthText, 0, 0, 1, letterSpacing, gap, backgroundColor, textColor);
        this.offsetHeight = offsetHeight;
        dialogArrowSkip = new DialogArrowSkip(this, 3.5F, 3.5F, -5f, -6f, -1);
        this.widthBox = widthBox;
        this.heightBox = heightBox;
        dialogAppearTime = 0.4;
    }

    public DialogRenderer2D(
            String text, int widthText, int widthBox, int heightBox, int offsetHeight, DialogData dialogData) {
        super(
                text.trim(),
                widthText,
                0,
                0,
                1,
                dialogData.getLetterSpacing(),
                dialogData.getGap(),
                dialogData.getBackgroundColor(),
                dialogData.getTextColor());
        this.offsetHeight = offsetHeight;
        dialogArrowSkip = new DialogArrowSkip(this, 3.5F, 3.5F, -5f, -6f, -1);
        this.widthBox = widthBox;
        this.heightBox = heightBox;
        dialogAppearTime = 0.4;
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {

        // 1.20.x: Use PoseStack instead of Matrix3x2fStack
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        float originalScale = scale;
        if (currentTick < totalTick) {
            double t = t(partialTick);
            t = Easing.SMOOTH.interpolate(t);
            if (dialogStarting || dialogStopping) {
                double opacity;
                if (dialogStarting) {
                    originalScale = (float) Mth.lerp(t, 0.8, scale);
                    opacity = Mth.lerp(t, 0.0, 1.0);
                } else {
                    originalScale = (float) Mth.lerp(t, scale, 0.8);
                    opacity = Mth.lerp(t, 0.8, 0.0);
                }
                backgroundColor = NarrativeCraftMod.getColorCompat().color((int) (opacity * 255.0), backgroundColor);
            }
        }
        if (currentTick == totalTick) {
            if (dialogStopping && !dialogStarting) dialogStopping = false;
            if (dialogStarting && !dialogStopping) dialogStarting = false;
        }

        int windowWidth = minecraft.getWindow().getGuiScaledWidth();
        int windowHeight = minecraft.getWindow().getGuiScaledHeight();

        int offsetDialog = offsetHeight;
        int guiScale = minecraft.options.guiScale().get();
        switch (guiScale) {
            case 1:
                offsetDialog *= 4;
            case 2:
                offsetDialog *= 2;
        }

        int centerX = windowWidth / 2;
        int centerY = windowHeight - offsetDialog - heightBox / 2;

        // 1.20.x: PoseStack.translate takes doubles and has 3 parameters (x, y, z)
        poseStack.translate(centerX, centerY, 0);
        poseStack.scale(originalScale, originalScale, 1.0f);

        poseStack.translate(-widthBox / 2.0, -heightBox / 2.0, 0);

        guiGraphics.fill(0, 0, widthBox, heightBox, backgroundColor);

        poseStack.translate(widthBox / 2.0 - 4.0, heightBox / 2.0, 0);

        if (!dialogStopping) {
            dialogScrollTextDialog.render(guiGraphics, partialTick);
            if (dialogScrollTextDialog.isFinished()) {
                if (!dialogAutoSkipping) {
                    dialogAutoSkipping = true;
                    currentTick = 0;
                }
                dialogArrowSkip.start();
            }
            if (!noSkip) {
                poseStack.pushPose();
                poseStack.translate((widthBox / 2.0), heightBox / 2.0 - 10, 0);
                dialogArrowSkip.render(guiGraphics, partialTick);
                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }

    public int getWidthBox() {
        return widthBox;
    }

    public void setWidthBox(int widthBox) {
        this.widthBox = widthBox;
    }

    public int getHeightBox() {
        return heightBox;
    }

    public void setHeightBox(int heightBox) {
        this.heightBox = heightBox;
    }

    public int getOffsetHeight() {
        return offsetHeight;
    }

    public void setOffsetHeight(int offsetHeight) {
        this.offsetHeight = offsetHeight;
    }
}
