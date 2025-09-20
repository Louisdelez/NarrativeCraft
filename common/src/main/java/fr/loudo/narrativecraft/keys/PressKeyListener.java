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

package fr.loudo.narrativecraft.keys;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.storyManager.chapter.ChaptersScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesMenuScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.Minecraft;

public class PressKeyListener {

    public static void onPressKey(Minecraft minecraft) {
        ModKeys.handleKeyPress(ModKeys.OPEN_STORY_MANAGER, () -> {
            if (!minecraft.player.hasPermissions(2)) return;
            PlayerSession playerSession =
                    NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(minecraft.player);
            if (playerSession == null) return;
            if (playerSession.getStoryHandler() != null) return;
            if (playerSession.getController() != null) {
                playerSession.getPlayer().sendSystemMessage(Translation.message("session.controller_set"));
                return;
            }
            if (playerSession.isSessionSet()) {
                minecraft.setScreen(new ScenesMenuScreen(playerSession.getScene()));
            } else {
                minecraft.setScreen(new ChaptersScreen());
            }
        });
        ModKeys.handleKeyPress(ModKeys.OPEN_CONTROLLER_SCREEN, () -> {
            PlayerSession playerSession =
                    NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(minecraft.player);
            if (playerSession == null) return;
            if (playerSession.getStoryHandler() != null) return;
            if (playerSession.getController() == null) return;
            minecraft.setScreen(playerSession.getController().getControllerScreen());
        });
        ModKeys.handleKeyPress(InputConstants.MOUSE_BUTTON_LEFT, minecraft.mouseHandler.isLeftPressed(), () -> {
            PlayerSession playerSession =
                    NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(minecraft.player);
            nextStory(playerSession);
        });
        ModKeys.handleKeyPress(ModKeys.NEXT_DIALOG, () -> {
            PlayerSession playerSession =
                    NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(minecraft.player);
            nextStory(playerSession);
        });
        ModKeys.handleKeyPress(ModKeys.STORY_DEBUG, () -> {
            PlayerSession playerSession =
                    NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(minecraft.player);
            if (playerSession == null) return;
            StoryHandler storyHandler = playerSession.getStoryHandler();
            if (storyHandler == null) return;
            playerSession.setShowDebugHud(!playerSession.isShowDebugHud());
        });
    }

    private static void nextStory(PlayerSession playerSession) {
        if (playerSession == null) return;
        DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
        if (dialogRenderer == null) return;
        if (dialogRenderer.isAnimating()) return;
        if (dialogRenderer.isNoSkip()) return;
        if (!dialogRenderer.getDialogScrollText().isFinished()) {
            dialogRenderer.getDialogScrollText().forceFinish();
            return;
        }
        if (playerSession.getStoryHandler() == null) return;
        StoryHandler storyHandler = playerSession.getStoryHandler();
        NarrativeCraftMod.server.execute(storyHandler::next);
    }
}
