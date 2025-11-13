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

package fr.loudo.narrativecraft.narrative.playback;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.recording.actions.Action;
import fr.loudo.narrativecraft.narrative.recording.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recording.actions.BreakBlockAction;
import fr.loudo.narrativecraft.util.FakePlayer;
import fr.loudo.narrativecraft.util.Util;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

public class PlaybackData {

    private final ActionsData actionsData;
    private final Playback playback;
    private Entity entity;
    private int localTick;

    public PlaybackData(ActionsData actionsData, Playback playback) {
        this.actionsData = actionsData;
        this.localTick = 0;
        this.playback = playback;
    }

    public void tick(int globalTick) {
        if (globalTick >= actionsData.getSpawnTick()) {
            if (entity == null) {
                spawnEntity(actionsData.getLocations().get(0));
            }
        }

        if (entity == null) return;

        List<Location> movements = actionsData.getLocations();
        if (localTick >= movements.size()) return;

        Location current = movements.get(localTick);
        Location next = localTick + 1 < movements.size() ? movements.get(localTick + 1) : current;

        moveEntity(current, next, false);

        localTick++;
    }

    public Level getLevel() {
        return entity.level();
    }

    public void changeLocationByTick(int newTick, boolean seamless) {
        if (newTick >= actionsData.getSpawnTick()) {
            if (entity == null) {
                spawnEntity(actionsData.getLocations().get(0));
            }
        } else {
            killEntity();
            reset();
            return;
        }
        localTick = newTick - actionsData.getSpawnTick();
        Location location = actionsData.getLocations().get(localTick);
        if (seamless) {
            moveEntity(location, location, true);
        } else {
            killEntity();
            spawnEntity(location);
        }
    }

    public void killEntity() {
        if (entity == null) return;
        NarrativeCraftMod.server.execute(() -> {
            entity.remove(Entity.RemovalReason.KILLED);
            if (entity instanceof FakePlayer fakePlayer) {
                Util.removeFakePlayerUUID(fakePlayer);
            }
            entity = null;
        });
    }

    public void spawnEntity(Location location) {
        if (actionsData.getEntityId() == BuiltInRegistries.ENTITY_TYPE.getId(EntityType.PLAYER)) return;
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.byId(actionsData.getEntityId());
        entity = Util.createEntityFromKey(entityType, (ServerLevel) playback.getLevel());
        if (entity == null) return;
        try {
            entity.load(Util.nbtFromString(actionsData.getNbtData()));
        } catch (CommandSyntaxException e) {
            NarrativeCraftMod.LOGGER.error("Unexpected error when trying to load nbt entity data! ", e);
            return;
        }
        if (entity instanceof Mob mob) mob.setNoAi(true);
        moveEntity(location, location, true);
        if (entity instanceof ItemEntity itemEntity) { // Drop Item
            List<Action> actions = playback.getMasterEntityData().getActions().stream()
                    .filter(action -> action instanceof BreakBlockAction && action.getTick() == playback.getTick() - 1)
                    .toList();
            boolean randomizeMotion = !actions.isEmpty();
            if (playback.getMasterEntity() instanceof FakePlayer fakePlayer) {
                entity = fakePlayer.drop(itemEntity.getItem(), randomizeMotion, false);
            }
        }
        playback.getLevel().addFreshEntity(entity);
    }

    private void moveEntity(Location current, Location next, boolean silent) {
        if (entity == null) return;
        entity.moveTo(current.x(), current.y(), current.z(), current.yaw(), current.pitch());
        entity.setYHeadRot(current.yaw());
        entity.setOnGround(current.onGround());
        if (!silent) {
            entity.move(MoverType.SELF, Location.deltaLocation(current, next).asVec3());
        }
    }

    public void reset() {
        this.localTick = 0;
        actionsData.reset(entity);
    }

    public boolean hasEnded() {
        return localTick >= actionsData.getLocations().size();
    }

    public ActionsData getActionsData() {
        return actionsData;
    }

    public int getLocalTick() {
        return localTick;
    }

    public void setLocalTick(int localTick) {
        this.localTick = localTick;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Playback getPlayback() {
        return playback;
    }
}
