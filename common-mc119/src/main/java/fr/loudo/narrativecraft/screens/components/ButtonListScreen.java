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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * MC 1.19.x version of ButtonListScreen.
 * Key differences:
 * - No HeaderAndFooterLayout - uses manual layout
 * - No LinearLayout.horizontal() - uses manual button positioning
 */
public abstract class ButtonListScreen extends Screen {

    protected Screen lastScreen;
    protected ObjectListScreen objectListScreen;
    protected Button doneButton;

    public ButtonListScreen(Screen lastScreen, Component title) {
        super(title);
        this.lastScreen = lastScreen;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    protected void addTitle() {
        // Title is drawn in render()
    }

    protected abstract void addContents();

    protected void addFooter() {
        this.doneButton = Button.builder(CommonComponents.GUI_DONE, p_345997_ -> this.onClose())
                .width(200)
                .pos(this.width / 2 - 100, this.height - 28)
                .build();
        this.addRenderableWidget(doneButton);
    }

    @Override
    protected void init() {
        addTitle();
        // Create the object list screen manually - positioned in screen area
        this.objectListScreen = new ObjectListScreen(this.minecraft, this, this.width, this.height - 64, 32);
        addContents();
        addFooter();
        this.addWidget(this.objectListScreen);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
        this.renderBackground(poseStack);

        // Draw title centered at top
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        // Render the list
        if (this.objectListScreen != null) {
            this.objectListScreen.render(poseStack, mouseX, mouseY, partialTick);
        }

        super.render(poseStack, mouseX, mouseY, partialTick);
    }
}
