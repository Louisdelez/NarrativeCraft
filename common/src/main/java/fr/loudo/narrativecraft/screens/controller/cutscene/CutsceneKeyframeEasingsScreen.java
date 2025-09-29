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

import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.util.Easing;
import fr.loudo.narrativecraft.util.Translation;
import java.util.Arrays;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CutsceneKeyframeEasingsScreen extends OptionsSubScreen {

    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private CutsceneEasingsList cutsceneEasingsList;
    private final CutsceneKeyframe keyframe;

    public CutsceneKeyframeEasingsScreen(Screen lastScreen, CutsceneKeyframe keyframe) {
        super(lastScreen, Minecraft.getInstance().options, Translation.message("screen.keyframe_advanced.easings"));
        this.keyframe = keyframe;
    }

    protected void addContents() {
        this.cutsceneEasingsList = this.layout.addToContents(new CutsceneEasingsList(this.minecraft));
    }

    protected void repositionElements() {
        super.repositionElements();
        this.cutsceneEasingsList.updateSize(this.width, this.height, this.layout.getX(), this.layout.getY());
    }

    @Override
    public void onClose() {
        CutsceneEasingsList.Entry entry = this.cutsceneEasingsList.getSelected();
        String selectedEasing = entry.easing.name();
        keyframe.setEasing(Easing.valueOf(selectedEasing));
        this.minecraft.setScreen(this.lastScreen);
        super.onClose();
    }

    class CutsceneEasingsList extends ObjectSelectionList<CutsceneEasingsList.Entry> implements LayoutElement {
        public CutsceneEasingsList(Minecraft minecraft) {
            super(
                    minecraft,
                    CutsceneKeyframeEasingsScreen.this.width,
                    CutsceneKeyframeEasingsScreen.this.height - 33 - 53,
                    33,
                    18,
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

        @Override
        public void setX(int x0) {
            this.x0 = x0;
        }

        @Override
        public void setY(int y0) {
            this.y0 = y0;
        }

        @Override
        public int getX() {
            return x0;
        }

        @Override
        public int getY() {
            return y0;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> consumer) {

        }

        public class Entry extends ObjectSelectionList.Entry<CutsceneEasingsList.Entry> {
            private final Easing easing;

            public Entry(Easing easing) {
                this.easing = easing;
            }

            public void render(
                    GuiGraphics p_345300_,
                    int p_345469_,
                    int p_345328_,
                    int p_345700_,
                    int p_345311_,
                    int p_345185_,
                    int p_344805_,
                    int p_345963_,
                    boolean p_345912_,
                    float p_346091_) {
                p_345300_.drawCenteredString(
                        CutsceneKeyframeEasingsScreen.this.font,
                        this.easing.name(),
                        CutsceneEasingsList.this.width / 2,
                        p_345328_ + p_345185_ / 2 - 4,
                        -1);
            }

            public boolean keyPressed(int p_346403_, int p_345881_, int p_345858_) {
                if (CommonInputs.selected(p_346403_)) {
                    this.select();
                    CutsceneKeyframeEasingsScreen.this.onClose();
                    return true;
                } else {
                    return super.keyPressed(p_346403_, p_345881_, p_345858_);
                }
            }

            public boolean mouseClicked(double p_344965_, double p_345385_, int p_345080_) {
                this.select();
                return super.mouseClicked(p_344965_, p_345385_, p_345080_);
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
