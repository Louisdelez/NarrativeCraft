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
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.util.Easing;
import fr.loudo.narrativecraft.util.Translation;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class CutsceneKeyframeEasingsScreen extends OptionsSubScreen {

    private CutsceneEasingsList cutsceneEasingsList;
    private final CutsceneKeyframe keyframe;

    public CutsceneKeyframeEasingsScreen(Screen lastScreen, CutsceneKeyframe keyframe) {
        super(lastScreen, Minecraft.getInstance().options, Translation.message("screen.keyframe_advanced.easings"));
        this.keyframe = keyframe;
    }

    protected void addContents() {
        this.cutsceneEasingsList = this.layout.addToContents(new CutsceneEasingsList(this.minecraft));
    }

    protected void addOptions() {}

    protected void repositionElements() {
        super.repositionElements();
        this.cutsceneEasingsList.updateSize(this.width, this.layout);
    }

    @Override
    public void onClose() {
        CutsceneEasingsList.Entry entry = this.cutsceneEasingsList.getSelected();
        String selectedEasing = entry.easing.name();
        keyframe.setEasing(Easing.valueOf(selectedEasing));
        this.minecraft.setScreen(this.lastScreen);
        super.onClose();
    }

    class CutsceneEasingsList extends ObjectSelectionList<CutsceneEasingsList.Entry> {
        public CutsceneEasingsList(Minecraft minecraft) {
            super(
                    minecraft,
                    CutsceneKeyframeEasingsScreen.this.width,
                    CutsceneKeyframeEasingsScreen.this.height - 33 - 53,
                    33,
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
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
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
