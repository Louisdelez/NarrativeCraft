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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class StoryElementList extends ObjectSelectionList<StoryElementList.Entry> {

    public StoryElementList(Minecraft minecraft, Screen screen, List<StoryEntryData> entriesData, boolean editButton) {
        super(minecraft, screen.width, screen.height, 32, screen.height - 65, 24);
        this.setRenderSelection(false);
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

    public static class Entry extends ObjectSelectionList.Entry<Entry> {
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

        @Override
        public void render(
                GuiGraphics graphics,
                int index,
                int top,
                int left,
                int width,
                int height,
                int mouseX,
                int mouseY,
                boolean hovered,
                float partial) {
            int totalWidth = buttons.stream().mapToInt(Button::getWidth).sum() + (buttons.size() - 1) * gap;
            int x = (screen.width / 2 - totalWidth / 2);
            if (buttons.size() > 1) {
                x -= gap;
            }
            for (Button button : buttons) {
                button.setPosition(x, top);
                button.render(graphics, mouseX, mouseY, partial);
                x += button.getWidth() + gap;
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            for (Button btn : buttons) {
                if (btn.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            for (Button btn : buttons) {
                btn.mouseReleased(mouseX, mouseY, button);
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            for (Button btn : buttons) {
                btn.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return mainButton.getMessage();
        }
    }
}
