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

package fr.loudo.narrativecraft.screens.controller.interaction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.controllers.interaction.InteractionController;
import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.CharacterInteraction;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.EntityInteraction;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.screens.components.ChooseCharacterScreen;
import fr.loudo.narrativecraft.screens.components.EntryBoxScreen;
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

public class InteractionControllerScreen extends Screen {

    private final int BUTTON_HEIGHT = 20;
    private final int BUTTON_WIDTH = 30;
    private final InteractionController controller;

    public InteractionControllerScreen(InteractionController controller) {
        super(Component.literal("Interaction Controller Screen"));
        this.controller = controller;
    }

    @Override
    protected void init() {
        int spacing = 5;
        int totalWidth = BUTTON_WIDTH * 5 + spacing * 3;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height - 50;

        Button addCharacter = Button.builder(ImageFontConstants.CHARACTER_ADD, button -> {
                    ChooseCharacterScreen screen = new ChooseCharacterScreen(
                            this,
                            Translation.message(
                                            "controller.add_character",
                                            controller.getInteraction().getName())
                                    .getString(),
                            null,
                            controller.getInteraction().getScene(),
                            characterStory -> {
                                if (characterStory == null) {
                                    onClose();
                                    return;
                                }
                                Location location =
                                        controller.getPlayerSession().getPlayerPosition();
                                CharacterStoryData characterStoryData = new CharacterStoryData(
                                        characterStory,
                                        location,
                                        true,
                                        controller.getInteraction().getScene());
                                characterStoryData.setItems(minecraft.player);
                                characterStoryData.setEntityByte(
                                        minecraft.player.getEntityData().get(EntityAccessor.getDATA_SHARED_FLAGS_ID()));
                                characterStoryData.setLivingEntityByte(minecraft
                                        .player
                                        .getEntityData()
                                        .get(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS()));
                                characterStoryData.spawn(
                                        controller
                                                .getPlayerSession()
                                                .getPlayer()
                                                .level(),
                                        Environment.DEVELOPMENT);
                                controller.getCharacterStoryDataList().add(characterStoryData);
                                minecraft.setScreen(null);
                                controller
                                        .getPlayerSession()
                                        .getCharacterRuntimes()
                                        .add(characterStoryData.getCharacterRuntime());
                                CharacterInteraction characterInteraction =
                                        new CharacterInteraction("", characterStoryData);
                                EntryBoxScreen screen1 = new EntryBoxScreen(
                                        null, Translation.message("global.stitch"), characterInteraction::setStitch);
                                controller.getCharacterInteractions().add(characterInteraction);
                                minecraft.setScreen(screen1);
                            });
                    minecraft.setScreen(screen);
                })
                .bounds(startX, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        addCharacter.setTooltip(Tooltip.create(Translation.message("tooltip.add_character")));
        this.addRenderableWidget(addCharacter);

        Button addEntityInteractionBtn = Button.builder(ImageFontConstants.EYE_OPEN, button -> {
                    EntryBoxScreen screen = new EntryBoxScreen(this, Translation.message("global.stitch"), s -> {
                        Vec3 position = controller
                                .getPlayerSession()
                                .getPlayerPosition()
                                .asVec3();
                        // position = position.add(0, controller.getPlayerSession().getPlayer().getEyeHeight() / 2.0,
                        // 0);
                        EntityInteraction entityInteraction = new EntityInteraction(s, position);
                        controller.getEntityInteractions().add(entityInteraction);
                        entityInteraction.spawn(controller.getPlayerSession().getPlayer(), controller.getEnvironment());
                    });
                    minecraft.setScreen(screen);
                })
                .bounds(startX + (BUTTON_WIDTH + spacing) * 1, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        addEntityInteractionBtn.setTooltip(
                Tooltip.create(Translation.message("controller.interaction.add_entity_interaction")));
        this.addRenderableWidget(addEntityInteractionBtn);

        Button areaTriggerMode = Button.builder(ImageFontConstants.BOX, button -> {
                    AreaTriggersScreen screen = new AreaTriggersScreen(this, controller);
                    minecraft.setScreen(screen);
                })
                .bounds(startX + (BUTTON_WIDTH + spacing) * 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        areaTriggerMode.setTooltip(Tooltip.create(Translation.message("controller.interaction.area_trigger_list")));
        this.addRenderableWidget(areaTriggerMode);

        Button saveButton = Button.builder(ImageFontConstants.SAVE, button -> {
                    NarrativeCraftMod.server.execute(() -> controller.stopSession(true));
                    this.onClose();
                })
                .bounds(startX + (BUTTON_WIDTH + spacing) * 3, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(saveButton);

        Button closeButton = Button.builder(Component.literal("✖"), button -> {
                    ConfirmScreen confirm = new ConfirmScreen(
                            b -> {
                                if (b) {
                                    NarrativeCraftMod.server.execute(() -> {
                                        controller.stopSession(false);
                                    });
                                    this.onClose();
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
                .bounds(startX + (BUTTON_WIDTH + spacing) * 4, y, BUTTON_WIDTH, BUTTON_HEIGHT)
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
