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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.controllers.CutsceneController;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframeGroup;
import fr.loudo.narrativecraft.screens.components.KeyframeOptionScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.MathHelper;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class KeyframeCutsceneOptionScreen extends KeyframeOptionScreen<CutsceneKeyframe> {

    private final CutsceneController cutsceneController;
    private final CutsceneKeyframeGroup cutsceneKeyframeGroup;
    private EditBox startDelayBox, pathTimeBox, transitionDelayBox, speedBox;

    public KeyframeCutsceneOptionScreen(
            CutsceneKeyframeGroup cutsceneKeyframeGroup, CutsceneKeyframe keyframe, ServerPlayer player, boolean hide) {
        super(keyframe, player, hide);
        this.cutsceneKeyframeGroup = cutsceneKeyframeGroup;
        this.cutsceneController = (CutsceneController) playerSession.getController();
    }

    @Override
    protected void init() {
        if (!hide) {
            if (!keyframe.isParentGroup()) {
                pathTimeBox = addLabeledEditBox(
                        Translation.message("screen.keyframe_option.path_time"),
                        String.valueOf(MathHelper.millisToSeconds(keyframe.getPathTime())));
                speedBox = addLabeledEditBox(
                        Translation.message("screen.keyframe_option.speed"), String.valueOf(keyframe.getSpeed()));
            }
            if (cutsceneKeyframeGroup.isLastKeyframe(keyframe)) {
                transitionDelayBox = addLabeledEditBox(
                        Translation.message("screen.keyframe_option.transition_delay"),
                        String.valueOf(MathHelper.millisToSeconds(keyframe.getTransitionDelay())));
            } else {
                startDelayBox = addLabeledEditBox(
                        Translation.message("screen.keyframe_option.start_delay"),
                        String.valueOf(MathHelper.millisToSeconds(keyframe.getStartDelay())));
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
                    updateCurrentTick();
                })
                .bounds(INITIAL_POS_X, currentY, this.font.width(updateTitle) + margin, BUTTON_HEIGHT)
                .build();
        Component playTitle = Translation.message("screen.keyframe_option.play_from_here");
        Button playFromHere = Button.builder(playTitle, button -> {
                    if (playerSession != null) {
                        //                CutscenePlayback cutscenePlayback = new CutscenePlayback(player,
                        // cutsceneController.getCutscene().getKeyframeGroupList(), helper, cutsceneController);
                        //                cutscenePlayback.start();
                        //                playerSession.setCutscenePlayback(cutscenePlayback);
                        //                minecraft.setScreen(null);
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
                    //            KeyframeAdvancedSettings screen = new KeyframeAdvancedSettings(cutsceneController,
                    // this, helper);
                    //            this.minecraft.setScreen(screen);
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
                                        updateCurrentTick();
                                        minecraft.setScreen(null);
                                    }
                                } else {
                                    KeyframeCutsceneOptionScreen screen = new KeyframeCutsceneOptionScreen(
                                            cutsceneKeyframeGroup, keyframe, player, false);
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
        this.addRenderableWidget(playFromHere);
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

    protected void initLittleButtons() {
        int currentX = this.width - INITIAL_POS_X;
        int gap = 5;
        int width = 20;
        if (hide) {
            Button eyeClosed = Button.builder(ImageFontConstants.EYE_CLOSED, button -> {
                        KeyframeCutsceneOptionScreen screen =
                                new KeyframeCutsceneOptionScreen(cutsceneKeyframeGroup, keyframe, player, false);
                        minecraft.setScreen(screen);
                    })
                    .bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT)
                    .build();
            this.addRenderableWidget(eyeClosed);
            return;
        }
        Button closeButton = Button.builder(Component.literal("✖"), button -> {
                    cutsceneController.setCamera(null);
                    minecraft.setScreen(null);
                })
                .bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT)
                .build();
        CutsceneKeyframe nextKeyframe = cutsceneController.getNextKeyframe(keyframe);
        if (nextKeyframe != null) {
            currentX -= INITIAL_POS_X + gap;
            Button rightKeyframeButton = Button.builder(Component.literal("▶"), button -> {
                        // TODO: Send a packet to the server instead.
                        NarrativeCraftMod.server.execute(() -> cutsceneController.setCamera(nextKeyframe));
                    })
                    .bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT)
                    .build();
            this.addRenderableWidget(rightKeyframeButton);
        }
        CutsceneKeyframe previousKeyframe = cutsceneController.getPreviousKeyframe(keyframe);
        if (previousKeyframe != null) {
            currentX -= INITIAL_POS_X + gap;
            Button leftKeyframeButton = Button.builder(Component.literal("◀"), button -> {
                        // TODO: Send a packet to the server instead.
                        NarrativeCraftMod.server.execute(() -> cutsceneController.setCamera(previousKeyframe));
                    })
                    .bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT)
                    .build();
            this.addRenderableWidget(leftKeyframeButton);
        }
        this.addRenderableWidget(closeButton);
        currentX -= INITIAL_POS_X + gap;
        Button eyeOpen = Button.builder(ImageFontConstants.EYE_OPEN, button -> {
                    KeyframeCutsceneOptionScreen screen =
                            new KeyframeCutsceneOptionScreen(cutsceneKeyframeGroup, keyframe, player, true);
                    minecraft.setScreen(screen);
                })
                .bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(eyeOpen);
    }

    protected void updateValues() {
        if (startDelayBox != null) {
            float startDelayVal = Float.parseFloat((startDelayBox.getValue()));
            keyframe.setStartDelay(MathHelper.secondsToMillis(startDelayVal));
        }
        if (transitionDelayBox != null) {
            float transitionDelayValue = Float.parseFloat((transitionDelayBox.getValue()));
            keyframe.setTransitionDelay(MathHelper.secondsToMillis(transitionDelayValue));
        }
        if (speedBox != null) {
            double speedValue = Double.parseDouble((speedBox.getValue()));
            keyframe.setSpeed(speedValue);
        }
        float pathTimeVal = pathTimeBox == null ? 0 : Float.parseFloat((pathTimeBox.getValue()));
        keyframe.setPathTime(MathHelper.secondsToMillis(pathTimeVal));
        KeyframeLocation location = getKeyframeLocation();
        keyframe.setKeyframeLocation(location);
        keyframe.updateEntityData(player);
    }

    private KeyframeLocation getKeyframeLocation() {
        float xVal = Float.parseFloat((coordinatesBoxList.get(0).getValue()));
        float yVal = Float.parseFloat((coordinatesBoxList.get(1).getValue()));
        float zVal = Float.parseFloat((coordinatesBoxList.get(2).getValue()));
        KeyframeLocation location = keyframe.getKeyframeLocation();
        location.setPitch(upDownValue);
        location.setYaw(leftRightValue);
        location.setRoll(rotationValue);
        location.setX(xVal);
        location.setY(yVal);
        location.setZ(zVal);
        location.setFov(fovValue);
        return location;
    }

    protected void updateCurrentTick() {
        List<CutsceneKeyframeGroup> keyframeGroupList =
                cutsceneController.getCutscene().getKeyframeGroups();

        int initialFirstTick = 0;
        if (!keyframeGroupList.isEmpty()
                && !keyframeGroupList.getFirst().getKeyframes().isEmpty()) {
            initialFirstTick =
                    keyframeGroupList.getFirst().getKeyframes().getFirst().getTick();
        }

        int referenceTick = 0;

        for (int i = 0; i < keyframeGroupList.size(); i++) {
            CutsceneKeyframeGroup group = keyframeGroupList.get(i);
            List<CutsceneKeyframe> keyframes = group.getKeyframes();

            if (i > 0 && !keyframeGroupList.get(i - 1).getKeyframes().isEmpty()) {
                CutsceneKeyframe lastKeyframePrevGroup =
                        keyframeGroupList.get(i - 1).getKeyframes().getLast();
                referenceTick += (int) (lastKeyframePrevGroup.getTransitionDelay() / 1000.0 * 20);
            }

            for (int j = 0; j < keyframes.size(); j++) {
                CutsceneKeyframe current = keyframes.get(j);

                int newTick;
                if (i == 0 && j == 0) {
                    newTick = initialFirstTick;
                } else if (j > 0) {
                    CutsceneKeyframe previous = keyframes.get(j - 1);
                    double startDelay = previous.getStartDelay() / 1000.0;
                    double pathTime = current.getPathTime() / 1000.0;
                    newTick = previous.getTick() + (int) ((startDelay + pathTime) * 20);
                } else {
                    newTick = referenceTick;
                }

                current.setTick(newTick);
                referenceTick = newTick;
            }
        }
        cutsceneController.changeTimePosition(keyframe.getTick(), true);
    }
}
