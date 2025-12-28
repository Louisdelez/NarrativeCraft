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

package fr.loudo.narrativecraft.screens.storyManager.character;

import fr.loudo.narrativecraft.client.NarrativeCraftModClient;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.network.data.BiCharacterDataPacket;
import fr.loudo.narrativecraft.network.data.BiNpcDataPacket;
import fr.loudo.narrativecraft.network.data.TypeStoryData;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.screens.characters.CharacterEntityTypeScreen;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesMenuScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public class CharactersScreen extends StoryElementScreen {

    private final Scene scene;

    public CharactersScreen(Scene scene) {
        super(
                scene == null
                        ? Translation.message("screen.story_manager.characters")
                        : Translation.message("screen.story_manager.npcs", scene.getName()));
        this.scene = scene;
    }

    @Override
    public void onClose() {
        if (scene != null) {
            minecraft.setScreen(new ScenesMenuScreen(scene));
        } else {
            super.onClose();
        }
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<CharacterStory> screen =
                    new EditInfoScreen<>(this, null, new EditScreenCharacterAdapter(scene));
            this.minecraft.setScreen(screen);
        });
        initFolderButton();
    }

    @Override
    protected void addContents() {
        CharacterManager characterManager =
                NarrativeCraftModClient.getInstance().getCharacterManager();
        List<CharacterStory> characterStories = scene == null
                ? characterManager.getCharacterStories().stream()
                        .filter(characterStory ->
                                !characterStory.getMainCharacterAttribute().isMainCharacter())
                        .collect(Collectors.toList())
                : scene.getNpcs();
        CharacterStory mainCharacter = characterManager.getMainCharacter();
        if (mainCharacter != null && scene == null) {
            characterStories.addFirst(mainCharacter);
        }

        List<StoryElementList.StoryEntryData> entries = characterStories.stream()
                .map(character -> {
                    String name = character.getName();
                    if (character.getMainCharacterAttribute().isMainCharacter()) {
                        name += " (" + Translation.message("global.main").getString() + ")";
                    }
                    Button button = Button.builder(Component.literal(name), button1 -> {})
                            .build();
                    button.active = false;
                    Button entityTypeButton = Button.builder(ImageFontConstants.ENTITY, button1 -> {
                                CharacterEntityTypeScreen screen =
                                        new CharacterEntityTypeScreen(this, character, scene);
                                minecraft.setScreen(screen);
                            })
                            .build();
                    entityTypeButton.setTooltip(Tooltip.create(Translation.message("tooltip.character_entity_type")));

                    return new StoryElementList.StoryEntryData(
                            button,
                            List.of(entityTypeButton),
                            () -> {
                                minecraft.setScreen(
                                        new EditInfoScreen<>(this, character, new EditScreenCharacterAdapter(scene)));
                            },
                            () -> {
                                if (scene == null) {
                                    Services.PACKET_SENDER.sendToServer(new BiCharacterDataPacket(
                                            character.getName(),
                                            character.getDescription(),
                                            character.getModel(),
                                            "",
                                            "",
                                            "",
                                            character.showNametag(),
                                            character
                                                    .getMainCharacterAttribute()
                                                    .isMainCharacter(),
                                            character.getMainCharacterAttribute().isSameSkinAsPlayer(),
                                            character.getMainCharacterAttribute().isSameSkinAsTheir(),
                                            character.getName(),
                                            TypeStoryData.REMOVE));
                                } else {
                                    Services.PACKET_SENDER.sendToServer(new BiNpcDataPacket(
                                            character.getName(),
                                            character.getDescription(),
                                            character.getModel(),
                                            character.showNametag(),
                                            scene.getChapter().getIndex(),
                                            scene.getName(),
                                            character.getName(),
                                            TypeStoryData.REMOVE));
                                }
                            });
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    @Override
    protected void openFolder() {
        if (scene == null) {
            Util.getPlatform().openPath(NarrativeCraftFile.characterDirectory.toPath());
        } else {
            Util.getPlatform()
                    .openPath(NarrativeCraftFile.getNpcFolder(scene).toPath());
        }
    }
}
