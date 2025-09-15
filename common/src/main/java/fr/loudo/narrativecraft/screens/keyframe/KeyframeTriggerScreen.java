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

package fr.loudo.narrativecraft.screens.keyframe;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionRegistry;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.controllers.keyframe.AbstractKeyframeController;
import fr.loudo.narrativecraft.controllers.mainScreen.MainScreenController;
import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.keyframes.keyframeTrigger.KeyframeTrigger;
import fr.loudo.narrativecraft.narrative.story.inkAction.*;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class KeyframeTriggerScreen extends Screen {

    private final int gap = 5;
    private final int tickBoxHeight = 20;
    private final int tickBoxWidth = 60;
    private final int commandBoxHeight = 120;
    private final int globalWidth = 240;

    private int defaultTick;

    private ScreenUtils.LabelBox tickBox;
    private ScreenUtils.MultilineLabelBox commandBox;

    private AbstractKeyframeController<? extends Keyframe> controller;
    private KeyframeTrigger keyframeTrigger;

    public KeyframeTriggerScreen(
            AbstractKeyframeController<? extends Keyframe> controller, KeyframeTrigger keyframeTrigger) {
        super(Component.literal("Keyframe Trigger Screen"));
        this.controller = controller;
        this.keyframeTrigger = keyframeTrigger;
        this.defaultTick = keyframeTrigger.getTick();
    }

    public KeyframeTriggerScreen(AbstractKeyframeController<? extends Keyframe> controller, int defaultTick) {
        super(Component.literal("Keyframe Trigger Screen"));
        this.controller = controller;
        this.defaultTick = defaultTick;
    }

    @Override
    protected void init() {

        int totalHeight = tickBoxHeight + gap + commandBoxHeight + gap + 20 + gap + 20;

        if (keyframeTrigger != null) {
            totalHeight += 20 + gap + 20;
        }

        int startY = (this.height - totalHeight) / 2;

        int xGlobal = (this.width - globalWidth) / 2;

        StringWidget tagWidget = new StringWidget(
                0, startY - minecraft.font.lineHeight - 30, 100, 40, Component.empty(), minecraft.font);
        addRenderableWidget(tagWidget);
        StringWidget errorWidget = new StringWidget(
                0, startY - minecraft.font.lineHeight - 20, 100, 40, Component.empty(), minecraft.font);
        addRenderableWidget(errorWidget);

        int currentY = startY;

        tickBox = new ScreenUtils.LabelBox(
                Component.literal("Tick"),
                minecraft.font,
                tickBoxWidth,
                tickBoxHeight,
                xGlobal,
                currentY,
                ScreenUtils.Align.HORIZONTAL);
        tickBox.getEditBox().setFilter(s -> s.matches("^(?:[0-9]+)?$"));
        this.addRenderableWidget(tickBox.getStringWidget());
        this.addRenderableWidget(tickBox.getEditBox());

        currentY += tickBoxHeight + gap;

        commandBox = new ScreenUtils.MultilineLabelBox(
                Component.literal("Tags"),
                minecraft.font,
                globalWidth,
                commandBoxHeight,
                xGlobal,
                currentY,
                Component.literal("animation start cathy_walk\ntime set 6000 to 90000 for 6 seconds\n..."));
        this.addRenderableWidget(commandBox.getStringWidget());
        this.addRenderableWidget(commandBox.getMultiLineEditBox());

        currentY += commandBoxHeight + commandBox.getStringWidget().getHeight() + gap * 2;

        Button doneButton = Button.builder(CommonComponents.GUI_DONE, button -> {
                    List<String> tags = Arrays.asList(
                            commandBox.getMultiLineEditBox().getValue().split("\n"));

                    for (String tag : tags) {
                        String stringTag = "\"" + tag + "\"";
                        int tagWidth = minecraft.font.width(stringTag);

                        tagWidget.setWidth(tagWidth);
                        tagWidget.setX(width / 2 - tagWidth / 2);

                        InkAction inkAction = InkActionRegistry.findByCommand(tag);

                        if (inkAction instanceof SaveInkAction || inkAction instanceof OnEnterInkAction) {
                            showError(
                                    tagWidget,
                                    errorWidget,
                                    stringTag,
                                    Translation.message("ink_action.not_authorized", stringTag));
                            return;
                        }

                        if (inkAction == null) {
                            showError(tagWidget, errorWidget, stringTag, Translation.message("ink_action.no_exists"));
                            return;
                        }

                        if (controller instanceof MainScreenController) {
                            if (inkAction.needScene()) {
                                showError(
                                        tagWidget,
                                        errorWidget,
                                        stringTag,
                                        Translation.message("ink_action.not_authorized", stringTag));
                                return;
                            }
                        }

                        InkActionResult result = inkAction.validate(
                                tag, controller.getPlayerSession().getScene());
                        if (result.isError()) {
                            showError(tagWidget, errorWidget, stringTag, Component.literal(result.errorMessage()));
                            return;
                        }
                    }

                    int tick = defaultTick;
                    try {
                        tick = Integer.parseInt(tickBox.getEditBox().getValue());
                    } catch (NumberFormatException ignored) {
                    }
                    String commands = commandBox.getMultiLineEditBox().getValue();

                    if (keyframeTrigger == null) {
                        controller.addKeyframeTrigger(tick, commands);
                    } else {
                        keyframeTrigger.setTick(tick);
                        keyframeTrigger.setCommands(commands);
                    }
                    this.onClose();
                })
                .width(globalWidth)
                .pos(xGlobal, currentY)
                .build();

        this.addRenderableWidget(doneButton);

        currentY += 20 + gap;

        Button closeButton = Button.builder(Translation.message("global.close"), button -> {
                    this.onClose();
                })
                .width(globalWidth)
                .pos(xGlobal, currentY)
                .build();
        this.addRenderableWidget(closeButton);

        if (keyframeTrigger != null) {
            currentY += 20 + gap;

            Button removeButton = Button.builder(Translation.message("global.remove"), button -> {
                        ConfirmScreen confirmScreen = new ConfirmScreen(
                                b -> {
                                    if (b) {
                                        controller.removeKeyframeTrigger(keyframeTrigger);
                                        onClose();
                                    } else {
                                        KeyframeTriggerScreen screen =
                                                new KeyframeTriggerScreen(controller, keyframeTrigger);
                                        minecraft.setScreen(screen);
                                    }
                                },
                                Component.literal(""),
                                Translation.message("global.confirm_delete"),
                                CommonComponents.GUI_YES,
                                CommonComponents.GUI_CANCEL);
                        minecraft.setScreen(confirmScreen);
                    })
                    .width(globalWidth)
                    .pos(xGlobal, currentY)
                    .build();

            this.addRenderableWidget(removeButton);
        }

        if (keyframeTrigger != null) {
            tickBox.getEditBox().setValue(String.valueOf(keyframeTrigger.getTick()));
            commandBox.getMultiLineEditBox().setValue(keyframeTrigger.getCommands());
        } else {
            tickBox.getEditBox().setValue(String.valueOf(defaultTick));
        }
    }

    private void showError(StringWidget tagWidget, StringWidget errorWidget, String stringTag, Component errorMessage) {
        tagWidget.setMessage(Component.literal(stringTag).withStyle(ChatFormatting.RED));

        errorWidget.setMessage(errorMessage.copy().withStyle(ChatFormatting.RED));
        int errorWidth = minecraft.font.width(errorMessage);
        errorWidget.setWidth(errorWidth);
        errorWidget.setX(width / 2 - errorWidth / 2);
    }
}
