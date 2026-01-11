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

package fr.loudo.narrativecraft.screens.components;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.controllers.interaction.InteractionController;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.EntityInteraction;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class InteractionOptionScreen extends ButtonListScreen {

    private final EntityInteraction entityInteraction;
    private final InteractionController controller;

    public InteractionOptionScreen(
            Screen lastScreen, EntityInteraction entityInteraction, InteractionController controller) {
        super(lastScreen, Translation.message("screen.interaction_option.title"));
        this.entityInteraction = entityInteraction;
        this.controller = controller;
    }

    @Override
    protected void addContents() {

        Button changeStitch = Button.builder(Translation.message("controller.interaction.change_stitch"), button -> {
                    EntryBoxScreen screen = new EntryBoxScreen(
                            this,
                            Translation.message("global.stitch"),
                            entityInteraction.getStitch(),
                            entityInteraction::setStitch);
                    minecraft.setScreen(screen);
                })
                .build();
        objectListScreen.addButton(changeStitch);

        Button removeCharacterButton = Button.builder(Translation.message("global.remove"), button -> {
                    ConfirmScreen confirm = new ConfirmScreen(
                            b -> {
                                if (b) {
                                    NarrativeCraftMod.server.execute(() -> {
                                        entityInteraction.kill(
                                                controller.getPlayerSession().getPlayer());
                                        controller.getEntityInteractions().remove(entityInteraction);
                                    });
                                }
                                minecraft.setScreen(null);
                            },
                            Component.literal(""),
                            Translation.message("global.confirm_delete"),
                            CommonComponents.GUI_YES,
                            CommonComponents.GUI_CANCEL);
                    minecraft.setScreen(confirm);
                })
                .build();
        objectListScreen.addButton(removeCharacterButton);
    }
}
