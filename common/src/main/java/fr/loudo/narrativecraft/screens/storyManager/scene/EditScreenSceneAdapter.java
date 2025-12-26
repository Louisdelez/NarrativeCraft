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
import fr.loudo.narrativecraft.network.data.BiSceneDataPacket;
import fr.loudo.narrativecraft.network.data.TypeStoryData;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class EditScreenSceneAdapter implements EditScreenAdapter<Scene> {

    private final Chapter chapter;

    public EditScreenSceneAdapter(Chapter chapter) {
        this.chapter = chapter;
    }

    @Override
    public void initExtraFields(EditInfoScreen<Scene> screen, Scene entry) {
        if (entry == null) return;
        ScreenUtils.LabelBox rankBox = new ScreenUtils.LabelBox(
                Translation.message("scene.rank"),
                screen.getFont(),
                40,
                screen.EDIT_BOX_NAME_HEIGHT,
                0,
                0,
                ScreenUtils.Align.HORIZONTAL);
        rankBox.getEditBox().setFilter(string -> string.matches(Util.REGEX_INT));
        rankBox.getEditBox().setValue(String.valueOf(entry.getRank()));
        screen.extraFields.putIfAbsent("rank", rankBox);
        screen.extraFields.putIfAbsent("rankEditBox", rankBox.getEditBox());
    }

    @Override
    public void renderExtraFields(EditInfoScreen<Scene> screen, Scene entry, int x, int y) {
        if (entry == null) return;
        ScreenUtils.LabelBox labelBox = (ScreenUtils.LabelBox) screen.extraFields.get("rank");
        labelBox.setPosition(x, y);
        screen.addRenderableWidget(labelBox.getStringWidget());
        screen.addRenderableWidget(labelBox.getEditBox());
    }

    @Override
    public void buildFromScreen(
            Screen screen,
            Map<String, Object> extraFields,
            Minecraft minecraft,
            @Nullable Scene existing,
            String name,
            String description) {

        if (existing == null) {
            if (chapter.sceneExists(name)) {
                ScreenUtils.sendToast(
                        Translation.message("global.error"),
                        Translation.message("scene.already_exists", name, chapter.getIndex()));
                return;
            }
            Services.PACKET_SENDER.sendToServer(
                    new BiSceneDataPacket(name, description, chapter.getIndex(), TypeStoryData.ADD));
        } else {
            Scene newScene = new Scene(name, description, chapter);
            newScene.setRank(existing.getRank());
            Scene oldScene = new Scene(existing.getName(), existing.getDescription(), chapter);
            oldScene.setRank(existing.getRank());
            try {
                ScreenUtils.LabelBox labelBox = (ScreenUtils.LabelBox) extraFields.get("rank");
                EditBox editBox = labelBox.getEditBox();
                int rank = 1;
                if (!editBox.getValue().isEmpty()) {
                    rank = Integer.parseInt(editBox.getValue());
                }
                if (rank > chapter.getScenes().size()) {
                    ScreenUtils.sendToast(
                            Translation.message("global.error"), Translation.message("scene.rank_above_scenes_size"));
                    return;
                } else if (rank < 1) {
                    ScreenUtils.sendToast(
                            Translation.message("global.error"), Translation.message("scene.rank_no_under_one"));
                    return;
                }
                NarrativeCraftFile.updateSceneData(oldScene, newScene);
                existing.setName(name);
                existing.setDescription(description);
                NarrativeCraftFile.updateSceneNameScript(oldScene, newScene);
                if (existing.getRank() != rank) {
                    chapter.setSceneRank(existing, rank);
                    NarrativeCraftFile.updateSceneRankData(chapter);
                }
                NarrativeCraftFile.updateMasterSceneKnot(existing);
                NarrativeCraftFile.updateInkIncludes();
                minecraft.setScreen(new ScenesScreen(chapter));
            } catch (Exception e) {
                existing.setName(oldScene.getName());
                existing.setDescription(oldScene.getDescription());
                chapter.setSceneRank(existing, oldScene.getRank());
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        }
    }
}
