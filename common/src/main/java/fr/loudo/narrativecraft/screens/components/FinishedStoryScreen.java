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

import fr.loudo.narrativecraft.compat.api.IGuiRenderCompat;
import fr.loudo.narrativecraft.compat.api.NcId;
import fr.loudo.narrativecraft.compat.api.VersionAdapterLoader;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FinishedStoryScreen extends Screen {

    private static final NcId WINDOW_LOCATION =
            NcId.of("minecraft", "textures/gui/advancements/window.png");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen oldScreen;

    public FinishedStoryScreen(Screen oldScreen) {
        super(Translation.message("screen.finished_story.title"));
        this.oldScreen = oldScreen;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(oldScreen);
    }

    @Override
    protected void init() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (p_331557_) -> this.onClose())
                .width(200)
                .build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        super.render(guiGraphics, x, y, partialTick);
        int i = (this.width - 252) / 2;
        int j = (this.height - 140) / 2;
        this.renderInside(guiGraphics, x, y, i, j);
        this.renderWindow(guiGraphics, i, j);
    }

    private void renderInside(GuiGraphics guiGraphics, int mouseX, int mouseY, int offsetX, int offsetY) {
        int i = offsetX + 9 + 117;
        guiGraphics.fill(offsetX + 9, offsetY + 18, offsetX + 9 + 234, offsetY + 18 + 113, -16777216);
        int textPosY = offsetY + 18 + 56 - 4;
        guiGraphics.drawCenteredString(
                this.font,
                Translation.message("screen.finished_story.line_1"),
                i,
                textPosY - minecraft.font.lineHeight / 2 - 2,
                -1);
        guiGraphics.drawCenteredString(
                this.font,
                Translation.message("screen.finished_story.line_2"),
                i,
                textPosY + minecraft.font.lineHeight / 2 + 2,
                -1);
    }

    public void renderWindow(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        IGuiRenderCompat guiCompat = VersionAdapterLoader.getAdapter().getGuiRenderCompat();
        guiCompat.blitTexture(guiGraphics, WINDOW_LOCATION.toString(), offsetX, offsetY, 0.0F, 0.0F, 252, 140, 256, 256);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
