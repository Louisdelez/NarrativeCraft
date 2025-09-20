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

import fr.loudo.narrativecraft.controllers.mainScreen.MainScreenController;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.keyframes.mainScreen.MainScreenKeyframe;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.components.KeyframeOptionScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class MainScreenKeyframeOptionsScreen extends KeyframeOptionScreen<MainScreenKeyframe, MainScreenController> {

    public MainScreenKeyframeOptionsScreen(
            MainScreenKeyframe keyframe, MainScreenController controller, PlayerSession playerSession, boolean hide) {
        super(keyframe, controller, playerSession, hide);
        reloadScreen = () ->
                minecraft.setScreen(new MainScreenKeyframeOptionsScreen(keyframe, controller, playerSession, false));
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
