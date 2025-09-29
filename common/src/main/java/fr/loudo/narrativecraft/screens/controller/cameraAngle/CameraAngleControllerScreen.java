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

package fr.loudo.narrativecraft.screens.controller.cameraAngle;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.controllers.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.screens.components.ChooseCharacterScreen;
import fr.loudo.narrativecraft.screens.components.EntryBoxScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class CameraAngleControllerScreen extends Screen {

    private final int BUTTON_HEIGHT = 20;
    private final int BUTTON_WIDTH = 30;
    private final CameraAngleController cameraAngleController;

    public CameraAngleControllerScreen(CameraAngleController cameraAngleController) {
        super(Component.literal("Camera Angle Controller Screen"));
        this.cameraAngleController = cameraAngleController;
    }

    @Override
    protected void init() {
        int spacing = 5;
        int totalWidth = BUTTON_WIDTH * 5 + spacing * 3;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height - 50;

        Button addKeyframe = Button.builder(ImageFontConstants.ADD_KEYFRAME, button -> {
                    EntryBoxScreen screen = new EntryBoxScreen(this, Translation.message("global.name"), name -> {
                        if (name.isEmpty()) {
                            ScreenUtils.sendToast(
                                    Translation.message("global.error"),
                                    Translation.message(
                                            "camera_angle.name_no_empty",
                                            name,
                                            cameraAngleController
                                                    .getCameraAngle()
                                                    .getName()));
                        } else if (cameraAngleController.getKeyframeByName(name) != null) {
                            ScreenUtils.sendToast(
                                    Translation.message("global.error"),
                                    Translation.message(
                                            "camera_angle.already_exists",
                                            name,
                                            cameraAngleController
                                                    .getCameraAngle()
                                                    .getName()));
                        } else {
                            cameraAngleController.createKeyframe(name);
                            onClose();
                        }
                    });
                    minecraft.setScreen(screen);
                })
                .bounds(startX, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        addKeyframe.setTooltip(Tooltip.create(Translation.message("tooltip.create_keyframe")));
        this.addRenderableWidget(addKeyframe);

        Button addCharacter = Button.builder(ImageFontConstants.CHARACTER_ADD, button -> {
                    ChooseCharacterScreen screen = new ChooseCharacterScreen(
                            this,
                            Translation.message(
                                            "controller.camera_angle.add_character",
                                            cameraAngleController
                                                    .getCameraAngle()
                                                    .getName())
                                    .getString(),
                            null,
                            cameraAngleController.getCameraAngle().getScene(),
                            characterStory -> {
                                if (characterStory == null) {
                                    onClose();
                                    return;
                                }
                                Location location =
                                        cameraAngleController.getPlayerSession().getPlayerPosition();
                                CharacterStoryData characterStoryData = new CharacterStoryData(
                                        characterStory,
                                        location,
                                        false,
                                        cameraAngleController.getCameraAngle().getScene());
                                characterStoryData.setItems(minecraft.player);
                                characterStoryData.setEntityByte(
                                        minecraft.player.getEntityData().get(EntityAccessor.getDATA_SHARED_FLAGS_ID()));
                                characterStoryData.setLivingEntityByte(minecraft
                                        .player
                                        .getEntityData()
                                        .get(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS()));
                                characterStoryData.spawn(
                                        cameraAngleController
                                                .getPlayerSession()
                                                .getPlayer()
                                                .level(),
                                        Environment.DEVELOPMENT);
                                cameraAngleController
                                        .getCharacterStoryDataList()
                                        .add(characterStoryData);
                                minecraft.setScreen(null);
                                cameraAngleController
                                        .getPlayerSession()
                                        .getCharacterRuntimes()
                                        .add(characterStoryData.getCharacterRuntime());
                            });
                    minecraft.setScreen(screen);
                })
                .bounds(startX + (BUTTON_WIDTH + spacing) * 1, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        addCharacter.setTooltip(Tooltip.create(Translation.message("tooltip.add_character")));
        this.addRenderableWidget(addCharacter);

        Button recordMenu = Button.builder(ImageFontConstants.SETTINGS, button -> {
                    CameraAngleAddTemplateCharacter cameraAngleAddRecord =
                            new CameraAngleAddTemplateCharacter(this, cameraAngleController);
                    minecraft.setScreen(cameraAngleAddRecord);
                })
                .bounds(startX + (BUTTON_WIDTH + spacing) * 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        recordMenu.setTooltip(Tooltip.create(Translation.message("tooltip.template_character")));
        this.addRenderableWidget(recordMenu);

        Button saveButton = Button.builder(ImageFontConstants.SAVE, button -> {
                    NarrativeCraftMod.server.execute(() -> cameraAngleController.stopSession(true));
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
                                        cameraAngleController.stopSession(false);
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
    public void renderBackground(GuiGraphics guiGraphics) { }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
