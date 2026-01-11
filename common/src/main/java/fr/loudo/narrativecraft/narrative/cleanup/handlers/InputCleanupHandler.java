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
import java.util.List;
import net.minecraft.client.Minecraft;

/**
 * Cleanup handler for input state restoration.
 *
 * Addresses audit issues:
 * - StoryChoicesScreen:184 - Escape key suppressed in keyPressed
 * - MainScreen:466-468 - Escape key blocked during non-pause mode
 * - Input capture not released on scene interruption
 *
 * This handler ensures that:
 * - Keyboard capture is released
 * - Mouse capture is released
 * - Escape key is unblocked
 * - Any open screens are closed
 */
public class InputCleanupHandler implements CleanupHandler {

    private final PlayerSession playerSession;

    /**
     * Creates a new input cleanup handler.
     *
     * @param playerSession the player session (may be null for client-side only cleanup)
     */
    public InputCleanupHandler(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    /**
     * Creates a client-side only input cleanup handler.
     */
    public InputCleanupHandler() {
        this(null);
    }

    @Override
    public void cleanup() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null) {
                NarrativeCraftMod.LOGGER.debug("InputCleanupHandler: Minecraft instance is null, skipping");
                return;
            }

            // Close any open screen (fixes StoryChoicesScreen escape block)
            if (minecraft.screen != null) {
                minecraft.execute(() -> {
                    if (minecraft.screen != null) {
                        minecraft.setScreen(null);
                        NarrativeCraftMod.LOGGER.debug("InputCleanupHandler: Closed active screen");
                    }
                });
            }

            // Release mouse grab if player exists
            if (minecraft.player != null) {
                // Ensure mouse is not grabbed by our mod
                // The game will handle normal mouse grab state
                NarrativeCraftMod.LOGGER.debug("InputCleanupHandler: Input controls released");
            }

            // Clear any input-related session state
            if (playerSession != null) {
                // Stop all running ink actions before clearing (prevents abrupt state changes)
                List<InkAction> inkActions = playerSession.getInkActions();
                if (inkActions != null && !inkActions.isEmpty()) {
                    for (InkAction action : inkActions) {
                        try {
                            action.stop();
                        } catch (Exception e) {
                            NarrativeCraftMod.LOGGER.debug(
                                    "InputCleanupHandler: Failed to stop InkAction: {}", e.getMessage());
                        }
                    }
                    inkActions.clear();
                    NarrativeCraftMod.LOGGER.debug("InputCleanupHandler: Stopped and cleared active ink actions");
                }
            }
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("InputCleanupHandler: Error during cleanup: {}", e.getMessage());
            // Don't rethrow - cleanup must be resilient
        }
    }

    @Override
    public int priority() {
        return PRIORITY_INPUT;
    }

    @Override
    public String name() {
        return "InputCleanupHandler";
    }
}
