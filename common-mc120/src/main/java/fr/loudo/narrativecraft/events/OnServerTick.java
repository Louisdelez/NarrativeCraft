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
import fr.loudo.narrativecraft.controllers.interaction.InteractionController;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.AreaTrigger;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.CharacterInteraction;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.EntityInteraction;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.util.NarrativeProfiler;
import java.util.Iterator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * MC 1.20.x version of OnServerTick.
 * Key differences from 1.21.x:
 * - LivingEntity.isLookingAtMe() doesn't exist in 1.20.x
 * - Uses custom isPlayerLookingAt() helper method instead
 */
public class OnServerTick {
    public static void tick(MinecraftServer server) {
        NarrativeProfiler.start(NarrativeProfiler.TICK_SERVER);
        try {
            tickInternal(server);
        } finally {
            NarrativeProfiler.stop(NarrativeProfiler.TICK_SERVER);
        }
    }

    private static void tickInternal(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerSession playerSession =
                    NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(player);

            // T095: Use Iterator to avoid ArrayList allocation for removal
            // Before: List<InkAction> toRemove = new ArrayList<>(); + removeAll()
            // After: Iterator with remove() - zero allocations
            Iterator<InkAction> iterator = playerSession.getServerSideInkActions().iterator();
            while (iterator.hasNext()) {
                InkAction inkAction = iterator.next();
                inkAction.tick();
                if (!inkAction.isRunning()) {
                    iterator.remove();
                }
            }
            StoryHandler storyHandler = playerSession.getStoryHandler();

            KeyframeLocation location = playerSession.getCurrentCamera();
            if (location != null && playerSession.shouldSyncCamera(location)) {
                playerSession
                        .getPlayer()
                        .connection
                        .teleport(
                                location.getX(),
                                location.getY(),
                                location.getZ(),
                                location.getYaw(),
                                location.getPitch());
                playerSession.markCameraSynced(location);
            }

            if (storyHandler == null) continue;

            // AreaTrigger detection
            if (!playerSession.isOnGameplay()) continue;
            if (playerSession.getCurrentCamera() != null) continue;
            Vec3 pPosition = playerSession.getPlayer().position();
            AreaTrigger areaTriggerInside = null;
            for (InteractionController interactionController : playerSession.getInteractionControllers()) {
                for (AreaTrigger areaTrigger : interactionController.getAreaTriggers()) {
                    if (AreaTrigger.isInside(areaTrigger, pPosition)) {
                        areaTriggerInside = areaTrigger;
                    }
                }
            }
            // What happens:
            // Player enter the area trigger, trigger one time
            // Exit, and when enter the area trigger again if it's not unique, trigger again
            // This prevents infinite trigger when the player is inside the area trigger.
            // IMPORTANT: Set lastAreaTriggerEntered BEFORE playStitch to prevent re-entrancy
            // T056: Added debounce check to prevent rapid re-triggering
            if (areaTriggerInside != null && !areaTriggerInside.equals(playerSession.getLastAreaTriggerEntered())) {
                if (areaTriggerInside.isUnique()
                        && playerSession.getAreaTriggersEntered().contains(areaTriggerInside)) continue;
                // T056: Debounce - skip if story is already running (prevents rapid re-trigger)
                if (storyHandler.isRunning()) continue;
                // Set guard BEFORE playStitch to prevent re-entrancy during story execution
                playerSession.addAreaTriggerEntered(areaTriggerInside);
                playerSession.setLastAreaTriggerEntered(areaTriggerInside);
                try {
                    storyHandler.playStitch(areaTriggerInside.getStitch());
                } catch (Exception e) {
                    NarrativeCraftMod.LOGGER.error("Error in area trigger playStitch: {}", e.getMessage());
                }
            } else if (areaTriggerInside == null && playerSession.getLastAreaTriggerEntered() != null) {
                playerSession.setLastAreaTriggerEntered(null);
            }

            // If player is looking to an interaction
            playerSession.setLookingAtEntityId(-1);
            for (InteractionController interactionController : playerSession.getInteractionControllers()) {
                for (CharacterInteraction characterInteraction : interactionController.getCharacterInteractions()) {
                    if (characterInteraction.getStitch().isEmpty()) continue;
                    if (playerSession.getDialogRenderer() != null
                            && playerSession.getLastInteraction() != null
                            && playerSession
                                    .getLastInteraction()
                                    .getStitch()
                                    .equals(characterInteraction.getStitch())) {
                        continue;
                    }
                    if (characterInteraction
                                    .getCharacterStoryData()
                                    .getCharacterRuntime()
                                    .getEntity()
                            instanceof LivingEntity livingEntity) {
                        double targetY = livingEntity.getY() + livingEntity.getBbHeight() / 2;
                        if (pPosition.distanceTo(livingEntity.position()) <= 5.0
                                && isPlayerLookingAt(player, livingEntity, 0.2, targetY)) {
                            playerSession.setLookingAtEntityId(livingEntity.getId());
                        }
                    }
                }
                for (EntityInteraction entityInteraction : interactionController.getEntityInteractions()) {
                    if (entityInteraction.getStitch().isEmpty()) continue;
                    if (playerSession.getDialogRenderer() != null
                            && playerSession.getLastInteraction() != null
                            && playerSession.getLastInteraction().getStitch().equals(entityInteraction.getStitch())) {
                        continue;
                    }
                    Entity armorStand = entityInteraction.getArmorStand();
                    double targetY = armorStand.getY() + armorStand.getBbHeight() / 2;
                    if (pPosition.distanceTo(armorStand.position()) <= 5.0
                            && isPlayerLookingAt(player, armorStand, 0.05, targetY)) {
                        playerSession.setLookingAtEntityId(armorStand.getId());
                    }
                }
            }
        }
    }

    /**
     * MC 1.20.x replacement for LivingEntity.isLookingAtMe().
     * Checks if the player is looking at the target entity within a given dot product threshold.
     *
     * @param player The player doing the looking
     * @param target The entity being looked at
     * @param dotThreshold The minimum dot product (0.0 = perpendicular, 1.0 = exactly aligned)
     * @param targetY The Y coordinate to target on the entity (usually center of bounding box)
     * @return true if the player is looking at the target
     */
    private static boolean isPlayerLookingAt(ServerPlayer player, Entity target, double dotThreshold, double targetY) {
        // Get player's view direction
        Vec3 viewVector = player.getViewVector(1.0F);

        // Get player eye position
        Vec3 eyePosition = player.getEyePosition();

        // Get direction from player to target
        Vec3 targetPosition = new Vec3(target.getX(), targetY, target.getZ());
        Vec3 toTarget = targetPosition.subtract(eyePosition).normalize();

        // Calculate dot product between view direction and direction to target
        double dot = viewVector.dot(toTarget);

        // If dot product is greater than threshold, player is looking at target
        // Higher threshold = more precise aim required
        return dot > (1.0 - dotThreshold);
    }
}
