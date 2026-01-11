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

package fr.loudo.narrativecraft.narrative.cleanup;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.cleanup.handlers.AudioCleanupHandler;
import fr.loudo.narrativecraft.narrative.cleanup.handlers.CameraCleanupHandler;
import fr.loudo.narrativecraft.narrative.cleanup.handlers.HudCleanupHandler;
import fr.loudo.narrativecraft.narrative.cleanup.handlers.InputCleanupHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.state.NarrativeStateManager;
import net.minecraft.client.Minecraft;

/**
 * Central service for managing narrative state cleanup.
 *
 * This service provides:
 * - Registration of cleanup handlers when entering narrative states
 * - Force cleanup on disconnect/world change
 * - Guaranteed state restoration on any exit path
 *
 * Addresses audit issues:
 * - MainScreen:177 - hideGui not restored
 * - CameraMixin:60 - camera state persisting
 * - StoryChoicesScreen:184 - input stuck
 */
public class NarrativeCleanupService {

    private static volatile boolean cleanupInProgress = false;

    /**
     * Registers all standard cleanup handlers for a narrative session.
     * Should be called when entering any narrative state (dialogue, cutscene, playback).
     *
     * @param playerSession the current player session (may be null for client-only cleanup)
     */
    public static void registerAllHandlers(PlayerSession playerSession) {
        NarrativeStateManager stateManager = NarrativeCraftMod.getInstance().getNarrativeStateManager();

        // Register handlers in priority order (lowest runs first)
        stateManager.registerCleanupHandler(new HudCleanupHandler(playerSession));
        stateManager.registerCleanupHandler(new CameraCleanupHandler(playerSession));
        stateManager.registerCleanupHandler(new InputCleanupHandler(playerSession));
        stateManager.registerCleanupHandler(new AudioCleanupHandler(playerSession));

        NarrativeCraftMod.LOGGER.debug("NarrativeCleanupService: Registered all cleanup handlers");
    }

    /**
     * Registers client-side only cleanup handlers (no session data).
     * Use when session is not available but cleanup is still needed.
     */
    public static void registerClientHandlers() {
        NarrativeStateManager stateManager = NarrativeCraftMod.getInstance().getNarrativeStateManager();

        stateManager.registerCleanupHandler(new HudCleanupHandler());
        stateManager.registerCleanupHandler(new CameraCleanupHandler());
        stateManager.registerCleanupHandler(new InputCleanupHandler());
        stateManager.registerCleanupHandler(new AudioCleanupHandler());

        NarrativeCraftMod.LOGGER.debug("NarrativeCleanupService: Registered client-only cleanup handlers");
    }

    /**
     * Performs immediate cleanup of all narrative state.
     * This is idempotent and safe to call multiple times.
     * Use for emergency cleanup (disconnect, world change, crash recovery).
     */
    public static void forceCleanupNow() {
        if (cleanupInProgress) {
            NarrativeCraftMod.LOGGER.debug("NarrativeCleanupService: Cleanup already in progress, skipping");
            return;
        }

        cleanupInProgress = true;
        try {
            NarrativeCraftMod.LOGGER.info("NarrativeCleanupService: Force cleanup initiated");

            // Force state manager to GAMEPLAY
            NarrativeStateManager stateManager = NarrativeCraftMod.getInstance().getNarrativeStateManager();
            stateManager.forceExitToGameplay();

            // Direct HUD restoration (failsafe)
            try {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft != null && minecraft.options != null) {
                    minecraft.options.hideGui = false;
                }
            } catch (Exception e) {
                NarrativeCraftMod.LOGGER.debug("NarrativeCleanupService: Could not restore HUD directly");
            }

            // Clear all managers
            NarrativeCraftMod.getInstance().clearManagers();

            NarrativeCraftMod.LOGGER.info("NarrativeCleanupService: Force cleanup completed");
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("NarrativeCleanupService: Error during force cleanup: {}", e.getMessage());
        } finally {
            cleanupInProgress = false;
        }
    }

    /**
     * Performs cleanup for a specific player session.
     * Called when a player's narrative session ends.
     *
     * @param playerSession the session to clean up
     */
    public static void cleanupSession(PlayerSession playerSession) {
        if (playerSession == null) {
            return;
        }

        NarrativeCraftMod.LOGGER.debug(
                "NarrativeCleanupService: Cleaning up session for player {}",
                playerSession.getPlayer().getName().getString());

        try {
            // Stop story handler if active
            if (playerSession.getStoryHandler() != null) {
                playerSession.getStoryHandler().stop();
            }

            // Clear dialog renderer
            playerSession.setDialogRenderer(null);

            // Reset camera
            playerSession.setCurrentCamera(null);

            // Clear ink actions
            playerSession.getInkActions().clear();

            // Reset session state
            playerSession.reset();

        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("NarrativeCleanupService: Error cleaning up session: {}", e.getMessage());
        }
    }

    /**
     * Called when the client disconnects from a server.
     * Ensures all narrative state is reset.
     */
    public static void onDisconnect() {
        NarrativeCraftMod.LOGGER.info("NarrativeCleanupService: Client disconnect detected");
        forceCleanupNow();
    }

    /**
     * Called when the world is being unloaded.
     * Ensures cleanup before world change.
     */
    public static void onWorldUnload() {
        NarrativeCraftMod.LOGGER.info("NarrativeCleanupService: World unload detected");
        forceCleanupNow();
    }

    /**
     * Called when returning to title screen.
     * Guarantees clean state for next session.
     */
    public static void onReturnToTitle() {
        NarrativeCraftMod.LOGGER.info("NarrativeCleanupService: Return to title detected");
        forceCleanupNow();
    }

    /**
     * Checks if cleanup is currently in progress.
     * Can be used to prevent re-entrancy.
     *
     * @return true if cleanup is in progress
     */
    public static boolean isCleanupInProgress() {
        return cleanupInProgress;
    }
}
