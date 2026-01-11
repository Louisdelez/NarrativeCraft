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
import fr.loudo.narrativecraft.compat.api.IGuiRenderCompat;
import fr.loudo.narrativecraft.compat.api.VersionAdapterLoader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

/**
 * MC 1.19.x version of AbstractTextBoxScreen.
 * LinearLayout and HeaderAndFooterLayout don't exist in 1.19.x.
 * Uses manual widget positioning instead.
 */
public abstract class AbstractTextBoxScreen extends Screen {

    // 1.19.x: Use constructor instead of withDefaultNamespace()
    private static final ResourceLocation WINDOW_LOCATION =
            new ResourceLocation("textures/gui/advancements/window.png");

    protected AbstractTextBoxScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        // 1.19.x: No LinearLayout, manually position the Done button at the bottom
        int buttonWidth = 130;
        int buttonHeight = 20;
        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(this.width / 2 - buttonWidth / 2, this.height - buttonHeight - 10, buttonWidth, buttonHeight)
                .build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        int i = (this.width - 252) / 2;
        int j = (this.height - 140) / 2;

        renderInsideBox(guiGraphics, i, j);

        renderWindow(guiGraphics, i, j);
    }

    private void renderInsideBox(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        int boxX = offsetX + 9;
        int boxY = offsetY + 18;
        int boxWidth = 234;
        int boxHeight = 113;

        int centerX = boxX + boxWidth / 2;

        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF000000);

        List<String> lines = renderContent();
        List<FormattedCharSequence> sequences = new ArrayList<>();
        for (String line : lines) {
            List<FormattedCharSequence> splits = minecraft.font.split(FormattedText.of(line), 200);
            sequences.addAll(splits);
        }
        int totalTextHeight = sequences.size() * minecraft.font.lineHeight;
        int startY = boxY + (boxHeight - totalTextHeight) / 2;

        int currentY = startY;
        PoseStack poseStack = guiGraphics.pose();
        for (FormattedCharSequence line : sequences) {
            // 1.19.x: Draw centered using font.draw directly
            int textWidth = minecraft.font.width(line);
            minecraft.font.draw(poseStack, line, centerX - textWidth / 2, currentY, -1);
            currentY += minecraft.font.lineHeight;
        }
    }

    public void renderWindow(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        IGuiRenderCompat guiCompat = VersionAdapterLoader.getAdapter().getGuiRenderCompat();
        guiCompat.blitTexture(guiGraphics, WINDOW_LOCATION.toString(), offsetX, offsetY, 0f, 0f, 252, 140, 256, 256);
    }

    protected abstract List<String> renderContent();
}
