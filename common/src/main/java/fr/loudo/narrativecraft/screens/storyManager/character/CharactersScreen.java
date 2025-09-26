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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.CameraAngle;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.screens.characters.CharacterEntityTypeScreen;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesMenuScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

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
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        List<CharacterStory> characterStories =
                scene == null ? characterManager.getCharacterStories() : scene.getNpcs();

        List<StoryElementList.StoryEntryData> entries = characterStories.stream()
                .map(character -> {
                    Button button = Button.builder(Component.literal(character.getName()), button1 -> {})
                            .build();
                    button.active = false;
                    Button entityTypeButton = Button.builder(ImageFontConstants.ENTITY, button1 -> {
                                CharacterEntityTypeScreen screen = new CharacterEntityTypeScreen(this, character);
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
                                    characterManager.removeCharacter(character);
                                    NarrativeCraftFile.deleteCharacterFolder(character);
                                } else {
                                    scene.removeNpc(character);
                                    NarrativeCraftFile.deleteCharacterFolder(character, scene);
                                }
                                try {
                                    for (Chapter chapter : NarrativeCraftMod.getInstance()
                                            .getChapterManager()
                                            .getChapters()) {
                                        for (Scene scene1 : chapter.getSortedSceneList()) {
                                            for (Animation animation : scene1.getAnimations()) {
                                                if (animation
                                                        .getCharacter()
                                                        .getName()
                                                        .equalsIgnoreCase(character.getName())) {
                                                    animation.setCharacter(null);
                                                    NarrativeCraftFile.updateAnimationFile(animation);
                                                }
                                            }
                                            for (CameraAngle cameraAngle : scene1.getCameraAngles()) {
                                                cameraAngle
                                                        .getCharacterStoryDataList()
                                                        .removeIf(characterStoryData -> characterStoryData
                                                                .getCharacterStory()
                                                                .getName()
                                                                .equalsIgnoreCase(character.getName()));
                                            }
                                            NarrativeCraftFile.updateCameraAngles(scene1);
                                        }
                                    }
                                } catch (Exception e) {
                                    Util.sendCrashMessage(minecraft.player, e);
                                    minecraft.setScreen(null);
                                    return;
                                }
                                minecraft.setScreen(new CharactersScreen(scene));
                            });
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    @Override
    protected void openFolder() {
        if (scene == null) {
            net.minecraft.Util.getPlatform().openPath(NarrativeCraftFile.characterDirectory.toPath());
        } else {
            net.minecraft.Util.getPlatform()
                    .openPath(NarrativeCraftFile.getNpcFolder(scene).toPath());
        }
    }
}
