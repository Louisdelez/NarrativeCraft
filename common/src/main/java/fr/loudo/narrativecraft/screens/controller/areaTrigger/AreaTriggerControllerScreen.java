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

package fr.loudo.narrativecraft.screens.controller.areaTrigger;

import fr.loudo.narrativecraft.controllers.interaction.InteractionController;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.AreaTrigger;
import fr.loudo.narrativecraft.screens.storyManager.areaTrigger.AreaTriggersScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class AreaTriggerControllerScreen extends Screen {

    private static Vec3 oldLoc1;
    private static Vec3 oldLoc2;

    private final int BUTTON_HEIGHT = 20;
    private final int BUTTON_WIDTH = 30;
    private final InteractionController interactionController;
    private final AreaTrigger areaTrigger;

    public AreaTriggerControllerScreen(InteractionController interactionController, AreaTrigger areaTrigger) {
        super(Component.literal("Area Trigger Controller Screen"));
        this.interactionController = interactionController;
        this.areaTrigger = areaTrigger;
    }

    public AreaTriggerControllerScreen(
            InteractionController interactionController, AreaTrigger areaTrigger, Vec3 oldLoc1, Vec3 oldLoc2) {
        super(Component.literal("Area Trigger Controller Screen"));
        this.interactionController = interactionController;
        this.areaTrigger = areaTrigger;
        AreaTriggerControllerScreen.oldLoc1 = oldLoc1;
        AreaTriggerControllerScreen.oldLoc2 = oldLoc2;
    }

    @Override
    protected void init() {
        int spacing = 5;
        int totalWidth = BUTTON_WIDTH * 4 + spacing * 3;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height - 50;

        Button location1Btn = Button.builder(Component.literal("1"), button -> {
                    areaTrigger.setPosition1(minecraft.player.position());
                })
                .bounds(startX, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        location1Btn.setTooltip(Tooltip.create(Translation.message("tooltip.area_trigger.loc_1")));
        this.addRenderableWidget(location1Btn);

        Button location2Btn = Button.builder(Component.literal("2"), button -> {
                    areaTrigger.setPosition2(minecraft.player.position());
                })
                .bounds(startX + (BUTTON_WIDTH + spacing), y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        location2Btn.setTooltip(Tooltip.create(Translation.message("tooltip.area_trigger.loc_2")));
        this.addRenderableWidget(location2Btn);

        Button saveButton = Button.builder(ImageFontConstants.SAVE, button -> {
                    interactionController.setAreaTriggerEditing(null);
                    minecraft.setScreen(new AreaTriggersScreen(null, interactionController));
                })
                .bounds(startX + (BUTTON_WIDTH + spacing) * 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(saveButton);

        Button closeButton = Button.builder(Component.literal("✖"), button -> {
                    ConfirmScreen confirm = new ConfirmScreen(
                            b -> {
                                if (b) {
                                    interactionController.setAreaTriggerEditing(null);
                                    minecraft.setScreen(new AreaTriggersScreen(null, interactionController));
                                    areaTrigger.setPosition1(AreaTriggerControllerScreen.oldLoc1);
                                    areaTrigger.setPosition2(AreaTriggerControllerScreen.oldLoc2);
                                } else {
                                    minecraft.setScreen(this);
                                }
                            },
                            Component.literal(""),
                            Translation.message("controller.confirm_leaving"),
                            CommonComponents.GUI_YES,
                            CommonComponents.GUI_CANCEL);
                    minecraft.setScreen(confirm);
                })
                .bounds(startX + (BUTTON_WIDTH + spacing) * 3, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(closeButton);
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
