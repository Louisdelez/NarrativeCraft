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

package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;

public class OnHudRender {
    public static void controllerHudInfo(GuiGraphics guiGraphics) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(Minecraft.getInstance().player);
        if (playerSession == null) return;
        if (playerSession.getController() == null) return;
        playerSession.getController().renderHUDInfo(guiGraphics);
    }

    public static void inkActionRender(GuiGraphics guiGraphics, float partialTick) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(Minecraft.getInstance().player);
        if (playerSession == null) return;
        List<InkAction> inkActionsClient = playerSession.getClientSideInkActions();
        for (InkAction inkAction : inkActionsClient) {
            inkAction.render(guiGraphics, partialTick);
        }
    }

    public static void dialogRender(GuiGraphics guiGraphics, float partialTick) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(Minecraft.getInstance().player);
        if (playerSession == null) return;
        if (playerSession.getDialogRenderer() != null) {
            playerSession.getDialogRenderer().render(guiGraphics, partialTick);
        }
    }

    public static void saveIconRender(GuiGraphics guiGraphics, float partialTick) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(Minecraft.getInstance().player);
        if (playerSession == null) return;
        playerSession.getStorySaveIconGui().render(guiGraphics, partialTick);
    }

    public static void storyDebugRender(GuiGraphics guiGraphics, float partialTick) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(Minecraft.getInstance().player);
        if (playerSession == null) return;
        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler == null) return;
        if (!storyHandler.isDebugMode()) return;
        String debugText = "Debug mode";
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(
                font, debugText, guiGraphics.guiWidth() - font.width(debugText) - 5, 5, ARGB.color(255, 255, 255, 255));
        if (!playerSession.isShowDebugHud()) return;
        storyHandler.getStoryDebugHud().render(guiGraphics, partialTick);
    }
}
