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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class GenericSelectionScreen<T extends NarrativeEntry> extends Screen {
    protected final List<T> itemList;
    protected final Consumer<T> consumer;
    protected final T currentSelection;
    protected final String screenTitle;
    protected final Screen lastScreen;
    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

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
        this.addTitle();
        this.addContents();
        this.addFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected void addTitle() {
        LinearLayout linearlayout =
                this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));

        addCustomTitleButtons(linearlayout);
    }

    protected void addCustomTitleButtons(LinearLayout layout) {}

    protected void addContents() {
        this.selectionList = this.layout.addToContents(new SelectionList<>(this.minecraft, this));
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, p_345997_ -> this.onClose())
                .width(200)
                .build());
    }

    protected void repositionElements() {
        this.layout.arrangeElements();
        this.selectionList.updateSize(this.width, this.layout);
    }

    @Override
    public void onClose() {
        SelectionList<T>.Entry entry = this.selectionList.getSelected();
        if (entry == null) {
            consumer.accept(null);
        } else {
            consumer.accept(entry.getItem());
            minecraft.setScreen(lastScreen);
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

        public SelectionList(Minecraft minecraft, GenericSelectionScreen<T> parentScreen) {
            super(minecraft, parentScreen.width, parentScreen.height - 33 - 53, 33, 18);
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
            public void render(
                    GuiGraphics guiGraphics,
                    int x,
                    int y,
                    int width,
                    int height,
                    int mouseX,
                    int mouseY,
                    int i6,
                    boolean isSelected,
                    float partialTick) {
                String displayName = this.item.getName();
                guiGraphics.drawCenteredString(parentScreen.font, displayName, SelectionList.this.width / 2, y + 3, -1);
            }

            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (CommonInputs.selected(keyCode)) {
                    this.select();
                    parentScreen.onClose();
                    return true;
                } else {
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            }

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
