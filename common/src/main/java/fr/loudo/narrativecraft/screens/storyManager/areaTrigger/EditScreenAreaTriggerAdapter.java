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

package fr.loudo.narrativecraft.screens.storyManager.areaTrigger;

import fr.loudo.narrativecraft.controllers.interaction.InteractionController;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.AreaTrigger;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.Interaction;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class EditScreenAreaTriggerAdapter implements EditScreenAdapter<AreaTrigger> {

    private final Screen lastScreen;
    private final Interaction interaction;
    private final InteractionController interactionController;

    public EditScreenAreaTriggerAdapter(Screen lastScreen, InteractionController interactionController) {
        this.lastScreen = lastScreen;
        this.interaction = interactionController.getInteraction();
        this.interactionController = interactionController;
    }

    @Override
    public void initExtraFields(EditInfoScreen<AreaTrigger> screen, AreaTrigger entry) {
        ScreenUtils.LabelBox stitch = new ScreenUtils.LabelBox(
                Translation.message("global.stitch"), minecraft.font, 110, 20, 0, 0, ScreenUtils.Align.HORIZONTAL);
        screen.extraFields.put("stitchBox", stitch.getEditBox());
        screen.extraFields.put("stitch", stitch);
        Checkbox isUniqueBox = new Checkbox(0, 0, 20, 20,
                        Translation.message("screen.story_manager.area_trigger.is_unique"), entry != null && entry.isUnique());
        isUniqueBox.setTooltip(Tooltip.create(Translation.message("tooltip.area_trigger.is_unique_explanation")));
        screen.extraFields.put("uniqueBox", isUniqueBox);
    }

    @Override
    public void renderExtraFields(EditInfoScreen<AreaTrigger> screen, AreaTrigger entry, int x, int y) {
        ScreenUtils.LabelBox stitch = (ScreenUtils.LabelBox) screen.extraFields.get("stitch");
        screen.addRenderableWidget(stitch.getStringWidget());
        screen.addRenderableWidget(stitch.getEditBox());
        stitch.setPosition(x, y);
        if (entry != null) {
            stitch.getEditBox().setValue(entry.getStitch());
        }
        y += stitch.getEditBox().getHeight() + screen.GAP;
        Checkbox isUniqueBox = (Checkbox) screen.extraFields.get("uniqueBox");
        screen.addRenderableWidget(isUniqueBox);
        isUniqueBox.setPosition(x, y);
    }

    @Override
    public void buildFromScreen(
            Screen screen,
            Map<String, Object> extraFields,
            Minecraft minecraft,
            @Nullable AreaTrigger existing,
            String name,
            String description) {
        ScreenUtils.LabelBox stitch = (ScreenUtils.LabelBox) extraFields.get("stitch");
        Checkbox isUniqueBox = (Checkbox) extraFields.get("uniqueBox");
        if (existing == null) {
            if (interactionController.areaTriggerExists(name)) {
                ScreenUtils.sendToast(
                        Translation.message("global.error"),
                        Translation.message("interaction.area_trigger.already_exists", name, interaction.getName()));
                return;
            }
            AreaTrigger areaTrigger = new AreaTrigger(
                    name,
                    description,
                    interaction.getScene(),
                    stitch.getEditBox().getValue(),
                    isUniqueBox.selected());
            interactionController.getAreaTriggers().add(areaTrigger);
        } else {
            existing.setName(name);
            existing.setDescription(description);
            existing.setStitch(stitch.getEditBox().getValue());
            existing.setUnique(isUniqueBox.selected());
        }
        minecraft.setScreen(new AreaTriggersScreen(lastScreen, interactionController));
    }
}
