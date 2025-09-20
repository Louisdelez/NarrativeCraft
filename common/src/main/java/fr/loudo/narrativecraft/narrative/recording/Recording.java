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
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.recording.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recording.actions.GameModeAction;
import fr.loudo.narrativecraft.narrative.recording.actions.RidingAction;
import fr.loudo.narrativecraft.narrative.recording.actions.modsListeners.ModsListenerImpl;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.ProjectileItem;

public class Recording {

    private final AtomicInteger ids = new AtomicInteger();

    private final List<Entity> trackedEntities = new ArrayList<>();
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
        trackedEntities.clear();
        recordingDataList.add(entityRecorderData);
        isRecording = true;
        if (entityRecorderData.getEntity() instanceof ServerPlayer player) {
            GameModeAction gameModeAction = new GameModeAction(
                    0, player.gameMode.getGameModeForPlayer(), player.gameMode.getGameModeForPlayer());
            entityRecorderData.getActionsData().addAction(gameModeAction);
        }
    }

    public void stop() {
        if (!isRecording) return;
        isRecording = false;
        for (Subscene subscene : subscenesPlaying) {
            subscene.stop(true);
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

        List<UUID> trackedUUIDs = trackedEntities.stream().map(Entity::getUUID).toList();

        List<Entity> nearbyEntities = entityRecorderData
                .getEntity()
                .level()
                .getEntities(
                        entityRecorderData.getEntity(),
                        entityRecorderData.getEntity().getBoundingBox().inflate(30));

        for (Entity entity : nearbyEntities) {
            if (!trackedUUIDs.contains(entity.getUUID())
                    && !(entity instanceof ProjectileItem)
                    && !(entity instanceof EyeOfEnder)
                    && !(entity instanceof ThrowableItemProjectile)) {
                trackedEntities.add(entity);
                RecordingData recordingData = new RecordingData(entity, this);
                recordingDataList.add(recordingData);
                if (entity instanceof VehicleEntity
                        || entity instanceof AbstractHorse
                        || entity instanceof ItemEntity) {
                    trackEntity(entity, tick);
                }
            }
        }

        for (RecordingData recordingData : recordingDataList) {
            if (playerSession.getPlaybackManager().entityInPlayback(recordingData.getEntity())) {
                recordingData.setSavingTrack(false);
            }
            recordingData.getActionsData().addLocation();
            recordingData.getActionDifferenceListener().listenDifference();
        }
        if (tick == 0 && entityRecorderData.getEntity().getVehicle() != null) {
            ActionsData actionsData =
                    getActionDataFromEntity(entityRecorderData.getEntity().getVehicle());
            if (actionsData != null) {
                RidingAction action = new RidingAction(0, actionsData.getEntityIdRecording());
                entityRecorderData.getActionsData().addAction(action);
            }
        }
        tick++;
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
}
