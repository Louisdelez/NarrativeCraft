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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CrashScreen extends Screen {

    private static final ResourceLocation WINDOW_LOCATION =
            ResourceLocation.withDefaultNamespace("textures/gui/advancements/window.png");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final PlayerSession playerSession;
    private final String message;

    public CrashScreen(PlayerSession playerSession, String message) {
        super(Component.literal("Crash Screen"));
        this.playerSession = playerSession;
        this.message = message;
    }

    @Override
    public void onClose() {
        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler != null && !storyHandler.isDebugMode()) {
            NarrativeWorldOption worldOption = NarrativeCraftMod.getInstance().getNarrativeWorldOption();
            if (worldOption.showMainScreen) {
                MainScreen mainScreen = new MainScreen(playerSession, false, false);
                minecraft.setScreen(mainScreen);
            }
        }
    }

    @Override
    protected void init() {
        LinearLayout linearLayout =
                this.layout.addToFooter(LinearLayout.horizontal().spacing(4));
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, (p_331557_) -> this.onClose())
                .width(130)
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
        List<String> lines = List.of(
                Translation.message("screen.crash.message", message).getString().split("\n"));
        int textPosY = offsetY + 18 + 56 - 4 - (minecraft.font.lineHeight * lines.size() + 2) / 2;
        guiGraphics.drawCenteredString(
                this.font, Translation.message("screen.crash.title").getString(), i, textPosY, -1);
        for (String line : lines) {
            textPosY += minecraft.font.lineHeight;
            guiGraphics.drawCenteredString(this.font, line, i, textPosY, -1);
        }
    }

    public void renderWindow(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED, WINDOW_LOCATION, offsetX, offsetY, 0.0F, 0.0F, 252, 140, 256, 256);
    }
}
