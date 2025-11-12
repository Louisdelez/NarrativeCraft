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

import fr.loudo.narrativecraft.controllers.AbstractController;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.CharacterInteraction;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.screens.characters.CharacterOptionsScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

public class CharacterInteractionOptionsScreen extends CharacterOptionsScreen {

    private final CharacterInteraction characterInteraction;

    public CharacterInteractionOptionsScreen(
            Screen lastScreen,
            AbstractController controller,
            CharacterRuntime characterRuntime,
            CharacterInteraction characterInteraction) {
        super(lastScreen, controller, characterRuntime);
        this.characterInteraction = characterInteraction;
    }

    @Override
    protected void addContents() {
        super.addContents();
        Button changeStitchBtn = Button.builder(Translation.message("controller.interaction.change_stitch"), button -> {
                    EntryBoxScreen screen = new EntryBoxScreen(
                            this,
                            Translation.message("global.stitch"),
                            characterInteraction.getStitch(),
                            characterInteraction::setStitch);
                    minecraft.setScreen(screen);
                })
                .build();
        objectListScreen.addButton(changeStitchBtn);
    }
}
