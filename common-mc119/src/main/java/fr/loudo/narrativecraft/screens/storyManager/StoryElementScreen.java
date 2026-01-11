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

package fr.loudo.narrativecraft.screens.storyManager;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * MC 1.19.x version of StoryElementScreen.
 * Key differences:
 * - No HeaderAndFooterLayout - uses manual layout
 * - No LinearLayout.horizontal() - uses manual button positioning
 */
public abstract class StoryElementScreen extends Screen {

    protected StoryElementList storyElementList;
    protected Button addButton;
    protected Button folderButton;
    protected Button doneButton;

    protected StoryElementScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        this.addTitle();
        this.addContents();
        this.addFooter();
    }

    protected void addTitle() {
        // Title is drawn in render(), buttons are positioned manually
        int buttonX = this.width / 2 + 100;
        int buttonY = 8;
        // Buttons will be added by subclasses via initAddButton and initFolderButton
    }

    protected void addFooter() {
        this.doneButton = Button.builder(CommonComponents.GUI_DONE, p_345997_ -> this.onClose())
                .width(200)
                .pos(this.width / 2 - 100, this.height - 28)
                .build();
        this.addRenderableWidget(doneButton);
    }

    protected abstract void addContents();

    protected void initAddButton(Button.OnPress onPress) {
        if (onPress == null) return;
        int titleWidth = this.font.width(this.title);
        addButton = Button.builder(ImageFontConstants.ADD, onPress)
                .width(25)
                .pos(this.width / 2 - titleWidth / 2 + titleWidth + 10, 6)
                .build();
        this.addRenderableWidget(addButton);
    }

    protected void initFolderButton(Button.OnPress addPress) {
        int titleWidth = this.font.width(this.title);
        int offset = addPress != null ? 35 : 0;
        folderButton = Button.builder(ImageFontConstants.FOLDER, button -> openFolder())
                .width(25)
                .pos(this.width / 2 - titleWidth / 2 + titleWidth + 10 + offset, 6)
                .build();
        this.addRenderableWidget(folderButton);
    }

    protected void initFolderButton() {
        initFolderButton(null);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // MC 1.19.x: render takes PoseStack, create GuiGraphics wrapper for drawing
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
        this.renderBackground(poseStack);

        // Draw title centered at top
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        // Render the list
        if (this.storyElementList != null) {
            this.storyElementList.render(poseStack, mouseX, mouseY, partialTick);
        }

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    protected abstract void openFolder();
}
