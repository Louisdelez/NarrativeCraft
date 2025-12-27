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

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.Interaction;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.io.IOException;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class EditScreenInteractionAdapter implements EditScreenAdapter<Interaction> {

    private final Scene scene;

    public EditScreenInteractionAdapter(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void initExtraFields(EditInfoScreen<Interaction> screen, Interaction entry) {}

    @Override
    public void renderExtraFields(EditInfoScreen<Interaction> screen, Interaction entry, int x, int y) {}

    @Override
    public void buildFromScreen(
            Screen screen,
            Map<String, Object> extraFields,
            Minecraft minecraft,
            @Nullable Interaction existing,
            String name,
            String description) {
        if (scene.interactionExists(name)) {
            ScreenUtils.sendToast(
                    Translation.message("global.error"),
                    Translation.message("interaction.already_exists", name, scene.getName()));
            return;
        }
        if (existing == null) {
            Interaction interaction = new Interaction(name, description, scene);
            try {
                scene.addInteraction(interaction);
                NarrativeCraftFile.updateInteractionsFile(scene);
                minecraft.setScreen(new InteractionsScreen(scene));
            } catch (IOException e) {
                scene.removeInteraction(interaction);
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        } else {
            Interaction oldInteraction = new Interaction(existing.getName(), existing.getDescription(), scene);

            try {
                existing.setName(name);
                existing.setDescription(description);
                NarrativeCraftFile.updateInteractionsFile(scene);
                minecraft.setScreen(new InteractionsScreen(scene));
            } catch (Exception e) {
                existing.setName(oldInteraction.getName());
                existing.setDescription(oldInteraction.getDescription());
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        }
    }
}
