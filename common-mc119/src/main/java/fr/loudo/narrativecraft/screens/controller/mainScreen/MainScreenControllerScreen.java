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

package fr.loudo.narrativecraft.screens.controller.mainScreen;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.controllers.mainScreen.MainScreenController;
import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.screens.components.ChooseCharacterScreen;
import fr.loudo.narrativecraft.screens.keyframe.KeyframeTriggerScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class MainScreenControllerScreen extends Screen {

    private final int BUTTON_HEIGHT = 20;
    private final int BUTTON_WIDTH = 30;
    private final MainScreenController mainScreenController;

    public MainScreenControllerScreen(MainScreenController mainScreenController) {
        super(Component.literal("Main Screen Controller Screen"));
        this.mainScreenController = mainScreenController;
    }

    @Override
    protected void init() {
        int spacing = 5;
        int totalWidth = BUTTON_WIDTH * 5 + spacing * 3;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height - 50;

        Button addKeyframe = Button.builder(ImageFontConstants.ADD_KEYFRAME, button -> {
                    if (mainScreenController.getKeyframe() == null) {
                        mainScreenController.addKeyframe();
                    } else {
                        minecraft.player.displayClientMessage(
                                Translation.message("controller.main_screen.only_one_keyframe")
                                        .withStyle(ChatFormatting.RED),
                                false);
                    }
                })
                .bounds(startX, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        addKeyframe.setTooltip(Tooltip.create(Translation.message("tooltip.create_keyframe")));
        this.addRenderableWidget(addKeyframe);

        Button addKeyframeTrigger = Button.builder(ImageFontConstants.ADD_KEYFRAME_TRIGGER, button -> {
                    if (mainScreenController.getKeyframeTrigger() == null) {
                        KeyframeTriggerScreen screen = new KeyframeTriggerScreen(mainScreenController, 0);
                        minecraft.setScreen(screen);
                    } else {
                        minecraft.player.displayClientMessage(
                                Translation.message("controller.main_screen.only_one_keyframe_trigger")
                                        .withStyle(ChatFormatting.RED),
                                false);
                    }
                })
                .bounds(startX + (BUTTON_WIDTH + spacing), y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        addKeyframeTrigger.setTooltip(Tooltip.create(Translation.message("tooltip.create_keyframe_trigger")));
        this.addRenderableWidget(addKeyframeTrigger);

        Button addCharacter = Button.builder(ImageFontConstants.CHARACTER_ADD, button -> {
                    ChooseCharacterScreen screen = new ChooseCharacterScreen(
                            this,
                            Translation.message("controller.add_character", "Main Screen character list")
                                    .getString(),
                            null,
                            null,
                            characterStory -> {
                                if (characterStory == null) {
                                    onClose();
                                    return;
                                }
                                Location location =
                                        mainScreenController.getPlayerSession().getPlayerPosition();
                                CharacterStoryData characterStoryData = new CharacterStoryData(
                                        characterStory,
                                        location,
                                        false,
                                        mainScreenController.getPlayerSession().getScene());
                                characterStoryData.setItems(minecraft.player);
                                characterStoryData.setEntityByte(
                                        minecraft.player.getEntityData().get(EntityAccessor.getDATA_SHARED_FLAGS_ID()));
                                characterStoryData.setLivingEntityByte(minecraft
                                        .player
                                        .getEntityData()
                                        .get(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS()));
                                characterStoryData.spawn(
                                        (ServerLevel) mainScreenController
                                                .getPlayerSession()
                                                .getPlayer()
                                                .getLevel(),
                                        Environment.DEVELOPMENT);
                                mainScreenController.getCharacterStoryDataList().add(characterStoryData);
                                mainScreenController
                                        .getPlayerSession()
                                        .getCharacterRuntimes()
                                        .add(characterStoryData.getCharacterRuntime());
                                minecraft.setScreen(null);
                            });
                    minecraft.setScreen(screen);
                })
                .bounds(startX + (BUTTON_WIDTH + spacing) * 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        addCharacter.setTooltip(Tooltip.create(Translation.message("tooltip.add_character")));
        this.addRenderableWidget(addCharacter);

        Button saveButton = Button.builder(ImageFontConstants.SAVE, button -> {
                    NarrativeCraftMod.server.execute(() -> mainScreenController.stopSession(true));
                    this.onClose();
                })
                .bounds(startX + (BUTTON_WIDTH + spacing) * 3, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(saveButton);

        Button closeButton = Button.builder(Component.literal("X"), button -> {
                    ConfirmScreen confirm = new ConfirmScreen(
                            b -> {
                                if (b) {
                                    NarrativeCraftMod.server.execute(() -> {
                                        mainScreenController.stopSession(false);
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

    // MC 1.19.x: renderBlurredBackground doesn't exist

    @Override
    public void renderBackground(PoseStack poseStack) {
        // Empty - no background rendering for controller screen
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
