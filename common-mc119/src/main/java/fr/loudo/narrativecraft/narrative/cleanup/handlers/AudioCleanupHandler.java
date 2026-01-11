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
import fr.loudo.narrativecraft.narrative.story.inkAction.sound.SoundInkAction;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreen;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Cleanup handler for audio state restoration.
 * MC 1.20.x version - uses musicInstance field directly instead of getMusicInstance() method.
 *
 * This handler ensures that:
 * - All narrative-related sounds are stopped
 * - Main screen music is stopped if playing
 * - Any active SoundInkActions are terminated
 * - Ambient sounds triggered by narrative are cleaned up
 */
public class AudioCleanupHandler implements CleanupHandler {

    private final PlayerSession playerSession;

    /**
     * Creates a new audio cleanup handler.
     *
     * @param playerSession the player session (may be null for client-side only cleanup)
     */
    public AudioCleanupHandler(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    /**
     * Creates a client-side only audio cleanup handler.
     */
    public AudioCleanupHandler() {
        this(null);
    }

    @Override
    public void cleanup() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null) {
                NarrativeCraftMod.LOGGER.debug("AudioCleanupHandler: Minecraft instance is null, skipping");
                return;
            }

            // Stop main screen music if playing
            // 1.20.x: Use musicInstance field directly instead of getMusicInstance() method
            if (minecraft.getSoundManager() != null) {
                try {
                    minecraft.getSoundManager().stop(MainScreen.musicInstance);
                    NarrativeCraftMod.LOGGER.debug("AudioCleanupHandler: Stopped main screen music");
                } catch (Exception e) {
                    NarrativeCraftMod.LOGGER.debug("AudioCleanupHandler: Main screen music was not playing");
                }
            }

            // Stop all active SoundInkActions from session
            if (playerSession != null && playerSession.getInkActions() != null) {
                List<InkAction> inkActions = playerSession.getInkActions();
                for (InkAction action : inkActions) {
                    if (action instanceof SoundInkAction soundAction) {
                        try {
                            soundAction.stop();
                            NarrativeCraftMod.LOGGER.debug("AudioCleanupHandler: Stopped SoundInkAction");
                        } catch (Exception e) {
                            NarrativeCraftMod.LOGGER.debug("AudioCleanupHandler: Failed to stop SoundInkAction: {}", e.getMessage());
                        }
                    }
                }
            }

            NarrativeCraftMod.LOGGER.debug("AudioCleanupHandler: Audio cleanup completed");
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("AudioCleanupHandler: Error during cleanup: {}", e.getMessage());
            // Don't rethrow - cleanup must be resilient
        }
    }

    @Override
    public int priority() {
        return PRIORITY_AUDIO;
    }

    @Override
    public String name() {
        return "AudioCleanupHandler";
    }
}
