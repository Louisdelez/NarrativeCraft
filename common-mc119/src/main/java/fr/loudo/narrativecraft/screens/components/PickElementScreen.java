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

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.util.ScreenUtils;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * MC 1.19.x version of PickElementScreen.
 * Uses manual positioning (no layout APIs in 1.19.x).
 * ObjectSelectionList uses 6-param constructor.
 * Entry.render uses PoseStack instead of GuiGraphics.
 */
public class PickElementScreen extends Screen {

    private static final Component AVAILABLE_TITLE = Component.translatable("pack.available.title");
    private static final Component SELECTED_TITLE = Component.translatable("pack.selected.title");
    private final Screen lastScreen;
    private final List<? extends NarrativeEntry> narrativeEntry1, narrativeEntry2;
    private Button moveButton, doneButton;
    private TransferableStorySelectionList availableList, selectedList;
    private StringWidget availableString, selectedString, headTitle;
    private Component availableMessage, selectedMessage, selector;
    private int initialY;
    Consumer<List<TransferableStorySelectionList.Entry>> onDone;

    public PickElementScreen(
            Screen lastScreen,
            Component title,
            Component selector,
            List<? extends NarrativeEntry> narrativeEntry1,
            List<? extends NarrativeEntry> narrativeEntry2,
            Consumer<List<TransferableStorySelectionList.Entry>> onDone) {
        super(title);
        this.selector = selector;
        this.narrativeEntry1 = narrativeEntry1;
        this.narrativeEntry2 = narrativeEntry2;
        this.onDone = onDone;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int headerHeight = 50;
        int footerHeight = 40;
        int listHeight = this.height - headerHeight - footerHeight;

        this.availableList = new TransferableStorySelectionList(
                this.minecraft, narrativeEntry1, 200, listHeight, headerHeight, this.height - footerHeight);
        this.selectedList = new TransferableStorySelectionList(
                this.minecraft, narrativeEntry2, 200, listHeight, headerHeight, this.height - footerHeight);

        this.addWidget(this.availableList);
        this.addWidget(this.selectedList);

        this.availableList.setOtherList(selectedList);
        this.selectedList.setOtherList(availableList);
        this.moveButton = this.addRenderableWidget(Button.builder(Component.literal("\u25C0"), button -> {
                    if (availableList.getSelected() != null) {
                        TransferableStorySelectionList.Entry selected = availableList.getSelected();
                        availableList.removeEntryPublic(selected);
                        selectedList.addEntryPublic(selected);
                        selectedList.setSelected(selected);
                        availableList.setSelected(null);
                        moveButton.setMessage(Component.literal("\u25C0"));
                    } else if (selectedList.getSelected() != null) {
                        TransferableStorySelectionList.Entry selected = selectedList.getSelected();
                        selectedList.removeEntryPublic(selected);
                        availableList.addEntryPublic(selected);
                        availableList.setSelected(selected);
                        selectedList.setSelected(null);
                        moveButton.setMessage(Component.literal("\u25B6"));
                    }
                    // refreshScrollAmount() doesn't exist in 1.19.x - use setScrollAmount(0) as alternative
                    availableList.setScrollAmount(0);
                    selectedList.setScrollAmount(0);
                    moveButton.active = availableList.getSelected() != null || selectedList.getSelected() != null;
                })
                .width(20)
                .build());

        moveButton.active = false;
        availableMessage = Component.literal(selector.getString() + " " + AVAILABLE_TITLE.getString());
        selectedMessage = Component.literal(selector.getString() + " " + SELECTED_TITLE.getString());
        availableString = this.addRenderableWidget(ScreenUtils.text(availableMessage, this.font, 0, 0));
        selectedString = this.addRenderableWidget(ScreenUtils.text(selectedMessage, this.font, 0, 0));
        doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                    onDone.accept(selectedList.children());
                })
                .width(200)
                .build());
        headTitle = this.addRenderableWidget(ScreenUtils.text(title, this.font, 0, 0));
        initialY = headerHeight + 15;
        repositionElements();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        this.availableList.render(poseStack, mouseX, mouseY, partialTick);
        this.selectedList.render(poseStack, mouseX, mouseY, partialTick);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    protected void repositionElements() {
        headTitle.setPosition(this.width / 2 - this.font.width(title) / 2, 12);
        availableList.setX(this.width / 2 - 15 - 200);
        availableList.setY(initialY);
        selectedList.setX(this.width / 2 + 15);
        selectedList.setY(initialY);
        moveButton.setPosition(
                this.width / 2 - moveButton.getWidth() / 2,
                initialY + availableList.getHeight() / 2 - moveButton.getHeight() / 2);
        availableString.setPosition(
                availableList.getX() + availableList.getWidth() / 2 - this.font.width(availableMessage) / 2,
                availableList.getY() - 15);
        selectedString.setPosition(
                selectedList.getX() + selectedList.getWidth() / 2 - this.font.width(selectedMessage) / 2,
                selectedList.getY() - 15);
        doneButton.setPosition(this.width / 2 - 200 / 2, this.height - 25);
        // refreshScrollAmount() doesn't exist in 1.19.x
        availableList.setScrollAmount(0);
        selectedList.setScrollAmount(0);
    }

    public class TransferableStorySelectionList extends ObjectSelectionList<TransferableStorySelectionList.Entry> {

        private TransferableStorySelectionList otherList;
        private int listX, listY;
        private final int listWidth;
        private final int listHeight;

        public TransferableStorySelectionList(
                Minecraft minecraft, List<? extends NarrativeEntry> narrativeEntries,
                int width, int height, int y0, int y1) {
            // 1.19.x: ObjectSelectionList(minecraft, width, height, y0, y1, itemHeight)
            super(minecraft, width, PickElementScreen.this.height, y0, y1, 18);
            this.listWidth = width;
            this.listHeight = height;
            for (NarrativeEntry narrativeEntry : narrativeEntries) {
                Entry entry = new Entry(narrativeEntry);
                this.addEntry(entry);
            }
        }

        @Override
        public int getRowWidth() {
            return this.listWidth - 4;
        }

        public int getWidth() {
            return this.listWidth;
        }

        public int getHeight() {
            return this.listHeight;
        }

        public int getX() {
            return this.listX;
        }

        public int getY() {
            return this.listY;
        }

        public void setX(int x) {
            this.listX = x;
            this.setLeftPos(x);
        }

        public void setY(int y) {
            this.listY = y;
        }

        // 1.19.x: removeEntry is protected and returns boolean - expose via public method
        public boolean removeEntryPublic(Entry entry) {
            return super.removeEntry(entry);
        }

        // 1.19.x: addEntry returns int - already public in superclass
        public int addEntryPublic(Entry entry) {
            return super.addEntry(entry);
        }

        public void setOtherList(TransferableStorySelectionList otherList) {
            this.otherList = otherList;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.listX + this.listWidth - 6;
        }

        @Override
        public void setSelected(@Nullable PickElementScreen.TransferableStorySelectionList.Entry selected) {
            super.setSelected(selected);
            if (otherList != null && selected != null) {
                otherList.setSelected(null);
                if (Objects.equals(otherList, PickElementScreen.this.selectedList)) {
                    PickElementScreen.this.moveButton.setMessage(Component.literal("\u25B6"));
                } else {
                    PickElementScreen.this.moveButton.setMessage(Component.literal("\u25C0"));
                }
                PickElementScreen.this.moveButton.active = true;
            }
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                    int mouseX, int mouseY, boolean hovering, float partialTick) {
                GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
                int centerX = left + width / 2;
                guiGraphics.drawCenteredString(
                        minecraft.font, narrativeEntry.getName(), centerX, top + height / 2 - 9 / 2, -1);
            }

            public NarrativeEntry getNarrativeEntry() {
                return narrativeEntry;
            }
        }
    }
}
