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

package fr.loudo.narrativecraft.screens.controller.cutscene;

import fr.loudo.narrativecraft.controllers.cutscene.CutsceneController;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CutsceneChangeTimeSkipScreen extends Screen {

    private final int INPUT_WIDTH = 100;
    private final int BUTTON_WIDTH = 60;
    private final int BUTTON_HEIGHT = 20;

    private final Screen lastScreen;
    private final CutsceneController cutsceneController;
    private EditBox numberInput;

    public CutsceneChangeTimeSkipScreen(CutsceneController cutsceneController, Screen lastScreen) {
        super(Component.literal("Change Time Skip Screen"));
        this.cutsceneController = cutsceneController;
        this.lastScreen = lastScreen;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int inputX = centerX - (INPUT_WIDTH + 5 + BUTTON_WIDTH) / 2;

        numberInput = new EditBox(
                this.font, inputX, centerY - 20, INPUT_WIDTH + 20, BUTTON_HEIGHT, Component.literal("Number"));
        numberInput.setFilter(s -> s.matches("^\\d*(\\.\\d*)?$"));
        numberInput.setMaxLength(10);
        this.addRenderableWidget(numberInput);

        int updateX = inputX + INPUT_WIDTH + 30;
        Button updateButton = Button.builder(Translation.message("global.update"), button -> {
                    String input = numberInput.getValue();
                    if (!input.isEmpty()) {
                        double value = Double.parseDouble(input);
                        cutsceneController.setSkipTickCount((int) (value * 20));
                        Minecraft.getInstance()
                                .player
                                .displayClientMessage(
                                        Translation.message("controller.cutscene.changed_time_skip_value", value),
                                        false);
                        minecraft.setScreen(null);
                    }
                })
                .bounds(updateX, centerY - 20, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(updateButton);

        double[] values = {0.5, 1, 5, 10, 15, 60};
        int buttonCount = values.length;
        int totalTopWidth = INPUT_WIDTH + 5 + BUTTON_WIDTH;
        int buttonWidth = 30;

        int totalButtonsWidth = buttonCount * buttonWidth;
        int spacing = (totalTopWidth - totalButtonsWidth) / (buttonCount - 1);

        for (int i = 0; i < buttonCount; i++) {
            double val = values[i];
            int x = inputX + i * (buttonWidth + spacing + 5);
            Button b = Button.builder(Component.literal(String.valueOf(val)), button -> {
                        numberInput.setValue(String.valueOf(val));
                    })
                    .bounds(x, centerY + 10, buttonWidth, BUTTON_HEIGHT)
                    .build();
            this.addRenderableWidget(b);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
