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
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframeGroup;
import fr.loudo.narrativecraft.screens.components.ButtonListScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class CutsceneSelectKeyframeGroupScreen extends ButtonListScreen {

    protected CutsceneController cutsceneController;

    public CutsceneSelectKeyframeGroupScreen(CutsceneController cutsceneController, Screen lastScreen) {
        super(lastScreen, Translation.message("controller.cutscene.settings.select_group"));
        this.cutsceneController = cutsceneController;
    }

    @Override
    protected void addContents() {
        for (CutsceneKeyframeGroup keyframeGroup : this.cutsceneController.getKeyframeGroups()) {
            Component name;
            if (cutsceneController.getSelectedGroup().getId() == keyframeGroup.getId()) {
                name = Translation.message(
                        "controller.cutscene.settings.select_group_name_selected", keyframeGroup.getId());
            } else {
                name = Translation.message("controller.cutscene.settings.select_group_name", keyframeGroup.getId());
            }
            Button button = Button.builder(name, button1 -> {
                        this.cutsceneController.setSelectedGroup(keyframeGroup);
                        Vec3 keyframeLocation = keyframeGroup
                                .getKeyframes()
                                .getFirst()
                                .getKeyframeLocation()
                                .getPosition();
                        minecraft.player.setPos(keyframeLocation);
                        minecraft.player.displayClientMessage(
                                Translation.message(
                                        "controller.cutscene.settings.group_selected", keyframeGroup.getId()),
                                false);
                        minecraft.setScreen(null);
                    })
                    .build();
            this.objectListScreen.addButton(button);
        }
    }
}
