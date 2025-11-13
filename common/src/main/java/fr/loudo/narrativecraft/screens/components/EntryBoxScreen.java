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

import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Util;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class EntryBoxScreen extends Screen {

    private final Screen lastScreen;
    private final Consumer<String> consumer;
    private final Component label;

    private String defaultValue = "";

    public EntryBoxScreen(Screen lastScreen, Component label, Consumer<String> consumer) {
        super(Component.literal("Entry box"));
        this.lastScreen = lastScreen;
        this.consumer = consumer;
        this.label = label;
    }

    public EntryBoxScreen(Screen lastScreen, Component label, String defaultValue, Consumer<String> consumer) {
        super(Component.literal("Entry box"));
        this.lastScreen = lastScreen;
        this.consumer = consumer;
        this.label = label;
        this.defaultValue = defaultValue;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    @Override
    protected void init() {
        int globalWidth = 150;
        int globalHeight = 20;
        int globalGap = 5;
        ScreenUtils.LabelBox labelBox = new ScreenUtils.LabelBox(
                label,
                minecraft.font,
                globalWidth,
                globalHeight,
                (this.width - globalWidth) / 2,
                (this.height / 2) - (globalHeight / 2) - (globalHeight * 3) / 2,
                ScreenUtils.Align.VERTICAL);
        labelBox.getEditBox().setFilter(text -> text.matches(Util.REGEX_NO_SPECIAL_CHARACTERS));
        labelBox.getEditBox().setValue(defaultValue);
        this.addRenderableWidget(labelBox.getStringWidget());
        this.addRenderableWidget(labelBox.getEditBox());

        Button doneButton = Button.builder(CommonComponents.GUI_DONE, button -> {
                    consumer.accept(labelBox.getEditBox().getValue());
                    onClose();
                })
                .bounds(
                        (width - globalWidth) / 2,
                        labelBox.getEditBox().getY() + labelBox.getEditBox().getHeight() + globalGap,
                        globalWidth,
                        globalHeight)
                .build();
        this.addRenderableWidget(doneButton);

        Button backButton = Button.builder(CommonComponents.GUI_BACK, button -> onClose())
                .bounds(
                        (width - globalWidth) / 2,
                        doneButton.getY() + doneButton.getHeight() + globalGap,
                        globalWidth,
                        globalHeight)
                .build();
        this.addRenderableWidget(backButton);
        minecraft.options.hideGui = false;
    }
}
