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

import fr.loudo.narrativecraft.controllers.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.keyframes.cameraAngle.CameraAngleKeyframe;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.components.EntryBoxScreen;
import fr.loudo.narrativecraft.screens.components.KeyframeOptionScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class CameraAngleOptionsScreen extends KeyframeOptionScreen<CameraAngleKeyframe, CameraAngleController> {

    public CameraAngleOptionsScreen(
            CameraAngleKeyframe keyframe,
            CameraAngleController keyframeController,
            PlayerSession playerSession,
            boolean hide) {
        super(keyframe, keyframeController, playerSession, hide);
        reloadScreen = () ->
                minecraft.setScreen(new CameraAngleOptionsScreen(keyframe, keyframeController, playerSession, false));
    }

    @Override
    protected void init() {
        if (!hide) {
            initPositionLabelBox();
            initButtons();
            initSliders();
            initTextSelectedKeyframe();
        }
        initLittleButtons();
        currentY = INITIAL_POS_Y;
    }

    @Override
    public void onClose() {}

    @Override
    protected void initLittleButtons() {
        super.initLittleButtons();
        if (hide) return;
        Button editButton = Button.builder(ImageFontConstants.EDIT, button -> {
                    EntryBoxScreen screen =
                            new EntryBoxScreen(this, Translation.message("global.name"), keyframe.getName(), name -> {
                                if (name.isEmpty()) {
                                    ScreenUtils.sendToast(
                                            Translation.message("global.error"),
                                            Translation.message(
                                                    "camera_angle.name_no_empty",
                                                    name,
                                                    keyframeController
                                                            .getCameraAngle()
                                                            .getName()));
                                } else if (keyframeController.getKeyframeByName(name) != null) {
                                    ScreenUtils.sendToast(
                                            Translation.message("global.error"),
                                            Translation.message(
                                                    "camera_angle.already_exists",
                                                    name,
                                                    keyframeController
                                                            .getCameraAngle()
                                                            .getName()));
                                } else {
                                    minecraft.options.hideGui = true;
                                    keyframe.setName(name);
                                    onClose();
                                }
                            });
                    minecraft.setScreen(screen);
                })
                .bounds(littleButtons.getLast().getX() - 25, INITIAL_POS_Y - 5, 20, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(editButton);
    }

    @Override
    protected void initTextSelectedKeyframe() {
        int y = 10;
        String cameraAngleName = keyframe.getName();
        this.addRenderableWidget(ScreenUtils.text(
                Component.literal(cameraAngleName),
                this.font,
                this.width / 2 - this.font.width(cameraAngleName) / 2,
                y));
    }

    @Override
    protected void initButtons() {
        Component updateTitle = Translation.message("screen.keyframe_option.update");
        Button updateButton = Button.builder(updateTitle, button -> {
                    updateValues();
                })
                .bounds(INITIAL_POS_X, currentY, this.font.width(updateTitle) + 30, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(updateButton);
        currentY += BUTTON_HEIGHT + 15;
        Component removeTitle = Translation.message("global.remove");
        Button removeKeyframe = Button.builder(removeTitle, button -> {
                    ConfirmScreen confirmScreen = new ConfirmScreen(
                            b -> {
                                if (b) {
                                    if (playerSession != null) {
                                        keyframeController.setCamera(null);
                                        keyframeController.removeKeyframe(keyframe);
                                        minecraft.setScreen(null);
                                    }
                                } else {
                                    minecraft.setScreen(this);
                                }
                            },
                            Component.literal(""),
                            Translation.message("global.confirm_delete"),
                            CommonComponents.GUI_YES,
                            CommonComponents.GUI_CANCEL);
                    minecraft.setScreen(confirmScreen);
                })
                .bounds(INITIAL_POS_X, currentY, this.font.width(removeTitle) + 15, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(removeKeyframe);
    }

    protected void updateValues() {
        KeyframeLocation position = keyframe.getKeyframeLocation();
        try {
            float xVal = Float.parseFloat((coordinatesBoxList.get(0).getValue()));
            float yVal = Float.parseFloat((coordinatesBoxList.get(1).getValue()));
            float zVal = Float.parseFloat((coordinatesBoxList.get(2).getValue()));
            position.setX(xVal);
            position.setY(yVal);
            position.setZ(zVal);
        } catch (NumberFormatException ignored) {
        }
        position.setPitch(upDownValue);
        position.setYaw(leftRightValue);
        position.setRoll(rotationValue);
        position.setFov(fovValue);
        keyframe.setKeyframeLocation(position);
        keyframe.updateEntityData(player);
    }
}
