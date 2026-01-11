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

package fr.loudo.narrativecraft.screens.storyManager.interaction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.controllers.interaction.InteractionController;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.Interaction;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesMenuScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * MC 1.20.x version - Uses net.minecraft.Util instead of net.minecraft.util.Util
 */
public class InteractionsScreen extends StoryElementScreen {

    private final Scene scene;

    public InteractionsScreen(Scene scene) {
        super(Translation.message("screen.story_manager.interaction_list", scene.getName()));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<Interaction> screen =
                    new EditInfoScreen<>(this, null, new EditScreenInteractionAdapter(scene));
            this.minecraft.setScreen(screen);
        });
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
        List<StoryElementList.StoryEntryData> entries = scene.getInteractions().stream()
                .map(interaction -> {
                    Button button = Button.builder(Component.literal(interaction.getName()), button1 -> {
                                NarrativeCraftMod.server.execute(() -> new InteractionController(
                                                Environment.DEVELOPMENT, minecraft.player, interaction)
                                        .startSession());
                                minecraft.setScreen(null);
                            })
                            .build();

                    return new StoryElementList.StoryEntryData(
                            button,
                            () -> {
                                minecraft.setScreen(new EditInfoScreen<>(
                                        this, interaction, new EditScreenInteractionAdapter(scene)));
                            },
                            () -> {
                                try {
                                    scene.removeInteraction(interaction);
                                    NarrativeCraftFile.updateInteractionsFile(scene);
                                } catch (Exception e) {
                                    scene.addInteraction(interaction);
                                    fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                                }
                                minecraft.setScreen(new InteractionsScreen(scene));
                            });
                })
                .toList();

        this.storyElementList = new StoryElementList(this.minecraft, this, entries, true);
        this.addWidget(this.storyElementList);
    }

    @Override
    protected void openFolder() {
        // 1.20.x: openFile() instead of openPath()
        Util.getPlatform().openFile(NarrativeCraftFile.getSceneFolder(scene));
    }
}
