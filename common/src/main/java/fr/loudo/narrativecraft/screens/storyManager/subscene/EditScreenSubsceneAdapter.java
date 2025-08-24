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

package fr.loudo.narrativecraft.screens.storyManager.subscene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class EditScreenSubsceneAdapter implements EditScreenAdapter<Subscene> {

    private final Scene scene;

    public EditScreenSubsceneAdapter(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void initExtraFields(EditInfoScreen<Subscene> screen, Subscene entry) {}

    @Override
    public void renderExtraFields(EditInfoScreen<Subscene> screen, Subscene entry, int x, int y) {}

    @Override
    public void buildFromScreen(
            Map<String, Object> extraFields,
            Minecraft minecraft,
            @Nullable Subscene existing,
            String name,
            String description) {
        if (existing == null) {
            if (scene.subsceneExists(name)) {
                ScreenUtils.sendToast(
                        Translation.message("global.error"),
                        Translation.message("subscene.already_exists", name, scene.getName()));
                return;
            }
            Subscene subscene = new Subscene(name, description, scene);
            try {
                scene.addSubscene(subscene);
                NarrativeCraftFile.updateSubsceneFile(scene);
                minecraft.setScreen(new SubscenesScreen(scene));
            } catch (Exception e) {
                scene.removeSubscene(subscene);
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        } else {
            Subscene oldSubscene = new Subscene(existing.getName(), existing.getDescription(), scene);
            try {
                existing.setName(name);
                existing.setDescription(description);
                NarrativeCraftFile.updateSubsceneFile(scene);
                minecraft.setScreen(new SubscenesScreen(scene));
            } catch (Exception e) {
                existing.setName(oldSubscene.getName());
                existing.setDescription(oldSubscene.getDescription());
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        }
    }
}
