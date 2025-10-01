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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChooseCharacterScreen extends GenericSelectionScreen<CharacterStory> {
    private final CharacterType characterType;
    private final Scene scene;

    public ChooseCharacterScreen(
            Screen lastScreen,
            String screenTitle,
            List<CharacterStory> itemList,
            CharacterType characterType,
            CharacterStory currentSelection,
            Scene scene,
            Consumer<CharacterStory> consumer) {
        super(lastScreen, screenTitle, itemList, currentSelection, consumer);
        this.characterType = characterType;
        this.scene = scene;
    }

    public ChooseCharacterScreen(
            Screen lastScreen,
            String screenTitle,
            CharacterStory currentSelection,
            Scene scene,
            Consumer<CharacterStory> consumer) {
        super(
                lastScreen,
                screenTitle,
                NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories(),
                currentSelection,
                consumer);
        this.characterType = CharacterType.MAIN;
        this.scene = scene;
    }

    @Override
    protected void addCustomTitleButtons(GridLayout.RowHelper rowHelper) {
        if (scene == null) return;
        rowHelper.addChild(Button.builder(
                        characterType == CharacterType.NPC ? Component.literal("<- MAIN") : Component.literal("NPC ->"),
                        button -> {
                            Screen screen = new ChooseCharacterScreen(
                                    lastScreen,
                                    screenTitle,
                                    characterType == CharacterType.MAIN
                                            ? scene.getNpcs()
                                            : NarrativeCraftMod.getInstance()
                                                    .getCharacterManager()
                                                    .getCharacterStories(),
                                    characterType == CharacterType.MAIN ? CharacterType.NPC : CharacterType.MAIN,
                                    null,
                                    scene,
                                    consumer);
                            minecraft.setScreen(screen);
                        })
                .width(40)
                .build());
    }
}
