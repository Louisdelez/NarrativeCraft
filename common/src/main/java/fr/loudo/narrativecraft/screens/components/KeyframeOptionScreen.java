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
import fr.loudo.narrativecraft.controllers.keyframe.AbstractKeyframeController;
import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class KeyframeOptionScreen<T extends Keyframe, E extends AbstractKeyframeController<T>> extends Screen {

    protected final int INITIAL_POS_X = 20;
    protected final int INITIAL_POS_Y = 15;
    protected final int EDIT_BOX_WIDTH = 60;
    protected final int EDIT_BOX_HEIGHT = 15;
    protected final int BUTTON_HEIGHT = 20;

    protected final List<EditBox> coordinatesBoxList = new ArrayList<>();
    protected final List<Button> littleButtons = new ArrayList<>();
    protected final ServerPlayer player;
    protected final PlayerSession playerSession;
    protected final T keyframe;
    protected final E keyframeController;
    protected Runnable reloadScreen;

    protected float upDownValue, leftRightValue, rotationValue, fovValue;
    protected int currentY = INITIAL_POS_Y;

    protected boolean hide;

    public KeyframeOptionScreen(T keyframe, E keyframeController, PlayerSession playerSession, boolean hide) {
        super(Translation.message("screen.keyframe_option.title"));
        this.keyframe = keyframe;
        this.player = playerSession.getPlayer();
        this.playerSession = playerSession;
        this.upDownValue = keyframe.getKeyframeLocation().getPitch();
        this.leftRightValue = keyframe.getKeyframeLocation().getYaw();
        this.rotationValue = keyframe.getKeyframeLocation().getRoll();
        this.fovValue = keyframe.getKeyframeLocation().getFov();
        this.hide = hide;
        this.keyframeController = keyframeController;
    }

    protected void init() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    protected EditBox addLabeledEditBox(Component text, String defaultValue) {
        StringWidget labelWidget = ScreenUtils.text(text, this.font, INITIAL_POS_X, currentY);

        EditBox editBox = new EditBox(
                this.font,
                INITIAL_POS_X + labelWidget.getWidth() + 5,
                currentY - labelWidget.getHeight() / 2,
                EDIT_BOX_WIDTH,
                EDIT_BOX_HEIGHT,
                Component.literal(text.getString() + " Value"));

        editBox.setValue(defaultValue);
        editBox.setFilter(s -> s.matches(Util.REGEX_FLOAT_ONLY_POSITIVE));

        this.addRenderableWidget(labelWidget);
        this.addRenderableWidget(editBox);

        currentY += EDIT_BOX_HEIGHT + 5;
        return editBox;
    }

    protected void initButtons() {}

    protected void initPositionLabelBox() {
        int currentX = INITIAL_POS_X;
        int editWidth = 50;
        int i = 0;
        KeyframeLocation position = keyframe.getKeyframeLocation();
        Double[] coords = {position.getX(), position.getY(), position.getZ()};
        String[] labels = {"X:", "Y:", "Z:"};
        for (String label : labels) {
            StringWidget stringWidget = ScreenUtils.text(Component.literal(label), this.font, currentX, currentY);
            EditBox box = new EditBox(
                    this.font,
                    currentX + stringWidget.getWidth() + 5,
                    currentY - stringWidget.getHeight() / 2,
                    editWidth,
                    EDIT_BOX_HEIGHT,
                    Component.literal(stringWidget + " Value"));
            box.setFilter(s -> s.matches(Util.REGEX_FLOAT));
            box.setValue(String.format(Locale.US, "%.2f", coords[i]));
            this.addRenderableWidget(stringWidget);
            this.addRenderableWidget(box);
            coordinatesBoxList.add(box);
            currentX += stringWidget.getWidth() + editWidth + 10;
            i++;
        }
        currentY += 20;
    }

    protected void initTextSelectedKeyframe() {}

    protected void initLittleButtons() {
        int currentX = this.width - INITIAL_POS_X;
        int gap = 5;
        int width = 20;
        if (hide) {
            Button eyeClosed = Button.builder(ImageFontConstants.EYE_CLOSED, button -> {
                        minecraft.setScreen(keyframeController.keyframeOptionScreen(keyframe, false));
                    })
                    .bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT)
                    .build();
            littleButtons.add(eyeClosed);
            this.addRenderableWidget(eyeClosed);
            return;
        }
        Button closeButton = Button.builder(Component.literal("✖"), button -> {
                    keyframeController.setCamera(null);
                    minecraft.setScreen(null);
                })
                .bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT)
                .build();
        littleButtons.add(closeButton);
        T nextKeyframe = keyframeController.getNextKeyframe(keyframe);
        if (nextKeyframe.getId() != keyframe.getId()) {
            currentX -= INITIAL_POS_X + gap;
            Button rightKeyframeButton = Button.builder(Component.literal("▶"), button -> {
                        // TODO: Send a packet to the server instead.
                        NarrativeCraftMod.server.execute(() -> keyframeController.setCamera(nextKeyframe));
                    })
                    .bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT)
                    .build();
            this.addRenderableWidget(rightKeyframeButton);
        }
        T previousKeyframe = keyframeController.getPreviousKeyframe(keyframe);
        if (previousKeyframe.getId() != keyframe.getId()) {
            currentX -= INITIAL_POS_X + gap;
            Button leftKeyframeButton = Button.builder(Component.literal("◀"), button -> {
                        // TODO: Send a packet to the server instead.
                        NarrativeCraftMod.server.execute(() -> keyframeController.setCamera(previousKeyframe));
                    })
                    .bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT)
                    .build();
            littleButtons.add(leftKeyframeButton);
            this.addRenderableWidget(leftKeyframeButton);
        }
        this.addRenderableWidget(closeButton);
        currentX -= INITIAL_POS_X + gap;
        Button eyeOpen = Button.builder(ImageFontConstants.EYE_OPEN, button -> {
                    minecraft.setScreen(keyframeController.keyframeOptionScreen(keyframe, true));
                })
                .bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT)
                .build();
        littleButtons.add(eyeOpen);
        this.addRenderableWidget(eyeOpen);
    }

    protected void initSliders() {
        int initialY = this.height - 50;
        int gap = 5;
        int numSliders = 4;
        int sliderWidth = (this.width - gap * (numSliders + 1)) / numSliders;
        int currentX = gap;

        Function<Float, String> formatFloat = val -> String.format(Locale.US, "%.2f", val);

        // === UP DOWN ===
        float defaultXRot = keyframe.getKeyframeLocation().getPitch();
        float defaultValXRot = defaultXRot + 90F;

        AbstractSliderButton upDownSlider =
                new AbstractSliderButton(
                        currentX,
                        initialY,
                        sliderWidth,
                        BUTTON_HEIGHT,
                        Translation.message("screen.keyframe_option.up_down", formatFloat.apply(defaultValXRot)),
                        defaultValXRot / 180F) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Translation.message(
                                "screen.keyframe_option.up_down", formatFloat.apply(getValue() + 90F)));
                    }

                    @Override
                    protected void applyValue() {
                        upDownValue = getValue();
                        updateValues();
                    }

                    public float getValue() {
                        return (float) (this.value * 180F - 90F);
                    }
                };

        EditBox upDownBox = new EditBox(
                this.font,
                currentX,
                initialY + BUTTON_HEIGHT + 5,
                EDIT_BOX_WIDTH,
                EDIT_BOX_HEIGHT,
                Component.literal("Up Down Value"));
        upDownBox.setValue(formatFloat.apply(defaultValXRot));
        upDownBox.setFilter(s -> s.matches(Util.REGEX_FLOAT));
        Button upDownButton = Button.builder(Component.literal("✔"), btn -> {
                    try {
                        upDownValue = Float.parseFloat(upDownBox.getValue()) - 90F;
                    } catch (NumberFormatException ignored) {
                    }
                    updateValues();
                    reloadScreen.run();
                })
                .bounds(currentX + EDIT_BOX_WIDTH + 5, upDownBox.getY(), 20, EDIT_BOX_HEIGHT)
                .build();

        this.addRenderableWidget(upDownSlider);
        this.addRenderableWidget(upDownBox);
        this.addRenderableWidget(upDownButton);

        currentX += sliderWidth + gap;

        // === LEFT RIGHT ===
        float defaultYRot =
                MathHelper.wrapDegrees360(keyframe.getKeyframeLocation().getYaw());

        AbstractSliderButton leftRightSlider =
                new AbstractSliderButton(
                        currentX,
                        initialY,
                        sliderWidth,
                        BUTTON_HEIGHT,
                        Translation.message("screen.keyframe_option.left_right", formatFloat.apply(defaultYRot)),
                        defaultYRot / 360F) {
                    @Override
                    protected void updateMessage() {
                        float value = MathHelper.wrapDegrees360(
                                keyframe.getKeyframeLocation().getYaw());
                        if (value == 0F && this.value == 1F) value = 360F;
                        this.setMessage(
                                Translation.message("screen.keyframe_option.left_right", formatFloat.apply(value)));
                    }

                    @Override
                    protected void applyValue() {
                        leftRightValue = getValue();
                        updateValues();
                    }

                    public float getValue() {
                        return Mth.wrapDegrees((float) (this.value * 360));
                    }
                };

        EditBox leftRightBox = new EditBox(
                this.font,
                currentX,
                initialY + BUTTON_HEIGHT + 5,
                EDIT_BOX_WIDTH,
                EDIT_BOX_HEIGHT,
                Component.literal("Left Right Value"));
        leftRightBox.setValue(formatFloat.apply(defaultYRot));
        leftRightBox.setFilter(s -> s.matches(Util.REGEX_FLOAT));
        Button leftRightButton = Button.builder(Component.literal("✔"), btn -> {
                    try {
                        leftRightValue = Float.parseFloat(leftRightBox.getValue());
                    } catch (NumberFormatException ignored) {
                    }
                    updateValues();
                    reloadScreen.run();
                })
                .bounds(currentX + EDIT_BOX_WIDTH + 5, leftRightBox.getY(), 20, EDIT_BOX_HEIGHT)
                .build();

        this.addRenderableWidget(leftRightSlider);
        this.addRenderableWidget(leftRightBox);
        this.addRenderableWidget(leftRightButton);

        currentX += sliderWidth + gap;

        // === ROTATION ===
        float defaultZRot = Mth.wrapDegrees(keyframe.getKeyframeLocation().getRoll());

        AbstractSliderButton rotationSlider =
                new AbstractSliderButton(
                        currentX,
                        initialY,
                        sliderWidth,
                        BUTTON_HEIGHT,
                        Translation.message("screen.keyframe_option.rotation", formatFloat.apply(defaultZRot)),
                        ((keyframe.getKeyframeLocation().getRoll() + 180F) % 360F) / 360F) {
                    @Override
                    protected void updateMessage() {
                        float angle = (float) (this.value * 360 - 180);
                        this.setMessage(
                                Translation.message("screen.keyframe_option.rotation", formatFloat.apply(angle)));
                    }

                    @Override
                    protected void applyValue() {
                        float angle = (float) (this.value * 360 - 180);
                        rotationValue = (angle + 360) % 360;
                        updateValues();
                    }
                };

        EditBox rotationBox = new EditBox(
                this.font,
                currentX,
                initialY + BUTTON_HEIGHT + 5,
                EDIT_BOX_WIDTH,
                EDIT_BOX_HEIGHT,
                Component.literal("Rotation Value"));
        rotationBox.setValue(formatFloat.apply(defaultZRot));
        rotationBox.setFilter(s -> s.matches(Util.REGEX_FLOAT));
        Button rotationButton = Button.builder(Component.literal("✔"), btn -> {
                    try {
                        float angle = Float.parseFloat(rotationBox.getValue());
                        rotationValue = (angle + 360F) % 360F;
                    } catch (NumberFormatException ignored) {
                    }
                    updateValues();
                    reloadScreen.run();
                })
                .bounds(currentX + EDIT_BOX_WIDTH + 5, rotationBox.getY(), 20, EDIT_BOX_HEIGHT)
                .build();

        this.addRenderableWidget(rotationSlider);
        this.addRenderableWidget(rotationBox);
        this.addRenderableWidget(rotationButton);

        currentX += sliderWidth + gap;

        // === FOV ===
        float defaultFov = keyframe.getKeyframeLocation().getFov();

        AbstractSliderButton fovSlider =
                new AbstractSliderButton(
                        currentX,
                        initialY,
                        sliderWidth,
                        BUTTON_HEIGHT,
                        Translation.message("screen.keyframe_option.fov", formatFloat.apply(defaultFov)),
                        defaultFov / 150F) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(
                                Translation.message("screen.keyframe_option.fov", formatFloat.apply(getValue())));
                    }

                    @Override
                    protected void applyValue() {
                        fovValue = getValue();
                        updateValues();
                    }

                    public float getValue() {
                        return (float) (this.value * 150F);
                    }
                };

        EditBox fovBox = new EditBox(
                this.font,
                currentX,
                initialY + BUTTON_HEIGHT + 5,
                EDIT_BOX_WIDTH,
                EDIT_BOX_HEIGHT,
                Component.literal("FOV Value"));
        fovBox.setValue(formatFloat.apply(defaultFov));
        fovBox.setFilter(s -> s.matches(Util.REGEX_FLOAT));
        Button fovButton = Button.builder(Component.literal("✔"), btn -> {
                    try {
                        fovValue = Float.parseFloat(fovBox.getValue());
                    } catch (NumberFormatException ignored) {
                    }
                    updateValues();
                    reloadScreen.run();
                })
                .bounds(currentX + EDIT_BOX_WIDTH + 5, fovBox.getY(), 20, EDIT_BOX_HEIGHT)
                .build();

        this.addRenderableWidget(fovSlider);
        this.addRenderableWidget(fovBox);
        this.addRenderableWidget(fovButton);
    }

    protected void updateValues() {}

    public boolean isHide() {
        return hide;
    }
}
