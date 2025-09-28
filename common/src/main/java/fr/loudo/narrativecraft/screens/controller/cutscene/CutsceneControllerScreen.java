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
import fr.loudo.narrativecraft.controllers.cutscene.CutsceneController;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframeGroup;
import fr.loudo.narrativecraft.screens.keyframe.KeyframeTriggerScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class CutsceneControllerScreen extends Screen {

    private final Component pauseText = Component.literal("⏸");
    private final Component playText = Component.literal("▶");

    private final int BUTTON_HEIGHT = 20;

    private int initialY;
    private int totalWidthControllerBtn;
    private Button controllerButton;

    private final CutsceneController cutsceneController;

    public CutsceneControllerScreen(CutsceneController cutsceneController) {
        super(Component.literal("Cutscene Controller Screen"));
        this.cutsceneController = cutsceneController;
    }

    @Override
    protected void init() {
        initialY = this.height - 80;
        initControllerButtons();
        initKeyframesButton();
        initSettingsButton();
    }

    private void initControllerButtons() {
        int pauseBtnWidth = 20;
        int btnWidth = 50;
        int gap = 5;
        totalWidthControllerBtn = pauseBtnWidth + (btnWidth * 2) + (gap * 2);
        int startX = (this.width - totalWidthControllerBtn) / 2;

        String previousText = "- %.1fs";
        Button previousSkip = Button.builder(
                        Component.literal(
                                String.format(previousText, ((double) cutsceneController.getSkipTickCount()) / 20)),
                        button -> {
                            cutsceneController.previousSecondSkip();
                        })
                .bounds(startX, initialY, btnWidth, BUTTON_HEIGHT)
                .build();

        controllerButton = Button.builder(cutsceneController.isPlaying() ? pauseText : playText, button -> {
                    playOrPause();
                })
                .bounds(startX + btnWidth + gap, initialY, pauseBtnWidth, BUTTON_HEIGHT)
                .build();

        String nextText = "+ %.1fs";
        Button nextSkip = Button.builder(
                        Component.literal(
                                String.format(nextText, ((double) cutsceneController.getSkipTickCount()) / 20)),
                        button -> {
                            cutsceneController.nextSecondSkip();
                        })
                .bounds(startX + btnWidth + gap + pauseBtnWidth + gap, initialY, btnWidth, BUTTON_HEIGHT)
                .build();

        this.addRenderableWidget(previousSkip);
        this.addRenderableWidget(controllerButton);
        this.addRenderableWidget(nextSkip);
    }

    private void initKeyframesButton() {
        int btnWidth = 30;
        int gap = 5;
        int totalWidth = (btnWidth * 3) + (gap * 2) + 15;
        int controllerStartX = (this.width - totalWidthControllerBtn) / 2;
        int startX = controllerStartX - gap - totalWidth;

        Button createKeyframeGroup = Button.builder(ImageFontConstants.CREATE_KEYFRAME_GROUP, button -> {
                    CutsceneKeyframeGroup keyframeGroup = cutsceneController.createKeyframeGroup();

                    if (keyframeGroup == null) return;
                    minecraft.player.displayClientMessage(
                            Translation.message("controller.cutscene.keyframe_group_created", keyframeGroup.getId()),
                            false);
                })
                .bounds(startX, initialY, btnWidth, BUTTON_HEIGHT)
                .build();
        createKeyframeGroup.setTooltip(Tooltip.create(Translation.message("tooltip.create_keyframe_group")));

        Button addKeyframe = Button.builder(ImageFontConstants.ADD_KEYFRAME, button -> {
                    cutsceneController.createKeyframe();
                })
                .bounds(startX + btnWidth + gap, initialY, btnWidth, BUTTON_HEIGHT)
                .build();
        addKeyframe.setTooltip(Tooltip.create(Translation.message("tooltip.create_keyframe")));

        Button addTriggerKeyframe = Button.builder(ImageFontConstants.ADD_KEYFRAME_TRIGGER, button -> {
                    KeyframeTriggerScreen screen =
                            new KeyframeTriggerScreen(cutsceneController, cutsceneController.getCurrentTick());
                    minecraft.setScreen(screen);
                })
                .bounds(startX + (btnWidth + gap) * 2, initialY, btnWidth, BUTTON_HEIGHT)
                .build();
        addTriggerKeyframe.setTooltip(Tooltip.create(Translation.message("tooltip.create_keyframe_trigger")));

        this.addRenderableWidget(createKeyframeGroup);
        this.addRenderableWidget(addKeyframe);
        this.addRenderableWidget(addTriggerKeyframe);
    }

    private void initSettingsButton() {
        int btnWidth = 30;
        int controllerStartX = (this.width - totalWidthControllerBtn) / 2;
        int startX = controllerStartX + totalWidthControllerBtn + 15;

        Button settingsButton = Button.builder(ImageFontConstants.SETTINGS, button -> {
                    minecraft.setScreen(new CutsceneSettingsScreen(
                            cutsceneController, this, Translation.message("controller.cutscene.settings.screen_name")));
                })
                .bounds(startX, initialY, btnWidth, BUTTON_HEIGHT)
                .build();
        settingsButton.setTooltip(Tooltip.create(Translation.message("tooltip.cutscene_settings")));
        this.addRenderableWidget(settingsButton);

        startX = settingsButton.getX() + settingsButton.getWidth() + 5;

        Button saveButton = Button.builder(ImageFontConstants.SAVE, button -> {
                    NarrativeCraftMod.server.execute(() -> cutsceneController.stopSession(true));
                    this.onClose();
                })
                .bounds(startX, initialY, btnWidth, BUTTON_HEIGHT)
                .build();
        saveButton.setTooltip(Tooltip.create(Translation.message("tooltip.save")));
        this.addRenderableWidget(saveButton);

        startX = saveButton.getX() + saveButton.getWidth() + 5;

        Button closeButton = Button.builder(Component.literal("✖"), button -> {
                    ConfirmScreen confirm = new ConfirmScreen(
                            b -> {
                                if (b) {
                                    NarrativeCraftMod.server.execute(() -> {
                                        cutsceneController.stopSession(false);
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
                .bounds(startX, initialY, btnWidth, BUTTON_HEIGHT)
                .build();
        closeButton.setTooltip(Tooltip.create(Translation.message("tooltip.leave_without_saving")));
        this.addRenderableWidget(closeButton);
    }

    private void playOrPause() {
        if (cutsceneController.isPlaying()) {
            cutsceneController.pause();
            controllerButton.setMessage(playText);
        } else {
            if (cutsceneController.atMaxTick()) return;
            cutsceneController.resume();
            controllerButton.setMessage(pauseText);
        }
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public Button getControllerButton() {
        return controllerButton;
    }

    public Component getPauseText() {
        return pauseText;
    }

    public Component getPlayText() {
        return playText;
    }
}
