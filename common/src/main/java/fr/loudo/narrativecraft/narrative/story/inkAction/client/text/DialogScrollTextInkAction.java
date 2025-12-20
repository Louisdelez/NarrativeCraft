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

package fr.loudo.narrativecraft.narrative.story.inkAction.client.text;

import fr.loudo.narrativecraft.gui.ICustomGuiRender;
import fr.loudo.narrativecraft.narrative.dialog.animation.AbstractDialogScrollText;
import fr.loudo.narrativecraft.util.Util;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

public class DialogScrollTextInkAction extends AbstractDialogScrollText {

    private final TextInkAction textInkAction;
    private boolean isBlock;
    private int endAt;

    public DialogScrollTextInkAction(Minecraft minecraft, TextInkAction textInkAction) {
        super(minecraft);
        this.textInkAction = textInkAction;
    }

    @Override
    protected List<String> splitTextIntoLines(String text) {
        Attribute attribute = textInkAction.getAttribute();
        return Util.splitText(text, minecraft.font, attribute.getWidth());
    }

    @Override
    protected float getInitialX() {
        return 0;
    }

    @Override
    protected float getInitialY() {
        return 0;
    }

    @Override
    protected boolean canScroll() {
        return true;
    }

    public int getEndAt() {
        return endAt;
    }

    public void setEndAt(int endAt) {
        this.endAt = endAt;
    }

    public void render(GuiGraphics guiGraphics, float partialTick, double opacity) {
        Attribute attribute = textInkAction.getAttribute();
        if (!attribute.isRender()) return;

        Matrix3x2fStack poseStack = guiGraphics.pose();
        Font font = attribute.getFont();
        Position position = attribute.getPosition();

        Component longerText = Component.literal(getLongerTextLine()).withStyle(style -> {
            if (attribute.getCustomFont() == null) return style;
            return style.withFont(new FontDescription.Resource(attribute.getCustomFont()));
        });

        int textWidth = font.width(longerText.getVisualOrderText());
        int textHeight = font.lineHeight;

        float centerX = guiGraphics.guiWidth() / 2f;
        float centerY = guiGraphics.guiHeight() / 2f;

        float totalHeight = textHeight * lines.size();

        float anchorX = 0;
        float anchorY = 0;
        float textOffsetX = 0;
        float textOffsetY = 0;

        switch (position) {
            case TOP -> {
                anchorX = centerX;
                anchorY = 0;
                textOffsetX = -textWidth / 2f;
                textOffsetY = 0;
            }
            case TOP_LEFT -> {
                anchorX = 0;
                anchorY = 0;
                textOffsetX = 0;
                textOffsetY = 0;
            }
            case TOP_RIGHT -> {
                anchorX = guiGraphics.guiWidth();
                anchorY = 0;
                textOffsetX = -textWidth;
                textOffsetY = 0;
            }
            case MIDDLE -> {
                anchorX = centerX;
                anchorY = centerY;
                textOffsetX = -textWidth / 2f;
                textOffsetY = -totalHeight / 2f;
            }
            case MIDDLE_LEFT -> {
                anchorX = 0;
                anchorY = centerY;
                textOffsetX = 0;
                textOffsetY = -totalHeight / 2f;
            }
            case MIDDLE_RIGHT -> {
                anchorX = guiGraphics.guiWidth();
                anchorY = centerY;
                textOffsetX = -textWidth;
                textOffsetY = -totalHeight / 2f;
            }
            case BOTTOM -> {
                anchorX = centerX;
                anchorY = guiGraphics.guiHeight();
                textOffsetX = -textWidth / 2f;
                textOffsetY = -totalHeight;
            }
            case BOTTOM_LEFT -> {
                anchorX = 0;
                anchorY = guiGraphics.guiHeight();
                textOffsetX = 0;
                textOffsetY = -totalHeight;
            }
            case BOTTOM_RIGHT -> {
                anchorX = guiGraphics.guiWidth();
                anchorY = guiGraphics.guiHeight();
                textOffsetX = -textWidth;
                textOffsetY = -totalHeight;
            }
        }

        float[] spacing = attribute.getSpacing();
        if (spacing == null) {
            spacing = new float[] {0, 0};
        }

        poseStack.pushMatrix();
        poseStack.translate(anchorX + spacing[0], anchorY + spacing[1]);
        poseStack.scale(attribute.getScale(), attribute.getScale());
        poseStack.translate(textOffsetX, textOffsetY);

        Map<Integer, Vector2f> offsets = getTextEffectOffsets(partialTick);

        for (int i = 0; i < lettersRenderer.size(); i++) {
            LetterLocation letter = lettersRenderer.get(i);
            if (!letter.render()) continue;

            float x = letter.x();
            float y = letter.y();

            if (offsets.containsKey(i)) {
                x += offsets.get(i).x;
                y += offsets.get(i).y;
            }

            ((ICustomGuiRender) guiGraphics)
                    .narrativecraft$drawStringFloat(
                            String.valueOf(letter.letter()),
                            font,
                            x,
                            y,
                            ARGB.color(
                                    (int) (opacity * 255.0),
                                    textInkAction.getAttribute().getColor()),
                            attribute.isDropShadow());
        }

        poseStack.popMatrix();
    }

    public boolean isBlock() {
        return isBlock;
    }

    public void setBlock(boolean block) {
        isBlock = block;
    }
}
