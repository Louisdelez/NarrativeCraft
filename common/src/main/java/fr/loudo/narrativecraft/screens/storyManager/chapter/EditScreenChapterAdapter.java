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
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.network.data.BiChapterDataPacket;
import fr.loudo.narrativecraft.network.data.TypeStoryData;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class EditScreenChapterAdapter implements EditScreenAdapter<Chapter> {
    @Override
    public void initExtraFields(EditInfoScreen<Chapter> screen, Chapter entry) {}

    @Override
    public void renderExtraFields(EditInfoScreen<Chapter> screen, Chapter entry, int startY, int centerX) {}

    @Override
    public void buildFromScreen(
            Screen screen,
            Map<String, Object> extraFields,
            Minecraft minecraft,
            @Nullable Chapter existing,
            String name,
            String description) {

        ChapterManager chapterManager = NarrativeCraftModClient.getInstance().getChapterManager();
        if (chapterManager.chapterExists(name)) {
            ScreenUtils.sendToast(
                    Translation.message("global.error"), Translation.message("chapter.already_exists", name));
            return;
        }
        if (existing == null) {
            Services.PACKET_SENDER.sendToServer(new BiChapterDataPacket(name, description, "", TypeStoryData.ADD));
        } else {
            Services.PACKET_SENDER.sendToServer(
                    new BiChapterDataPacket(name, description, existing.getName(), TypeStoryData.EDIT));
        }
    }
}
