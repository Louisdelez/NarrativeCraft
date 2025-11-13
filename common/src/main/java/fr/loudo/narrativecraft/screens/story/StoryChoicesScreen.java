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

package fr.loudo.narrativecraft.screens.story;

import com.bladecoder.ink.runtime.Choice;
import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.components.ChoiceButtonWidget;
import fr.loudo.narrativecraft.util.Util;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

public class StoryChoicesScreen extends Screen {
    private static final double APPEAR_TIME = 0.25;
    private static final int OFFSET = 10;

    private final List<Choice> choiceList;
    private final List<AnimatedChoice> animatedChoices;
    private final List<ChoiceButtonWidget> choiceButtonWidgetList = new ArrayList<>();
    private final int totalTick;
    private PlayerSession playerSession;
    private StoryHandler storyHandler;
    private boolean initiated;
    private double t;
    private int currentTick;

    public StoryChoicesScreen(PlayerSession playerSession, boolean animate) {
        super(Component.literal("Choice screen"));
        this.playerSession = playerSession;
        storyHandler = playerSession.getStoryHandler();
        List<Choice> choices = playerSession.getStoryHandler().getStory().getCurrentChoices();
        this.choiceList = choices.subList(0, Math.min(choices.size(), 4));
        this.animatedChoices = new ArrayList<>();
        initiated = !animate;
        totalTick = (int) (APPEAR_TIME * 20.0);
    }

    public StoryChoicesScreen(List<Choice> choiceList, boolean animate) {
        super(Component.literal("Choice screen"));
        this.choiceList = choiceList.subList(0, Math.min(choiceList.size(), 4));
        this.animatedChoices = new ArrayList<>();
        initiated = !animate;
        totalTick = (int) (APPEAR_TIME * 20.0);
    }

    public static StoryChoicesScreen fromStrings(List<String> stringChoiceList) {
        List<Choice> choices = new ArrayList<>();
        for (String choiceString : stringChoiceList) {
            Choice choice = new Choice();
            choice.setIndex(0);
            choice.setText(choiceString);
            choices.add(choice);
        }
        return new StoryChoicesScreen(choices, true);
    }

    @Override
    public void tick() {
        super.tick();
        if (currentTick < totalTick) {
            currentTick++;
        }
        for (ChoiceButtonWidget choiceButtonWidget : choiceButtonWidgetList) {
            choiceButtonWidget.tick();
        }
    }

    @Override
    protected void init() {
        if (!initiated) {
            ResourceLocation soundRes = new ResourceLocation(NarrativeCraftMod.MOD_ID, "sfx.choice_appear");
            SoundEvent sound = SoundEvent.createVariableRangeEvent(soundRes);
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0f, 1.0f));
        }
        choiceButtonWidgetList.clear();
        for (Choice choice : choiceList) {
            choice.setText(choice.getText()
                    .replace("__username__", playerSession.getPlayer().getName().getString()));
            choiceButtonWidgetList.add(new ChoiceButtonWidget(choice, index -> {
                minecraft.setScreen(null);
                NarrativeCraftMod.server.execute(() -> storyHandler.chooseChoiceAndNext(index));
            }));
        }
        int spacing = 10;
        int baseY = 60;
        int maxWidthUpDown = 0;
        for (int i = 0; i < choiceButtonWidgetList.size(); i++) {
            if (i % 2 != 0) {
                if (choiceButtonWidgetList.get(i).getWidth() > maxWidthUpDown) {
                    maxWidthUpDown = choiceButtonWidgetList.get(i).getWidth();
                }
            }
        }
        for (int i = 0; i < choiceButtonWidgetList.size(); i++) {
            ChoiceButtonWidget choiceButtonWidget = choiceButtonWidgetList.get(i);
            choiceButtonWidget.setOpacity(5);
            choiceButtonWidget.setCanPress(false);
            int currentX = 0;
            int offsetX = 0;
            int offsetY = 0;
            int currentY = this.height - baseY;
            if (choiceButtonWidgetList.size() == 4) currentY -= choiceButtonWidget.getHeight();
            switch (i) {
                case 0:
                    if (choiceButtonWidgetList.size() == 1) {
                        currentX = this.width / 2 - choiceButtonWidget.getWidth() / 2;
                    } else if (choiceButtonWidgetList.size() > 2) {
                        currentX = this.width / 2 - choiceButtonWidget.getWidth() - maxWidthUpDown / 2;
                    } else {
                        currentX = this.width / 2 - choiceButtonWidget.getWidth() - spacing;
                    }
                    offsetX = OFFSET;
                    break;
                case 1:
                    if (choiceButtonWidgetList.size() > 2) {
                        currentY -= choiceButtonWidget.getHeight() + spacing;
                        currentX = this.width / 2 - choiceButtonWidget.getWidth() / 2;
                        offsetY = OFFSET;
                    } else {
                        currentX = this.width / 2 + spacing;
                        offsetX = -OFFSET;
                    }
                    break;
                case 2:
                    currentX = this.width / 2 + maxWidthUpDown / 2;
                    offsetX = -OFFSET;
                    break;
                case 3:
                    currentY += choiceButtonWidget.getHeight() + spacing;
                    currentX = this.width / 2 - choiceButtonWidget.getWidth() / 2;
                    offsetY = -OFFSET;
                    break;
            }
            choiceButtonWidget.setX(currentX);
            choiceButtonWidget.setY(currentY);
            this.addRenderableWidget(choiceButtonWidget);
            AnimatedChoice animatedChoice = new AnimatedChoice(choiceButtonWidget, offsetX, offsetY);
            animatedChoices.add(animatedChoice);
        }
        if (!initiated) {
            t = 0;
            initiated = true;
        } else {
            t = 1;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE) return false;
        if (storyHandler == null) {
            minecraft.setScreen(null);
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        List<KeyMapping> choiceKeys = List.of(
                ModKeys.SELECT_CHOICE_1, ModKeys.SELECT_CHOICE_2, ModKeys.SELECT_CHOICE_3, ModKeys.SELECT_CHOICE_4);
        for (int i = 0; i < choiceList.size(); i++) {
            if (keyCode == choiceKeys.get(i).getDefaultKey().getValue()) {
                minecraft.setScreen(null);
                try {
                    int finalI = i;
                    NarrativeCraftMod.server.execute(() -> storyHandler.chooseChoiceAndNext(finalI));
                } catch (Exception e) {
                    storyHandler.stop();
                    Util.sendCrashMessage(minecraft.player, e);
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        t = Mth.clamp((currentTick + partialTick) / totalTick, 0.0, 1.0);
        for (AnimatedChoice ac : animatedChoices) {
            int newOpacity = (int) Mth.lerp(t, 5, 255);
            guiGraphics.pose().pushPose();
            if (choiceList.size() > 1) {
                guiGraphics.pose().translate((float) Mth.lerp(t, ac.offsetX, 0), (float) Mth.lerp(t, ac.offsetY, 0), 0);
            }
            ac.widget.setOpacity(newOpacity);
            ac.widget.render(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.pose().popPose();
            if (t >= 1.0) {
                ac.widget.setCanPress(true);
            }
        }
    }

    @Override
    protected void repositionElements() {
        animatedChoices.clear();
        super.repositionElements();
    }

    @Override
    public void onClose() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {}

    private record AnimatedChoice(ChoiceButtonWidget widget, int offsetX, int offsetY) {}
}
