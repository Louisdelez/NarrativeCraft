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

import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.Translation;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * MC 1.19.x version of StoryElementList.
 * Key differences from 1.20.x+:
 * - ContainerObjectSelectionList constructor: (minecraft, width, height, y0, y1, itemHeight)
 * - Entry.render() uses 10-parameter signature instead of renderContent()
 * - No getContentY() method - use 'top' parameter from render() instead
 */
public class StoryElementList extends ContainerObjectSelectionList<StoryElementList.Entry> {

    public StoryElementList(Minecraft minecraft, Screen screen, List<StoryEntryData> entriesData, boolean editButton) {
        // 1.19.x: Constructor is (minecraft, width, height, y0, y1, itemHeight)
        super(minecraft, 240, screen.height, 25, screen.height - 25, 25);
        for (StoryEntryData data : entriesData) {
            this.addEntry(new Entry(data, screen, editButton));
        }
    }

    public static class StoryEntryData {
        public final Button mainButton;
        public List<Button> extraButtons;
        public Runnable onDelete;
        public Runnable onUpdate;

        public StoryEntryData(Button mainButton, List<Button> extraButtons, Runnable onUpdate, Runnable onDelete) {
            this.mainButton = mainButton;
            this.extraButtons = extraButtons;
            this.onUpdate = onUpdate;
            this.onDelete = onDelete;
        }

        public StoryEntryData(Button mainButton, Runnable onUpdate, Runnable onDelete) {
            this.mainButton = mainButton;
            this.onUpdate = onUpdate;
            this.onDelete = onDelete;
        }

        public StoryEntryData(Button mainButton) {
            this.mainButton = mainButton;
        }
    }

    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final int gap = 5;
        private final Button mainButton;
        private final List<Button> buttons;
        private final Screen screen;

        public Entry(StoryEntryData data, Screen screen, boolean editButton) {
            this.screen = screen;
            this.mainButton = data.mainButton;
            this.buttons = new ArrayList<>();
            buttons.add(mainButton);

            if (data.onUpdate != null && editButton) {
                buttons.add(createEditButton(data.onUpdate));
                buttons.add(createRemoveButton(data.onDelete));
            }

            if (data.extraButtons != null) {
                data.extraButtons.forEach(button -> {
                    button.setWidth(20);
                });
                buttons.addAll(data.extraButtons);
            }
        }

        private Button createEditButton(Runnable onUpdate) {
            return Button.builder(ImageFontConstants.EDIT, btn -> {
                        onUpdate.run();
                    })
                    .width(20)
                    .build();
        }

        private Button createRemoveButton(Runnable onDelete) {
            return Button.builder(ImageFontConstants.REMOVE, btn -> {
                        ConfirmScreen confirm = new ConfirmScreen(
                                b -> {
                                    if (b) {
                                        onDelete.run();
                                    } else {
                                        Minecraft.getInstance().setScreen(screen);
                                    }
                                },
                                Component.literal(""),
                                Translation.message("global.confirm_delete"),
                                CommonComponents.GUI_YES,
                                CommonComponents.GUI_CANCEL);
                        Minecraft.getInstance().setScreen(confirm);
                    })
                    .width(20)
                    .build();
        }

        /**
         * 1.19.x: render() takes PoseStack, not GuiGraphics.
         * The 'top' parameter provides the Y position instead of getContentY().
         */
        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                          int mouseX, int mouseY, boolean hovering, float partialTick) {
            // Create a GuiGraphics wrapper for compatibility
            GuiGraphics guiGraphics = new GuiGraphics(Minecraft.getInstance(), poseStack);
            int totalWidth = buttons.stream().mapToInt(Button::getWidth).sum() + (buttons.size() - 1) * gap;
            int x = (screen.width / 2 - totalWidth / 2);
            if (buttons.size() > 1) {
                x -= gap;
            }
            for (Button button : buttons) {
                // 1.19.x: Use 'top' parameter instead of getContentY()
                button.setPosition(x, top);
                button.render(poseStack, mouseX, mouseY, partialTick);
                x += button.getWidth() + gap;
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return buttons;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return buttons;
        }
    }
}
