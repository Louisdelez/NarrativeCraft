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

package fr.loudo.narrativecraft.narrative.recording;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.recording.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recording.actions.GameModeAction;
import fr.loudo.narrativecraft.narrative.recording.actions.RidingAction;
import fr.loudo.narrativecraft.narrative.recording.actions.modsListeners.ModsListenerImpl;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.NarrativeCraftConstants;
import fr.loudo.narrativecraft.util.NarrativeProfiler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
// 1.20.x package path: animal.horse instead of animal.equine
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.EyeOfEnder;
// 1.20.x package path: projectile.ThrowableItemProjectile (not in subpackage)
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;

/**
 * MC 1.20.x version of Recording.
 * Key differences from 1.21.x:
 * - AbstractHorse: net.minecraft.world.entity.animal.horse instead of animal.equine
 * - ThrowableItemProjectile: net.minecraft.world.entity.projectile (not in throwableitemprojectile subpackage)
 * - No VehicleEntity class - use Boat/Minecart directly
 * - No ProjectileItem interface - removed the check
 */
public class Recording {

    private final AtomicInteger ids = new AtomicInteger();

    // T095/T096: Changed from List to HashSet for O(1) contains() lookup
    // Before: List<Entity> + stream().map().toList() each tick
    // After: Set<UUID> - zero allocation lookup via contains()
    private final Set<UUID> trackedEntityUUIDs = new HashSet<>(NarrativeCraftConstants.TRACKED_ENTITIES_INITIAL_CAPACITY);
    private final List<RecordingData> recordingDataList = new ArrayList<>();
    private final PlayerSession playerSession;
    private List<Subscene> subscenesPlaying = new ArrayList<>();
    private RecordingData entityRecorderData;
    private boolean isRecording;
    private int tick;

    public Recording(LivingEntity entity, PlayerSession playerSession) {
        tick = 0;
        entityRecorderData = new RecordingData(entity, this);
        entityRecorderData.setSavingTrack(true);
        entityRecorderData.getActionsData().setEntityIdRecording(ids.incrementAndGet());
        isRecording = false;
        this.playerSession = playerSession;
    }

    public Recording(LivingEntity entity, PlayerSession playerSession, List<Subscene> subscenes) {
        this(entity, playerSession);
        subscenesPlaying = subscenes;
    }

    public boolean isSameEntity(Entity entity) {
        return entityRecorderData.getEntity().getUUID().equals(entity.getUUID());
    }

    public void start() {
        if (isRecording) return;
        tick = 0;
        ids.set(0);
        entityRecorderData = new RecordingData(entityRecorderData.getEntity(), this);
        entityRecorderData.setSavingTrack(true);
        entityRecorderData.getActionsData().setEntityIdRecording(ids.incrementAndGet());
        recordingDataList.clear();
        trackedEntityUUIDs.clear();
        recordingDataList.add(entityRecorderData);
        isRecording = true;
        if (entityRecorderData.getEntity() instanceof ServerPlayer player) {
            GameModeAction gameModeAction = new GameModeAction(
                    0, player.gameMode.getGameModeForPlayer(), player.gameMode.getGameModeForPlayer());
            entityRecorderData.getActionsData().addAction(gameModeAction);
        }
        for (Subscene subscene : subscenesPlaying) {
            subscene.start(playerSession.getPlayer().getLevel(), Environment.RECORDING, false);
            playerSession.getPlaybackManager().getPlaybacks().addAll(subscene.getPlaybacks());
        }
    }

    public void stop() {
        if (!isRecording) return;
        isRecording = false;
        for (Subscene subscene : subscenesPlaying) {
            subscene.stop(true);
            playerSession.getPlaybackManager().getPlaybacks().removeAll(subscene.getPlaybacks());
        }
        subscenesPlaying.clear();
        for (RecordingData recordingData : recordingDataList) {
            if (!(recordingData.getEntity() instanceof LivingEntity)) continue;
            recordingData.getActionsData().reset(recordingData.getEntity());
            for (ModsListenerImpl modsListener :
                    recordingData.getActionDifferenceListener().getModsListenerList()) {
                modsListener.stop();
            }
        }
    }

    public void save(Animation animation) throws IOException {
        if (isRecording) return;
        List<ActionsData> actionsDataList = recordingDataList.stream()
                .filter(RecordingData::isSavingTrack)
                .map(RecordingData::getActionsData)
                .toList();
        animation.setActionsData(actionsDataList);
        NarrativeCraftFile.updateAnimationFile(animation);
        animation.getScene().addAnimation(animation);
        recordingDataList.clear();
    }

    public void tick() {
        NarrativeProfiler.start(NarrativeProfiler.RECORDING);

        // T095/T096: Removed stream().map().toList() allocation
        // Before: List<UUID> trackedUUIDs = trackedEntities.stream().map(Entity::getUUID).toList();
        // After: Direct HashSet lookup - O(1) instead of O(n) and zero allocations

        List<Entity> nearbyEntities = entityRecorderData
                .getEntity()
                .getLevel()
                .getEntities(
                        entityRecorderData.getEntity(),
                        entityRecorderData.getEntity().getBoundingBox().inflate(NarrativeCraftConstants.ENTITY_TRACKING_RADIUS));

        for (Entity entity : nearbyEntities) {
            UUID entityUUID = entity.getUUID();
            // T096: O(1) HashSet lookup instead of O(n) List.contains()
            // 1.20.x: No ProjectileItem interface, removed that check
            if (!trackedEntityUUIDs.contains(entityUUID)
                    && !(entity instanceof EyeOfEnder)
                    && !(entity instanceof ThrowableItemProjectile)) {
                trackedEntityUUIDs.add(entityUUID);
                RecordingData recordingData = new RecordingData(entity, this);
                recordingDataList.add(recordingData);
                // 1.20.x: No VehicleEntity, check Boat/Minecart directly
                if (entity instanceof Boat
                        || entity instanceof Minecart
                        || entity instanceof AbstractHorse
                        || entity instanceof ItemEntity) {
                    trackEntity(entity, tick);
                }
            }
        }

        // Handle first-tick vehicle detection BEFORE recording locations
        // (fixes race condition where vehicle state was recorded after location)
        if (tick == 0 && entityRecorderData.getEntity().getVehicle() != null) {
            Entity vehicle = entityRecorderData.getEntity().getVehicle();
            // Ensure vehicle is tracked before recording
            ActionsData actionsData = getActionDataFromEntity(vehicle);
            if (actionsData != null) {
                RidingAction action = new RidingAction(0, actionsData.getEntityIdRecording());
                entityRecorderData.getActionsData().addAction(action);
            }
        }

        for (RecordingData recordingData : recordingDataList) {
            if (playerSession.getPlaybackManager().entityInPlayback(recordingData.getEntity())) {
                recordingData.setSavingTrack(false);
            }
            recordingData.getActionsData().addLocation();
            recordingData.getActionDifferenceListener().listenDifference();
        }
        tick++;
        NarrativeProfiler.stop(NarrativeProfiler.RECORDING);
    }

    public ActionsData getActionDataFromEntity(Entity entity) {
        for (RecordingData recordingData : recordingDataList) {
            if (recordingData.isSameEntity(entity)) {
                recordingData.setSavingTrack(true);
                return recordingData.getActionsData();
            }
        }
        return null;
    }

    public RecordingData getRecordingDataFromEntity(Entity entity) {
        for (RecordingData recordingData : recordingDataList) {
            if (recordingData.isSameEntity(entity)) {
                return recordingData;
            }
        }
        return null;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public Entity getEntity() {
        return entityRecorderData.getEntity();
    }

    public int getTick() {
        return tick;
    }

    public AtomicInteger getIds() {
        return ids;
    }

    public boolean trackEntity(Entity entity) {
        RecordingData recordingData = getRecordingDataFromEntity(entity);
        if (recordingData == null || recordingData.isSavingTrack()) return false;
        recordingData.setSavingTrack(true);
        recordingData.getActionsData().setSpawnTick(0);
        recordingData.getActionsData().setEntityIdRecording(ids.incrementAndGet());
        return true;
    }

    public void trackEntity(Entity entity, int tickSpawn) {
        if (!trackEntity(entity)) return;
        RecordingData recordingData = getRecordingDataFromEntity(entity);
        recordingData.getActionsData().setSpawnTick(tickSpawn);
    }

    public List<Subscene> getSubscenesPlaying() {
        return subscenesPlaying;
    }

    public void setSubscenesPlaying(List<Subscene> subscenesPlaying) {
        this.subscenesPlaying = subscenesPlaying;
    }
}
