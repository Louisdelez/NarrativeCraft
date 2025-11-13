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
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.controller.areaTrigger.AreaTriggerControllerScreen;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class AreaTriggersScreen extends StoryElementScreen {

    private final Screen lastScreen;
    private final InteractionController interactionController;

    public AreaTriggersScreen(Screen lastScreen, InteractionController interactionController) {
        super(Translation.message(
                "controller.interaction.area_trigger_list",
                interactionController.getInteraction().getName()));
        this.lastScreen = lastScreen;
        this.interactionController = interactionController;
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<AreaTrigger> screen = new EditInfoScreen<>(
                    this, null, new EditScreenAreaTriggerAdapter(lastScreen, interactionController));
            this.minecraft.setScreen(screen);
        });
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, p_345997_ -> this.onClose())
                .width(200)
                .build());
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    @Override
    protected void addContents() {
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
    }

    @Override
    protected void openFolder() {}
}
