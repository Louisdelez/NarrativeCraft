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

package fr.loudo.narrativecraft.screens.storyManager.animations;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.screens.components.ChooseCharacterScreen;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesMenuScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import java.io.IOException;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * MC 1.20.x version - Uses net.minecraft.Util instead of net.minecraft.util.Util
 */
public class AnimationsScreen extends StoryElementScreen {

    private final Scene scene;

    public AnimationsScreen(Scene scene) {
        super(Translation.message("screen.story_manager.animation_list", scene.getName()));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initFolderButton();
    }

    @Override
    protected void addFooter() {
        this.doneButton = Button.builder(CommonComponents.GUI_BACK, p_345997_ -> this.onClose())
                .width(200)
                .pos(this.width / 2 - 100, this.height - 28)
                .build();
        this.addRenderableWidget(this.doneButton);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new ScenesMenuScreen(scene));
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = scene.getAnimations().stream()
                .map(animation -> {
                    Button button = Button.builder(Component.literal(animation.getName()), button1 -> {})
                            .build();
                    button.active = false;
                    return new StoryElementList.StoryEntryData(
                            button,
                            List.of(createSettingsButton(animation)),
                            () -> {
                                minecraft.setScreen(
                                        new EditInfoScreen<>(this, animation, new EditScreenAnimationAdapter(scene)));
                            },
                            () -> {
                                minecraft.setScreen(new AnimationsScreen(scene));
                                scene.removeAnimation(animation);
                                NarrativeCraftFile.deleteAnimationFile(animation);
                                minecraft.setScreen(new AnimationsScreen(scene));
                            });
                })
                .toList();

        this.storyElementList = new StoryElementList(this.minecraft, this, entries, true);
        this.addWidget(this.storyElementList);
    }

    private Button createSettingsButton(Animation animation) {

        return Button.builder(ImageFontConstants.SETTINGS, button1 -> {
                    ChooseCharacterScreen screen = new ChooseCharacterScreen(
                            this,
                            Translation.message("screen.story_manager.link_animation_character")
                                    .getString(),
                            animation.getCharacter(),
                            scene,
                            characterStory -> {
                                if (characterStory == null) {
                                    ScreenUtils.sendToast(
                                            Translation.message("global.error"),
                                            Translation.message("character.must_link_character"));
                                    return;
                                }
                                CharacterStory oldCharacter = animation.getCharacter();
                                try {
                                    animation.setCharacter(characterStory);
                                    NarrativeCraftFile.updateAnimationFile(animation);
                                } catch (IOException e) {
                                    fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                                    animation.setCharacter(oldCharacter);
                                }
                            });
                    this.minecraft.setScreen(screen);
                })
                .build();
    }

    @Override
    protected void openFolder() {
        // 1.20.x: openFile() instead of openPath()
        Util.getPlatform().openFile(NarrativeCraftFile.getAnimationsFolder(scene));
    }
}
