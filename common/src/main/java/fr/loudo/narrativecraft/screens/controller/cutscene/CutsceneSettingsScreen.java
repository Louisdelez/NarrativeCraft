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
import fr.loudo.narrativecraft.screens.components.ButtonListScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CutsceneSettingsScreen extends ButtonListScreen {

    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    protected CutsceneController cutsceneController;

    public CutsceneSettingsScreen(CutsceneController cutsceneController, Screen lastScreen, Component title) {
        super(lastScreen, title);
        this.cutsceneController = cutsceneController;
    }

    @Override
    protected void addContents() {
        Button changeTimeSkip = Button.builder(
                        Translation.message("controller.cutscene.settings.time_skip"), button -> {
                            CutsceneChangeTimeSkipScreen screen =
                                    new CutsceneChangeTimeSkipScreen(cutsceneController, this);
                            this.minecraft.setScreen(screen);
                        })
                .build();

        Button selectKeyframeGroup = Button.builder(
                        Translation.message("controller.cutscene.settings.select_group"), button -> {
                            CutsceneSelectKeyframeGroupScreen screen =
                                    new CutsceneSelectKeyframeGroupScreen(cutsceneController, this);
                            this.minecraft.setScreen(screen);
                        })
                .build();

        objectListScreen.addButton(changeTimeSkip);
        objectListScreen.addButton(selectKeyframeGroup);
    }
}
