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

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * MC 1.19.x version of GenericSelectionScreen.
 * Uses manual positioning (HeaderAndFooterLayout/LinearLayout don't exist in 1.19.x).
 * ObjectSelectionList uses 6-param constructor.
 * Entry.render uses PoseStack instead of GuiGraphics.
 */
public class GenericSelectionScreen<T extends NarrativeEntry> extends Screen {
    protected final List<T> itemList;
    protected final Consumer<T> consumer;
    protected final T currentSelection;
    protected final String screenTitle;
    protected final Screen lastScreen;

    private SelectionList<T> selectionList;

    public GenericSelectionScreen(
            Screen lastScreen, String screenTitle, List<T> itemList, T currentSelection, Consumer<T> consumer) {
        super(Component.literal(screenTitle));
        this.lastScreen = lastScreen;
        this.screenTitle = screenTitle;
        this.itemList = itemList;
        this.currentSelection = currentSelection;
        this.consumer = consumer;
    }

    @Override
    protected void init() {
        int headerHeight = 33;
        int footerHeight = 53;
        this.selectionList = new SelectionList<>(this.minecraft, this, headerHeight, footerHeight);
        this.addWidget(this.selectionList);

        // Done button at the bottom
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
        this.renderBackground(poseStack);
        this.selectionList.render(poseStack, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        SelectionList<T>.Entry entry = this.selectionList.getSelected();
        if (entry == null) {
            consumer.accept(null);
        } else {
            minecraft.setScreen(lastScreen);
            consumer.accept(entry.getItem());
        }
    }

    protected List<T> getItemList() {
        return itemList;
    }

    protected T getCurrentSelection() {
        return currentSelection;
    }

    static class SelectionList<T extends NarrativeEntry> extends ObjectSelectionList<SelectionList<T>.Entry> {
        private final GenericSelectionScreen<T> parentScreen;

        public SelectionList(Minecraft minecraft, GenericSelectionScreen<T> parentScreen, int headerHeight, int footerHeight) {
            // 1.19.x: ObjectSelectionList(minecraft, width, height, y0, y1, itemHeight)
            super(minecraft,
                    parentScreen.width,
                    parentScreen.height,
                    headerHeight,
                    parentScreen.height - footerHeight,
                    18);
            this.parentScreen = parentScreen;

            String selectedName = "";
            if (parentScreen.getCurrentSelection() != null) {
                selectedName = parentScreen.getCurrentSelection().getName();
            }

            for (T item : parentScreen.getItemList()) {
                Entry entry = new Entry(item);
                this.addEntry(entry);

                String itemName = item.getName();
                if (selectedName.equalsIgnoreCase(itemName)) {
                    this.setSelected(entry);
                }
            }

            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final T item;

            public Entry(T item) {
                this.item = item;
            }

            public T getItem() {
                return item;
            }

            @Override
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                    int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                GuiGraphics guiGraphics = new GuiGraphics(parentScreen.minecraft, poseStack);
                String displayName = this.item.getName();
                guiGraphics.drawCenteredString(
                        parentScreen.font,
                        displayName,
                        SelectionList.this.width / 2,
                        top + height / 2 - 9 / 2,
                        -1);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
                    this.select();
                    parentScreen.onClose();
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
                SelectionList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                String displayName = this.item.getName();
                return Component.literal(displayName);
            }
        }
    }
}
