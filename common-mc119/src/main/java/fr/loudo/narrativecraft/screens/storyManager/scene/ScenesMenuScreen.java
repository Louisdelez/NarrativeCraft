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

package fr.loudo.narrativecraft.screens.storyManager.scene;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.animations.AnimationsScreen;
import fr.loudo.narrativecraft.screens.storyManager.cameraAngle.CameraAngleScreen;
import fr.loudo.narrativecraft.screens.storyManager.character.CharactersScreen;
import fr.loudo.narrativecraft.screens.storyManager.cutscene.CutscenesScreen;
import fr.loudo.narrativecraft.screens.storyManager.interaction.InteractionsScreen;
import fr.loudo.narrativecraft.screens.storyManager.subscene.SubscenesScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;

/**
 * MC 1.20.x version - Uses net.minecraft.Util instead of net.minecraft.util.Util
 */
public class ScenesMenuScreen extends StoryElementScreen {

    private final Scene scene;

    public ScenesMenuScreen(Scene scene) {
        super(Translation.message("screen.story_manager.scene_menu", scene.getName()));
        this.scene = scene;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new ScenesScreen(scene.getChapter()));
    }

    protected void addTitle() {
        super.addTitle();
        initFolderButton();
    }

    @Override
    protected void addFooter() {
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(minecraft.player);
        if (playerSession.isSessionSet() && playerSession.getScene().equals(scene)) {
            // Two buttons side by side: Back and Done
            int buttonWidth = 100;
            int spacing = 8;
            int totalWidth = buttonWidth * 2 + spacing;
            int startX = this.width / 2 - totalWidth / 2;

            Button backButton = Button.builder(CommonComponents.GUI_BACK, (p_345997_) -> {
                        ScenesScreen screen = new ScenesScreen(scene.getChapter());
                        this.minecraft.setScreen(screen);
                    })
                    .width(buttonWidth)
                    .pos(startX, this.height - 28)
                    .build();
            this.addRenderableWidget(backButton);

            this.doneButton = Button.builder(CommonComponents.GUI_DONE, (p_345997_) -> minecraft.setScreen(null))
                    .width(buttonWidth)
                    .pos(startX + buttonWidth + spacing, this.height - 28)
                    .build();
            this.addRenderableWidget(this.doneButton);
        } else {
            // Single back button
            this.doneButton = Button.builder(CommonComponents.GUI_BACK, (p_345997_) -> this.onClose())
                    .width(200)
                    .pos(this.width / 2 - 100, this.height - 28)
                    .build();
            this.addRenderableWidget(this.doneButton);
        }
    }

    @Override
    protected void addContents() {
        StoryElementList.StoryEntryData animation =
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.animation"), button -> {
                            minecraft.setScreen(new AnimationsScreen(scene));
                        })
                        .build());
        StoryElementList.StoryEntryData cameraAngle = new StoryElementList.StoryEntryData(
                Button.builder(Translation.message("global.camera_angle"), button -> {
                            minecraft.setScreen(new CameraAngleScreen(scene));
                        })
                        .build());
        StoryElementList.StoryEntryData cutscene =
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.cutscene"), button -> {
                            minecraft.setScreen(new CutscenesScreen(scene));
                        })
                        .build());
        Button interactionBtn = Button.builder(Translation.message("global.interaction"), button -> {
                    minecraft.setScreen(new InteractionsScreen(scene));
                })
                .build();
        StoryElementList.StoryEntryData interaction = new StoryElementList.StoryEntryData(interactionBtn);
        StoryElementList.StoryEntryData npc = new StoryElementList.StoryEntryData(Button.builder(
                        Translation.message("global.npc"), button -> minecraft.setScreen(new CharactersScreen(scene)))
                .build());

        StoryElementList.StoryEntryData subscene =
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.subscene"), button -> {
                            minecraft.setScreen(new SubscenesScreen(scene));
                        })
                        .build());
        List<StoryElementList.StoryEntryData> entries =
                List.of(animation, cameraAngle, cutscene, interaction, npc, subscene);
        this.storyElementList = new StoryElementList(this.minecraft, this, entries, true);
        this.addWidget(this.storyElementList);
    }

    @Override
    protected void openFolder() {
        // 1.20.x: openFile() instead of openPath()
        Util.getPlatform().openFile(NarrativeCraftFile.getSceneFolder(scene));
    }
}
