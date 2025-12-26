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

package fr.loudo.narrativecraft.screens.storyManager.chapter;

import fr.loudo.narrativecraft.client.NarrativeCraftModClient;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public class ChaptersScreen extends StoryElementScreen {

    public ChaptersScreen() {
        super(Translation.message("screen.story_manager.chapter_list"));
    }

    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<Chapter> screen = new EditInfoScreen<>(this, null, new EditScreenChapterAdapter());
            this.minecraft.setScreen(screen);
        });
        initFolderButton();
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (p_345997_) -> this.onClose())
                .width(200)
                .build());
    }

    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.chaptersDirectory.toPath());
    }

    @Override
    protected void addContents() {
        ChapterManager chapterManager = NarrativeCraftModClient.getInstance().getChapterManager();

        List<StoryElementList.StoryEntryData> entries = chapterManager.getChapters().stream()
                .map(chapter -> {
                    String label = String.valueOf(chapter.getIndex());
                    if (!chapter.getName().isEmpty()) {
                        label += " - " + chapter.getName();
                    }

                    Button button = Button.builder(Component.literal(label), b -> {
                                this.minecraft.setScreen(new ScenesScreen(chapter));
                            })
                            .build();

                    return new StoryElementList.StoryEntryData(
                            button,
                            () -> {
                                minecraft.setScreen(
                                        new EditInfoScreen<>(this, chapter, new EditScreenChapterAdapter()));
                            },
                            () -> {
                                try {
                                    chapterManager.removeChapter(chapter);
                                    NarrativeCraftFile.deleteChapterDirectory(chapter);
                                    minecraft.setScreen(new ChaptersScreen());
                                } catch (Exception e) {
                                    chapterManager.addChapter(chapter);
                                    fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                                }
                            });
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }
}
