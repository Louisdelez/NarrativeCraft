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
import fr.loudo.narrativecraft.narrative.chapter.scene.data.CameraAngle;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
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
            CameraAngle cameraAngleGroup = new CameraAngle(name, description, scene);
            try {
                scene.addCameraAngleGroup(cameraAngleGroup);
                NarrativeCraftFile.updateCameraAngles(scene);
                minecraft.setScreen(new CameraAngleScreen(scene));
            } catch (Exception e) {
                scene.removeCameraAngleGroup(cameraAngleGroup);
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        } else {
            CameraAngle oldCameraAngleGroup = new CameraAngle(existing.getName(), existing.getDescription(), scene);
            try {
                existing.setName(name);
                existing.setDescription(description);
                NarrativeCraftFile.updateCameraAngles(scene);
                minecraft.setScreen(new CameraAngleScreen(scene));
            } catch (Exception e) {
                existing.setName(oldCameraAngleGroup.getName());
                existing.setDescription(oldCameraAngleGroup.getDescription());
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        }
    }
}
