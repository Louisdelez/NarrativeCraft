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

package fr.loudo.narrativecraft.screens.components;

import com.bladecoder.ink.runtime.Choice;
import fr.loudo.narrativecraft.gui.ICustomGuiRender;
import fr.loudo.narrativecraft.narrative.story.text.ParsedDialog;
import fr.loudo.narrativecraft.narrative.story.text.TextEffectAnimation;
import fr.loudo.narrativecraft.util.Util;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.joml.Vector2f;

public class ChoiceButtonWidget extends AbstractButton {

    private final Minecraft minecraft = Minecraft.getInstance();

    private final int index;
    private final int paddingX;
    private final int paddingY;
    private final int hoverWidth;
    private final boolean hoverBorder;
    private final Consumer<Integer> onPress;
    private int backgroundColor, textColor, hoverColor;
    private final ParsedDialog parsedDialog;
    private final TextEffectAnimation textEffectAnimation;
    private boolean canPress;

    public ChoiceButtonWidget(Choice choice, Consumer<Integer> onPress) {
        super(0, 0, 0, 0, Component.literal(choice.getText()));
        Font font = Minecraft.getInstance().font;
        this.onPress = onPress;
        String choiceString = this.getMessage().getString();
        index = choice.getIndex();
        paddingX = 9;
        paddingY = 6;
        backgroundColor = 0x000000;
        textColor = 0xFFFFFF;
        hoverColor = 0xFFFFFF;
        hoverBorder = true;
        hoverWidth = 1;
        canPress = true;
        parsedDialog = ParsedDialog.parse(choiceString);
        int width = font.width(parsedDialog.cleanedText());
        int height = font.lineHeight;
        this.setWidth(width + paddingX * 2);
        this.setHeight(height + paddingY * 2);
        textEffectAnimation = new TextEffectAnimation(parsedDialog);
    }

    public void tick() {
        textEffectAnimation.tick();
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = this.getX();
        int top = this.getY();
        int right = left + this.getWidth();
        int bottom = top + this.getHeight();

        if (this.isHovered && hoverBorder && canPress) {
            guiGraphics.fill(left - hoverWidth, top - hoverWidth, right + hoverWidth, bottom + hoverWidth, hoverColor);
        }

        guiGraphics.fill(left, top, right, bottom, backgroundColor);

        Map<Integer, Vector2f> letterOffsets = textEffectAnimation.getOffsets(partialTick);

        float startX = left + paddingX;

        for (int i = 0; i < parsedDialog.cleanedText().length(); i++) {
            Vector2f offset = letterOffsets.getOrDefault(i, new Vector2f(0, 0));
            String character = String.valueOf(parsedDialog.cleanedText().charAt(i));
            ((ICustomGuiRender) guiGraphics)
                    .narrativecraft$drawStringFloat(
                            character,
                            Minecraft.getInstance().font,
                            startX + offset.x,
                            top + paddingY + 1 + offset.y,
                            textColor,
                            false);
            startX += Util.getLetterWidth(parsedDialog.cleanedText().codePointAt(i), minecraft);
        }
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        if (!canPress) return;
        onPress.accept(index);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(event.buttonInfo())) {
                boolean flag = this.isMouseOver(event.x(), event.y());
                if (flag) {
                    this.onClick(event, isDoubleClick);
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public void setOpacity(int opacity) {
        backgroundColor = ARGB.color(opacity, backgroundColor);
        textColor = ARGB.color(opacity, textColor);
        hoverColor = ARGB.color(opacity, hoverColor);
    }

    public boolean isCanPress() {
        return canPress;
    }

    public void setCanPress(boolean canPress) {
        this.canPress = canPress;
    }
}
