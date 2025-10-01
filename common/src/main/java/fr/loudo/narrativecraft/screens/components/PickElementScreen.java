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

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class PickElementScreen extends Screen {

    private static final Component AVAILABLE_TITLE = Component.translatable("pack.available.title");
    private static final Component SELECTED_TITLE = Component.translatable("pack.selected.title");
    private static final int LIST_WIDTH = 200;

    private final Screen lastScreen;
    private final List<? extends NarrativeEntry> availableEntries;
    private final List<? extends NarrativeEntry> selectedEntries;
    private final Component selector;
    private final Consumer<List<TransferableStorySelectionList.Entry>> onDone;

    private Button doneButton;
    private Button moveButton;
    private TransferableStorySelectionList availableList;
    private TransferableStorySelectionList selectedList;

    public PickElementScreen(
            Screen lastScreen,
            Component title,
            Component selector,
            List<? extends NarrativeEntry> availableEntries,
            List<? extends NarrativeEntry> selectedEntries,
            Consumer<List<TransferableStorySelectionList.Entry>> onDone) {
        super(title);
        this.lastScreen = lastScreen;
        this.selector = selector;
        this.availableEntries = availableEntries;
        this.selectedEntries = selectedEntries;
        this.onDone = onDone;
    }

    @Override
    protected void init() {
        this.availableList = new TransferableStorySelectionList(
                this.minecraft,
                this,
                availableEntries,
                LIST_WIDTH,
                this.height,
                Component.literal(selector.getString() + " " + AVAILABLE_TITLE.getString()));
        this.availableList.setLeftPos(this.width / 2 - 18 - LIST_WIDTH);
        this.addWidget(this.availableList);

        this.selectedList = new TransferableStorySelectionList(
                this.minecraft,
                this,
                selectedEntries,
                LIST_WIDTH,
                this.height,
                Component.literal(selector.getString() + " " + SELECTED_TITLE.getString()));
        this.selectedList.setLeftPos(this.width / 2 + 18);
        this.addWidget(this.selectedList);

        this.availableList.setOtherList(selectedList);
        this.selectedList.setOtherList(availableList);

        this.moveButton = this.addRenderableWidget(Button.builder(Component.literal("▶"), button -> {
                    if (availableList.getSelected() != null) {
                        TransferableStorySelectionList.Entry selected = availableList.getSelected();
                        availableList.children().remove(selected);
                        selectedList.children().add(selected);
                        selectedList.setSelected(selected);
                        availableList.setSelected(null);
                        moveButton.setMessage(Component.literal("◀"));
                    } else if (selectedList.getSelected() != null) {
                        TransferableStorySelectionList.Entry selected = selectedList.getSelected();
                        selectedList.children().remove(selected);
                        availableList.children().add(selected);
                        availableList.setSelected(selected);
                        selectedList.setSelected(null);
                        moveButton.setMessage(Component.literal("▶"));
                    }
                    updateMoveButton();
                })
                .bounds(this.width / 2 - 10, this.height / 2 - 10, 20, 20)
                .build());
        this.moveButton.active = false;

        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                    onDone.accept(selectedList.children());
                })
                .bounds(this.width / 2 - 200 / 2, this.height - 48, 200, 20)
                .build());
    }

    private void updateMoveButton() {
        if (this.moveButton != null) {
            boolean hasSelection = availableList.getSelected() != null || selectedList.getSelected() != null;
            this.moveButton.active = hasSelection;

            if (availableList.getSelected() != null) {
                this.moveButton.setMessage(Component.literal("▶"));
            } else if (selectedList.getSelected() != null) {
                this.moveButton.setMessage(Component.literal("◀"));
            }
        }
    }

    public void clearSelected() {
        this.selectedList.setSelected(null);
        this.availableList.setSelected(null);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.availableList.render(guiGraphics, mouseX, mouseY, partialTick);
        this.selectedList.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    public class TransferableStorySelectionList extends ObjectSelectionList<TransferableStorySelectionList.Entry> {

        private TransferableStorySelectionList otherList;
        private final Component title;
        private final PickElementScreen screen;

        public TransferableStorySelectionList(
                Minecraft minecraft,
                PickElementScreen screen,
                List<? extends NarrativeEntry> narrativeEntries,
                int width,
                int height,
                Component title) {
            super(minecraft, width, height, 32, height - 55 + 4, 18);
            this.screen = screen;
            this.title = title;
            this.centerListVertically = false;
            this.setRenderHeader(true, 13);

            for (NarrativeEntry narrativeEntry : narrativeEntries) {
                this.addEntry(new Entry(narrativeEntry));
            }
        }

        public void setOtherList(TransferableStorySelectionList otherList) {
            this.otherList = otherList;
        }

        public void setLeftPos(int x) {
            this.x0 = x;
            this.x1 = x + this.width;
        }

        @Override
        protected void renderHeader(GuiGraphics guiGraphics, int x, int y) {
            Component component = Component.empty().append(this.title).withStyle(ChatFormatting.UNDERLINE);
            guiGraphics.drawString(
                    this.minecraft.font,
                    component,
                    x + this.width / 2 - this.minecraft.font.width(component) / 2,
                    Math.min(this.y0 + 3, y),
                    16777215,
                    false);
        }

        @Override
        public int getRowWidth() {
            return this.width;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.x1 - 6;
        }

        @Override
        public void setSelected(@Nullable Entry selected) {
            super.setSelected(selected);
            if (otherList != null && selected != null) {
                otherList.setSelected(null);
            }
            PickElementScreen.this.updateMoveButton();
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {

            private final NarrativeEntry narrativeEntry;

            public Entry(NarrativeEntry narrativeEntry) {
                this.narrativeEntry = narrativeEntry;
            }

            @Override
            public Component getNarration() {
                return Component.literal(narrativeEntry.getName());
            }

            @Override
            public void render(
                    GuiGraphics guiGraphics,
                    int index,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,
                    int mouseY,
                    boolean hovering,
                    float partialTick) {

                int textWidth = minecraft.font.width(narrativeEntry.getName());
                int centeredX = left + (width - textWidth) / 2;

                guiGraphics.drawString(minecraft.font, narrativeEntry.getName(), centeredX, top + 2, 16777215);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    TransferableStorySelectionList.this.setSelected(this);
                    PickElementScreen.this.clearSelected();
                    TransferableStorySelectionList.this.setSelected(this);
                    return true;
                }
                return false;
            }

            public NarrativeEntry getNarrativeEntry() {
                return narrativeEntry;
            }
        }
    }
}
