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

package fr.loudo.narrativecraft.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

/**
 * MC 1.20.x version of ScreenUtils.
 * Key differences from 1.21.x:
 * - Toast API: Use getToasts() instead of getToastManager()
 * - MultiLineEditBox: Use direct constructor instead of builder pattern
 *   In 1.21.x: MultiLineEditBox.builder().setPlaceholder(...).setX(...).setY(...).build(font, width, height, label)
 *   In 1.20.x: new MultiLineEditBox(font, x, y, width, height, placeholder, label)
 */
public class ScreenUtils {

    public static StringWidget text(Component text, Font font, int x, int y) {
        return new StringWidget(x, y, font.width(text.getVisualOrderText()), 9, text, font);
    }

    public static StringWidget text(Component text, Font font, int x, int y, int color) {
        StringWidget stringWidget = new StringWidget(x, y, font.width(text.getVisualOrderText()), 9, text, font);
//        stringWidget.setColor(color);
        return stringWidget;
    }

    public static void sendToast(Component name, Component description) {
        // 1.20.x API: getToasts() instead of getToastManager()
        Minecraft.getInstance()
                .getToasts()
                .addToast(new SystemToast(SystemToast.SystemToastId.NARRATOR_TOGGLE, name, description));
    }

    public static class LabelBox {

        private final StringWidget stringWidget;
        private final EditBox editBox;
        private final Align align;

        public LabelBox(Component text, Font font, int width, int height, int x, int y, Align align) {
            int yStringWidget = y;
            if (align == Align.HORIZONTAL) {
                y += height / 2;
                yStringWidget = y - font.lineHeight / 2;
            }
            stringWidget = ScreenUtils.text(text, font, x, yStringWidget);
            if (align == Align.HORIZONTAL) {
                x = stringWidget.getX() + stringWidget.getWidth() + 5;
                y -= height / 2;
            } else if (align == Align.VERTICAL) {
                y += font.lineHeight + 5;
            }
            editBox = new EditBox(font, x, y, width, height, Component.literal(text + " value"));
            this.align = align;
        }

        public void setPosition(int x, int y) {
            if (align == Align.HORIZONTAL) {
                stringWidget.setPosition(x, y + editBox.getHeight() / 2 - stringWidget.getHeight() / 2);
            } else if (align == Align.VERTICAL) {
                stringWidget.setPosition(x, y);
            }
            if (align == Align.HORIZONTAL) {
                x += stringWidget.getWidth() + 5;
            } else if (align == Align.VERTICAL) {
                y = stringWidget.getY() + stringWidget.getHeight() + 5;
            }
            editBox.setPosition(x, y);
        }

        public StringWidget getStringWidget() {
            return stringWidget;
        }

        public EditBox getEditBox() {
            return editBox;
        }
    }

    public static class MultilineLabelBox {

        private final StringWidget stringWidget;
        private final MultiLineEditBox multiLineEditBox;

        public MultilineLabelBox(
                Component text, Font font, int width, int height, int x, int y, Component placeholder) {
            stringWidget = ScreenUtils.text(text, font, x, y);
            // 1.20.x API: Use direct constructor instead of builder pattern
            // In 1.21.x: MultiLineEditBox.builder().setPlaceholder(placeholder).setX(x).setY(y + ...).build(font, width, height, label)
            // In 1.20.x: new MultiLineEditBox(font, x, y, width, height, placeholder, label)
            multiLineEditBox = new MultiLineEditBox(
                    font, x, y + stringWidget.getHeight() + 5, width, height, placeholder, Component.literal(""));
        }

        public void setPosition(int x, int y) {
            stringWidget.setPosition(x, y);
            y += stringWidget.getHeight() + 5;
            multiLineEditBox.setPosition(x, y);
        }

        public StringWidget getStringWidget() {
            return stringWidget;
        }

        public MultiLineEditBox getMultiLineEditBox() {
            return multiLineEditBox;
        }
    }

    public enum Align {
        VERTICAL,
        HORIZONTAL
    }
}
