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

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.util.Easing;
import fr.loudo.narrativecraft.util.Translation;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * MC 1.19.x version of CutsceneKeyframeEasingsScreen.
 * Uses Screen directly (OptionsSubScreen doesn't exist in 1.19.x).
 * ObjectSelectionList uses 6-param constructor (minecraft, width, height, y0, y1, itemHeight).
 * Entry.render uses PoseStack instead of GuiGraphics.
 */
public class CutsceneKeyframeEasingsScreen extends Screen {

    private CutsceneEasingsList cutsceneEasingsList;
    private final CutsceneKeyframe keyframe;
    private final Screen lastScreen;

    public CutsceneKeyframeEasingsScreen(Screen lastScreen, CutsceneKeyframe keyframe) {
        super(Translation.message("screen.keyframe_advanced.easings"));
        this.lastScreen = lastScreen;
        this.keyframe = keyframe;
    }

    @Override
    protected void init() {
        int headerHeight = 33;
        int footerHeight = 53;
        this.cutsceneEasingsList = new CutsceneEasingsList(this.minecraft, headerHeight, footerHeight);
        this.addWidget(this.cutsceneEasingsList);

        // Done button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
        this.renderBackground(poseStack);
        this.cutsceneEasingsList.render(poseStack, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        CutsceneEasingsList.Entry entry = this.cutsceneEasingsList.getSelected();
        if (entry != null) {
            String selectedEasing = entry.easing.name();
            keyframe.setEasing(Easing.valueOf(selectedEasing));
        }
        this.minecraft.setScreen(this.lastScreen);
    }

    class CutsceneEasingsList extends ObjectSelectionList<CutsceneEasingsList.Entry> {
        public CutsceneEasingsList(Minecraft minecraft, int headerHeight, int footerHeight) {
            // 1.19.x: ObjectSelectionList(minecraft, width, height, y0, y1, itemHeight)
            super(
                    minecraft,
                    CutsceneKeyframeEasingsScreen.this.width,
                    CutsceneKeyframeEasingsScreen.this.height,
                    headerHeight,
                    CutsceneKeyframeEasingsScreen.this.height - footerHeight,
                    18);
            String selectedEasing = keyframe.getEasing().name();
            Arrays.stream(Easing.values()).toList().forEach(easing -> {
                Entry entry = new Entry(easing);
                this.addEntry(entry);
                if (selectedEasing.equals(easing.name())) {
                    this.setSelected(entry);
                }
            });
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class Entry extends ObjectSelectionList.Entry<CutsceneEasingsList.Entry> {
            private final Easing easing;

            public Entry(Easing easing) {
                this.easing = easing;
            }

            @Override
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                GuiGraphics guiGraphics = new GuiGraphics(CutsceneKeyframeEasingsScreen.this.minecraft, poseStack);
                guiGraphics.drawCenteredString(
                        CutsceneKeyframeEasingsScreen.this.font,
                        this.easing.name(),
                        CutsceneEasingsList.this.width / 2,
                        top + height / 2 - 9 / 2,
                        -1);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
                    this.select();
                    CutsceneKeyframeEasingsScreen.this.onClose();
                    return true;
                } else {
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.select();
                return super.mouseClicked(mouseX, mouseY, button);
            }

            private void select() {
                CutsceneEasingsList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.literal(easing.name());
            }
        }
    }
}
