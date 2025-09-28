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

package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.Location;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ActionsData {

    private transient Entity entity;
    private final int entityId;
    private int entityIdRecording;
    private int spawnTick;
    private String nbtData;
    private final List<Location> locations;
    private final List<Action> actions;

    public ActionsData(Entity entity, int spawnTick) {
        this.locations = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.entity = entity;
        if (!(entity instanceof ServerPlayer)) {
            nbtData = String.valueOf(serializeNBT());
        }
        entityId = BuiltInRegistries.ENTITY_TYPE.getId(entity.getType());
        this.spawnTick = spawnTick;
        entityIdRecording = -1;
    }

    private CompoundTag serializeNBT() {
        CompoundTag compoundTag = entity.saveWithoutId(new CompoundTag());
        compoundTag.remove("UUID");
        compoundTag.remove("Pos");
        compoundTag.remove("Motion");
        return compoundTag;
    }

    public void addLocation() {
        Location currentLoc = new Location(
                entity.getX(), entity.getY(), entity.getZ(), entity.getXRot(), entity.getYRot(), entity.onGround());
        locations.add(currentLoc);
    }

    public void reset(Entity entity) {
        PlaybackData playbackData = new PlaybackData(this, null);
        playbackData.setEntity(entity);
        Map<BlockPos, Action> latestActions = new HashMap<>();

        for (Action action : actions) {
            BlockPos pos = getPosFromAction(action);
            if (pos == null) continue;
            latestActions.putIfAbsent(pos, action);
        }

        for (Map.Entry<BlockPos, Action> entry : latestActions.entrySet()) {
            Action action = entry.getValue();

            if (action instanceof PlaceBlockAction place) {
                place.rewind(playbackData);
            } else if (action instanceof BreakBlockAction breakBlockAction) {
                breakBlockAction.rewind(playbackData);
            }
        }
    }

    private BlockPos getPosFromAction(Action action) {
        if (action instanceof PlaceBlockAction p) {
            return p.getBlockPos();
        } else if (action instanceof BreakBlockAction b) {
            return b.getBlockPos();
        }
        return null;
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public List<Location> getLocations() {
        return locations;
    }

    public List<Action> getActions() {
        return actions;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public int getSpawnTick() {
        return spawnTick;
    }

    public void setSpawnTick(int spawnTick) {
        this.spawnTick = spawnTick;
    }

    public int getEntityIdRecording() {
        return entityIdRecording;
    }

    public void setEntityIdRecording(int entityIdRecording) {
        this.entityIdRecording = entityIdRecording;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getNbtData() {
        return nbtData;
    }
}
