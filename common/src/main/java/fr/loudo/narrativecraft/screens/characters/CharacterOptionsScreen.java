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

package fr.loudo.narrativecraft.screens.characters;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.controllers.keyframe.AbstractKeyframeController;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import fr.loudo.narrativecraft.screens.components.ButtonListScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class CharacterOptionsScreen extends ButtonListScreen {

    private final AbstractKeyframeController<? extends Keyframe> controller;
    private final CharacterStoryData characterStoryData;

    public CharacterOptionsScreen(
            Screen lastScreen,
            AbstractKeyframeController<? extends Keyframe> controller,
            CharacterStoryData characterStoryData) {
        super(lastScreen, Component.literal("Character options screen"));
        this.controller = controller;
        this.characterStoryData = characterStoryData;
    }

    @Override
    protected void addContents() {
        Button changeCharacterPoseButton = Button.builder(Translation.message("character.change_pose"), button -> {
                    CharacterChangePoseScreen screen = new CharacterChangePoseScreen(this, characterStoryData);
                    minecraft.setScreen(screen);
                })
                .build();
        objectListScreen.addButton(changeCharacterPoseButton);

        Button removeCharacterButton = Button.builder(Translation.message("global.remove"), button -> {
                    ConfirmScreen confirm = new ConfirmScreen(
                            b -> {
                                if (b) {
                                    NarrativeCraftMod.server.execute(() -> {
                                        characterStoryData.kill();
                                        controller.getCharacterStoryDataList().remove(characterStoryData);
                                    });
                                }
                                minecraft.setScreen(null);
                            },
                            Component.literal(""),
                            Translation.message("global.confirm_delete"),
                            CommonComponents.GUI_YES,
                            CommonComponents.GUI_CANCEL);
                    minecraft.setScreen(confirm);
                })
                .build();
        objectListScreen.addButton(removeCharacterButton);
    }
}
