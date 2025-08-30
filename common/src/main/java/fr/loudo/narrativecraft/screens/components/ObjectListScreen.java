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

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

public class ObjectListScreen extends ContainerObjectSelectionList<ObjectListScreen.Entry> {

    private static final int BUTTON_WIDTH = 170;
    private final Screen screen;

    public ObjectListScreen(Minecraft minecraft, Screen screen, int width, int contentHeight, int headerHeight) {
        super(minecraft, width, contentHeight, headerHeight, 25);
        this.centerListVertically = false;
        this.screen = screen;
    }

    public void addButton(AbstractWidget button) {
        button.setWidth(BUTTON_WIDTH);
        this.addEntry(new Entry(button, screen));
    }

    protected static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final AbstractWidget children;
        private final Screen screen;

        Entry(AbstractWidget children, Screen screen) {
            this.children = children;
            this.screen = screen;
        }

        // GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean
        // isHovered, int partialTick
        public void render(
                GuiGraphics p_281311_,
                int p_94497_,
                int p_94498_,
                int p_94499_,
                int p_94500_,
                int p_94501_,
                int p_94502_,
                int p_94503_,
                boolean p_94504_,
                float p_94505_) {

            children.setPosition(this.screen.width / 2 - (children.getWidth() / 2), p_94498_);
            children.render(p_281311_, p_94502_, p_94503_, p_94505_);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(children);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(children);
        }
    }
}
