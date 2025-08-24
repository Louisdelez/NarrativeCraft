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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class EditScreenChapterAdapter implements EditScreenAdapter<Chapter> {
    @Override
    public void initExtraFields(EditInfoScreen<Chapter> screen, Chapter entry) {}

    @Override
    public void renderExtraFields(EditInfoScreen<Chapter> screen, Chapter entry, int startY, int centerX) {}

    @Override
    public void buildFromScreen(
            Map<String, Object> extraFields,
            Minecraft minecraft,
            @Nullable Chapter existing,
            String name,
            String description) {

        if (existing == null) {
            ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
            if (chapterManager.chapterExists(name)) {
                ScreenUtils.sendToast(
                        Translation.message("global.error"), Translation.message("chapter.already_exists", name));
                return;
            }
            try {
                Chapter chapter = new Chapter(
                        name, description, chapterManager.getChapters().size() + 1);
                chapterManager.addChapter(chapter);
                NarrativeCraftFile.createChapterDirectory(chapter);
                minecraft.setScreen(new ChaptersScreen());
            } catch (Exception e) {
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        } else {
            Chapter newChapter = new Chapter(name, description, existing.getIndex());
            Chapter oldChapter = new Chapter(existing.getName(), existing.getDescription(), existing.getIndex());
            try {
                NarrativeCraftFile.updateChapterData(newChapter);

                existing.setName(name);
                existing.setDescription(description);
                NarrativeCraftFile.updateInkIncludes();
                minecraft.setScreen(new ChaptersScreen());
            } catch (Exception e) {
                existing.setName(oldChapter.getName());
                existing.setDescription(oldChapter.getDescription());
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        }
    }
}
