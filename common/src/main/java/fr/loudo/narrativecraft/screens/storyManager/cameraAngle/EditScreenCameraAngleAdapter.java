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

import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.CameraAngle;
import fr.loudo.narrativecraft.network.data.BiCameraAngleDataPacket;
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

public class EditScreenCameraAngleAdapter implements EditScreenAdapter<CameraAngle> {

    private final Scene scene;

    public EditScreenCameraAngleAdapter(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void initExtraFields(EditInfoScreen<CameraAngle> screen, CameraAngle entry) {}

    @Override
    public void renderExtraFields(EditInfoScreen<CameraAngle> screen, CameraAngle entry, int x, int y) {}

    @Override
    public void buildFromScreen(
            Screen screen,
            Map<String, Object> extraFields,
            Minecraft minecraft,
            @Nullable CameraAngle existing,
            String name,
            String description) {
        if (existing == null) {
            if (scene.cameraAngleExists(name)) {
                ScreenUtils.sendToast(
                        Translation.message("global.error"),
                        Translation.message("camera_angle.already_exists", name, scene.getName()));
                return;
            }
            Services.PACKET_SENDER.sendToServer(new BiCameraAngleDataPacket(
                    name, description, scene.getChapter().getIndex(), scene.getName(), "", TypeStoryData.ADD));
        } else {
            Services.PACKET_SENDER.sendToServer(new BiCameraAngleDataPacket(
                    name,
                    description,
                    scene.getChapter().getIndex(),
                    scene.getName(),
                    existing.getName(),
                    TypeStoryData.EDIT));
        }
    }
}
