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

package fr.loudo.narrativecraft.screens.storyManager.cameraAngle;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.cameraAngle.CameraAngleGroup;
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

public class CameraAngleScreen extends StoryElementScreen {

    private final Scene scene;

    public CameraAngleScreen(Scene scene) {
        super(Translation.message("screen.story_manager.camera_angle_list", scene.getName()));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<CameraAngleGroup> screen =
                    new EditInfoScreen<>(this, null, new EditScreenCameraAngleAdapter(scene));
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
        minecraft.setScreen(new ScenesMenuScreen(scene));
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = scene.getCameraAngleGroups().stream()
                .map(cameraAngleGroup -> {
                    Button button = Button.builder(Component.literal(cameraAngleGroup.getName()), button1 -> {})
                            .build();
                    button.active = false;

                    return new StoryElementList.StoryEntryData(
                            button,
                            () -> {
                                minecraft.setScreen(new EditInfoScreen<>(
                                        this, cameraAngleGroup, new EditScreenCameraAngleAdapter(scene)));
                            },
                            () -> {
                                minecraft.setScreen(new CameraAngleScreen(scene));
                                try {
                                    scene.removeCameraAngleGroup(cameraAngleGroup);
                                    NarrativeCraftFile.updateCameraAngleGroup(scene);
                                    minecraft.setScreen(new CameraAngleScreen(scene));
                                } catch (Exception e) {
                                    scene.addCameraAngleGroup(cameraAngleGroup);
                                    fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                                }
                            });
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    @Override
    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.getSceneFolder(scene).toPath());
    }
}
