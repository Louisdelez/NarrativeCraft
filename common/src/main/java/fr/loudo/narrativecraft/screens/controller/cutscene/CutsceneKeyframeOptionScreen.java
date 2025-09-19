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

package fr.loudo.narrativecraft.screens.controller.cutscene;

import fr.loudo.narrativecraft.controllers.cutscene.CutsceneController;
import fr.loudo.narrativecraft.controllers.cutscene.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframeGroup;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.components.KeyframeOptionScreen;
import fr.loudo.narrativecraft.util.MathHelper;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CutsceneKeyframeOptionScreen extends KeyframeOptionScreen<CutsceneKeyframe, CutsceneController> {

    private final CutsceneController cutsceneController;
    private final CutsceneKeyframeGroup cutsceneKeyframeGroup;
    private EditBox startDelayBox, pathTimeBox, transitionDelayBox, speedBox;

    public CutsceneKeyframeOptionScreen(CutsceneKeyframe keyframe, PlayerSession playerSession, boolean hide) {
        super(keyframe, (CutsceneController) playerSession.getController(), playerSession, hide);
        this.cutsceneController = (CutsceneController) playerSession.getController();
        this.cutsceneKeyframeGroup = cutsceneController.getKeyframeGroupOfKeyframe(keyframe);
        this.reloadScreen = () -> minecraft.setScreen(new CutsceneKeyframeOptionScreen(keyframe, playerSession, false));
    }

    @Override
    protected void init() {
        if (!hide) {
            if (!keyframe.isParentGroup()) {
                pathTimeBox = addLabeledEditBox(
                        Translation.message("screen.keyframe_option.path_time"),
                        String.valueOf(MathHelper.tickToSeconds(keyframe.getPathTick())));
                speedBox = addLabeledEditBox(
                        Translation.message("screen.keyframe_option.speed"), String.valueOf(keyframe.getSpeed()));
            }
            if (cutsceneKeyframeGroup.isLastKeyframe(keyframe)) {
                transitionDelayBox = addLabeledEditBox(
                        Translation.message("screen.keyframe_option.transition_delay"),
                        String.valueOf(MathHelper.tickToSeconds(keyframe.getTransitionDelayTick())));
            } else {
                startDelayBox = addLabeledEditBox(
                        Translation.message("screen.keyframe_option.start_delay"),
                        String.valueOf(MathHelper.tickToSeconds(keyframe.getStartDelayTick())));
            }
            initPositionLabelBox();
            initSliders();
            initButtons();
            initTextSelectedKeyframe();
        }
        initLittleButtons();
        // Reset for responsive (changing windows size or going fullscreen)
        currentY = INITIAL_POS_Y;
    }

    @Override
    public void onClose() {}

    protected void initButtons() {
        // currentY -= 30;
        int gap = 15;
        int margin = 15;
        Component updateTitle = Translation.message("screen.keyframe_option.update");
        Button updateButton = Button.builder(updateTitle, button -> {
                    updateValues();
                    cutsceneController.updateCurrentTick(keyframe.getTick());
                    cutsceneController.changeTimePosition(keyframe.getTick(), true);
                })
                .bounds(INITIAL_POS_X, currentY, this.font.width(updateTitle) + margin, BUTTON_HEIGHT)
                .build();
        Component playTitle = Translation.message("screen.keyframe_option.play_from_here");
        Button playFromHere = Button.builder(playTitle, button -> {
                    if (playerSession != null) {
                        CutscenePlayback cutscenePlayback = cutsceneController.getCutscenePlayback();
                        if (cutsceneKeyframeGroup.isLastKeyframe(keyframe)) {
                            if (cutsceneKeyframeGroup.getKeyframes().size() > 1) {
                                CutsceneKeyframe previous = cutsceneController.getPreviousKeyframe(keyframe);
                                cutscenePlayback.setupAndPlay(previous, keyframe);
                                int offset = keyframe.getTick() - previous.getTick();
                                cutscenePlayback.setSegmentTick(offset);
                                cutscenePlayback.setTotalTick(previous.getTick() + offset);
                            } else {
                                cutscenePlayback.setupAndPlay(keyframe, keyframe);
                            }
                        } else {
                            cutscenePlayback.setupAndPlay(keyframe, cutsceneController.getNextKeyframe(keyframe));
                        }
                        cutsceneController.setPlaying(true);
                        minecraft.setScreen(null);
                    }
                })
                .bounds(
                        updateButton.getWidth() + updateButton.getX() + 5,
                        currentY,
                        this.font.width(playTitle) + margin,
                        BUTTON_HEIGHT)
                .build();
        currentY += BUTTON_HEIGHT + gap - 10;
        Component advancedTitle = Translation.message("screen.keyframe_option.advanced");
        Button advancedButton = Button.builder(advancedTitle, button -> {
                    CutsceneKeyframeAdvancedSettings screen = new CutsceneKeyframeAdvancedSettings(this, keyframe);
                    this.minecraft.setScreen(screen);
                })
                .bounds(INITIAL_POS_X, currentY, this.font.width(advancedTitle) + margin, BUTTON_HEIGHT)
                .build();
        currentY += BUTTON_HEIGHT + gap;
        Component removeTitle = Translation.message("global.remove");
        Button removeKeyframe = Button.builder(removeTitle, button -> {
                    ConfirmScreen confirmScreen = new ConfirmScreen(
                            b -> {
                                if (b) {
                                    if (playerSession != null) {
                                        cutsceneController.setCamera(null);
                                        cutsceneController.removeKeyframe(keyframe);
                                        cutsceneController.updateCurrentTick(keyframe.getTick());
                                        minecraft.setScreen(null);
                                    }
                                } else {
                                    CutsceneKeyframeOptionScreen screen =
                                            new CutsceneKeyframeOptionScreen(keyframe, playerSession, false);
                                    minecraft.setScreen(screen);
                                }
                            },
                            Component.literal(""),
                            Translation.message("global.confirm_delete"),
                            CommonComponents.GUI_YES,
                            CommonComponents.GUI_CANCEL);
                    minecraft.setScreen(confirmScreen);
                })
                .bounds(INITIAL_POS_X, currentY, this.font.width(removeTitle) + margin, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(updateButton);
        this.addRenderableWidget(advancedButton);
        if (!cutsceneController.isLastKeyframe(keyframe)) {
            this.addRenderableWidget(playFromHere);
        }
        this.addRenderableWidget(removeKeyframe);
    }

    protected void initTextSelectedKeyframe() {
        int y = 10;

        MutableComponent groupText =
                Translation.message("screen.keyframe_option.keyframe_group", cutsceneKeyframeGroup.getId());
        MutableComponent keyframeText = Translation.message("screen.keyframe_option.keyframe_id", keyframe.getId());

        int groupWidth = this.font.width(groupText);
        int keyframeWidth = this.font.width(keyframeText);
        int spacing = 5;
        int totalWidth = groupWidth + spacing + keyframeWidth;

        int startX = (this.width - totalWidth) / 2;

        StringWidget groupLabel = ScreenUtils.text(groupText, this.font, startX, y, 0x27cf1f);
        StringWidget keyframeIdLabel =
                ScreenUtils.text(keyframeText, this.font, startX + groupWidth + spacing, y, 0xF1C40F);

        this.addRenderableWidget(groupLabel);
        this.addRenderableWidget(keyframeIdLabel);
    }

    protected void updateValues() {
        if (startDelayBox != null) {
            try {
                float startDelayVal = Float.parseFloat((startDelayBox.getValue()));
                keyframe.setStartDelayTick(MathHelper.secondsToTick(startDelayVal));
            } catch (NumberFormatException ignored) {
            }
        }
        if (transitionDelayBox != null) {
            try {
                float transitionDelayValue = Float.parseFloat((transitionDelayBox.getValue()));
                keyframe.setTransitionDelayTick(MathHelper.secondsToTick(transitionDelayValue));
            } catch (NumberFormatException ignored) {
            }
        }
        if (speedBox != null) {
            try {
                double speedValue = Double.parseDouble((speedBox.getValue()));
                keyframe.setSpeed(speedValue);
            } catch (NumberFormatException ignored) {
            }
        }
        try {
            float pathTimeVal = pathTimeBox == null ? 0 : Float.parseFloat((pathTimeBox.getValue()));
            keyframe.setPathTick(MathHelper.secondsToTick(pathTimeVal));
        } catch (NumberFormatException ignored) {
        }
        KeyframeLocation location = getKeyframeLocation();
        keyframe.setKeyframeLocation(location);
        keyframe.updateEntityData(player);
    }

    private KeyframeLocation getKeyframeLocation() {
        KeyframeLocation location = keyframe.getKeyframeLocation();
        location.setPitch(upDownValue);
        location.setYaw(leftRightValue);
        location.setRoll(rotationValue);
        try {
            float xVal = Float.parseFloat((coordinatesBoxList.get(0).getValue()));
            float yVal = Float.parseFloat((coordinatesBoxList.get(1).getValue()));
            float zVal = Float.parseFloat((coordinatesBoxList.get(2).getValue()));
            location.setX(xVal);
            location.setY(yVal);
            location.setZ(zVal);
        } catch (NumberFormatException ignored) {
        }
        location.setFov(fovValue);
        return location;
    }
}
