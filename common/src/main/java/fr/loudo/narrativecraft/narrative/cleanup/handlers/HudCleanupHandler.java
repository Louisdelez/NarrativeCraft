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

package fr.loudo.narrativecraft.narrative.cleanup.handlers;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.cleanup.CleanupHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.inkAction.BorderInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.FadeInkAction;
import java.util.List;
import net.minecraft.client.Minecraft;

/**
 * Cleanup handler for HUD state restoration.
 *
 * Addresses audit issues:
 * - MainScreen:177 - hideGui set without restoration guarantee
 * - DialogCustomOptionsScreen:109 - hideGui modified in init() without finally
 * - CameraAngleOptionsScreen:65 - Empty onClose() prevents parent cleanup
 * - StoryChoicesScreen:231 - Empty onClose() - no widget cleanup
 *
 * This handler ensures that:
 * - hideGui is restored to false
 * - Dialog renderer is cleared
 * - Any active InkActions affecting HUD are stopped
 */
public class HudCleanupHandler implements CleanupHandler {

    private final PlayerSession playerSession;

    /**
     * Creates a new HUD cleanup handler.
     *
     * @param playerSession the player session (may be null for client-side only cleanup)
     */
    public HudCleanupHandler(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    /**
     * Creates a client-side only HUD cleanup handler.
     */
    public HudCleanupHandler() {
        this(null);
    }

    @Override
    public void cleanup() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null) {
                NarrativeCraftMod.LOGGER.debug("HudCleanupHandler: Minecraft instance is null, skipping");
                return;
            }

            // Restore HUD visibility (fixes MainScreen:177, DialogCustomOptionsScreen:109)
            if (minecraft.options != null) {
                minecraft.options.hideGui = false;
                NarrativeCraftMod.LOGGER.debug("HudCleanupHandler: Restored hideGui to false");
            }

            // Clear dialog renderer from session if available
            if (playerSession != null) {
                if (playerSession.getDialogRenderer() != null) {
                    playerSession.setDialogRenderer(null);
                    NarrativeCraftMod.LOGGER.debug("HudCleanupHandler: Cleared dialog renderer");
                }

                // Stop and clear any HUD-related InkActions (border, fade)
                List<InkAction> inkActions = playerSession.getInkActions();
                if (inkActions != null) {
                    for (InkAction action : inkActions) {
                        if (action instanceof BorderInkAction || action instanceof FadeInkAction) {
                            try {
                                action.stop();
                            } catch (Exception e) {
                                NarrativeCraftMod.LOGGER.debug(
                                        "HudCleanupHandler: Failed to stop InkAction: {}", e.getMessage());
                            }
                        }
                    }
                    // Remove stopped HUD actions
                    inkActions.removeIf(action -> action instanceof BorderInkAction || action instanceof FadeInkAction);
                    NarrativeCraftMod.LOGGER.debug("HudCleanupHandler: Cleared HUD-related InkActions");
                }
            }
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("HudCleanupHandler: Error during cleanup: {}", e.getMessage());
            // Don't rethrow - cleanup must be resilient
        }
    }

    @Override
    public int priority() {
        return PRIORITY_HUD;
    }

    @Override
    public String name() {
        return "HudCleanupHandler";
    }
}
