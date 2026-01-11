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
import fr.loudo.narrativecraft.narrative.cleanup.CleanupHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

/**
 * Cleanup handler for camera state restoration.
 *
 * Addresses audit issues:
 * - CameraMixin:60 - Minecraft.getInstance().player may be null
 * - Cutscene playback camera lock not restored after interruption
 * - Player currentCamera field persisting after scene exit
 *
 * This handler ensures that:
 * - Camera is returned to first-person player control
 * - currentCamera reference is cleared
 * - Any active cutscene playback is stopped
 */
public class CameraCleanupHandler implements CleanupHandler {

    private final PlayerSession playerSession;

    /**
     * Creates a new camera cleanup handler.
     *
     * @param playerSession the player session (may be null for client-side only cleanup)
     */
    public CameraCleanupHandler(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    /**
     * Creates a client-side only camera cleanup handler.
     */
    public CameraCleanupHandler() {
        this(null);
    }

    @Override
    public void cleanup() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null) {
                NarrativeCraftMod.LOGGER.debug("CameraCleanupHandler: Minecraft instance is null, skipping");
                return;
            }

            // Null check for player (fixes CameraMixin:60)
            if (minecraft.player == null) {
                NarrativeCraftMod.LOGGER.debug(
                        "CameraCleanupHandler: Player is null, skipping player-specific cleanup");
            }

            // Restore camera to first-person mode
            if (minecraft.options != null) {
                minecraft.options.setCameraType(CameraType.FIRST_PERSON);
                NarrativeCraftMod.LOGGER.debug("CameraCleanupHandler: Restored camera to FIRST_PERSON");
            }

            // Clear currentCamera from session if available
            if (playerSession != null) {
                if (playerSession.getCurrentCamera() != null) {
                    playerSession.setCurrentCamera(null);
                    NarrativeCraftMod.LOGGER.debug("CameraCleanupHandler: Cleared currentCamera reference");
                }

                // Stop any active controller that may be locking the camera
                if (playerSession.getController() != null) {
                    try {
                        NarrativeCraftMod.server.execute(() -> {
                            if (playerSession.getController() != null) {
                                playerSession.getController().stopSession(false);
                            }
                        });
                        NarrativeCraftMod.LOGGER.debug("CameraCleanupHandler: Requested controller stop");
                    } catch (Exception e) {
                        NarrativeCraftMod.LOGGER.warn(
                                "CameraCleanupHandler: Failed to stop controller: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("CameraCleanupHandler: Error during cleanup: {}", e.getMessage());
            // Don't rethrow - cleanup must be resilient
        }
    }

    @Override
    public int priority() {
        return PRIORITY_CAMERA;
    }

    @Override
    public String name() {
        return "CameraCleanupHandler";
    }
}
