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

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.chapter.ChaptersScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ScenesScreen extends StoryElementScreen {

    private final Chapter chapter;

    public ScenesScreen(Chapter chapter) {
        super(Translation.message("screen.story_manager.scene_list", chapter.getIndex()));
        this.chapter = chapter;
    }

    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<Scene> screen = new EditInfoScreen<>(this, null, new EditScreenSceneAdapter(chapter));
            this.minecraft.setScreen(screen);
        });
        initFolderButton();
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, p_345997_ -> this.onClose())
                .width(200)
                .build());
    }

    @Override
    public void onClose() {
        ChaptersScreen screen = new ChaptersScreen();
        this.minecraft.setScreen(screen);
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = chapter.getSortedSceneList().stream()
                .map(scene -> {
                    Button button = Button.builder(Component.literal(scene.getName()), b -> {
                                this.minecraft.setScreen(new ScenesMenuScreen(scene));
                            })
                            .build();
                    return new StoryElementList.StoryEntryData(
                            button,
                            () -> {
                                minecraft.setScreen(
                                        new EditInfoScreen<>(this, scene, new EditScreenSceneAdapter(chapter)));
                            },
                            () -> {
                                try {
                                    chapter.removeScene(scene);
                                    NarrativeCraftFile.deleteSceneDirectory(scene);
                                    if (scene.getRank() == 1
                                            && chapter.getSortedSceneList().size() > 1) {
                                        NarrativeCraftFile.updateMasterSceneKnot(
                                                chapter.getSortedSceneList().getFirst());
                                    }
                                    minecraft.setScreen(new ScenesScreen(chapter));
                                } catch (Exception e) {
                                    chapter.addScene(scene);
                                    chapter.setSceneRank(scene, scene.getRank());
                                    fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                                    minecraft.setScreen(null);
                                }
                            });
                })
                .toList();
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    @Override
    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.getScenesFolder(chapter).toPath());
    }
}
