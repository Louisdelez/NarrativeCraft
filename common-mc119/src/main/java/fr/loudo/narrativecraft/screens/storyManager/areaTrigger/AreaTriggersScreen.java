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

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.controllers.interaction.InteractionController;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.AreaTrigger;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.controller.areaTrigger.AreaTriggerControllerScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/**
 * MC 1.19.x version of AreaTriggersScreen.
 * Uses manual layout (no HeaderAndFooterLayout in 1.19.x).
 */
public class AreaTriggersScreen extends Screen {

    private final Screen lastScreen;
    private final InteractionController interactionController;
    private StoryElementList storyElementList;

    public AreaTriggersScreen(Screen lastScreen, InteractionController interactionController) {
        super(Translation.message(
                "controller.interaction.area_trigger_list",
                interactionController.getInteraction().getName()));
        this.lastScreen = lastScreen;
        this.interactionController = interactionController;
    }

    @Override
    protected void init() {
        // Add button in header
        int titleWidth = this.font.width(this.title);
        Button addButton = Button.builder(ImageFontConstants.ADD, button -> {
                    EditInfoScreen<AreaTrigger> screen = new EditInfoScreen<>(
                            this, null, new EditScreenAreaTriggerAdapter(lastScreen, interactionController));
                    this.minecraft.setScreen(screen);
                })
                .width(25)
                .pos(this.width / 2 - titleWidth / 2 + titleWidth + 10, 6)
                .build();
        this.addRenderableWidget(addButton);

        // Create content list
        List<StoryElementList.StoryEntryData> entries = interactionController.getAreaTriggers().stream()
                .map(areaTrigger -> {
                    Button button = Button.builder(Component.literal(areaTrigger.getName()), button1 -> {
                                interactionController.setAreaTriggerEditing(areaTrigger);
                                Vec3 oldLoc1 = new Vec3(
                                        areaTrigger.getPosition1().x,
                                        areaTrigger.getPosition1().y,
                                        areaTrigger.getPosition1().z);
                                Vec3 oldLoc2 = new Vec3(
                                        areaTrigger.getPosition2().x,
                                        areaTrigger.getPosition2().y,
                                        areaTrigger.getPosition2().z);
                                AreaTriggerControllerScreen screen = new AreaTriggerControllerScreen(
                                        interactionController, areaTrigger, oldLoc1, oldLoc2);
                                minecraft.setScreen(screen);
                            })
                            .build();

                    return new StoryElementList.StoryEntryData(
                            button,
                            () -> {
                                minecraft.setScreen(new EditInfoScreen<>(
                                        this,
                                        areaTrigger,
                                        new EditScreenAreaTriggerAdapter(lastScreen, interactionController)));
                            },
                            () -> {
                                interactionController.getAreaTriggers().remove(areaTrigger);
                                minecraft.setScreen(new AreaTriggersScreen(lastScreen, interactionController));
                            });
                })
                .toList();

        this.storyElementList = new StoryElementList(this.minecraft, this, entries, true);
        this.addWidget(this.storyElementList);

        // Done button at bottom
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
        this.renderBackground(poseStack);

        // Render list
        if (this.storyElementList != null) {
            this.storyElementList.render(poseStack, mouseX, mouseY, partialTick);
        }

        // Draw title centered at top
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }
}
