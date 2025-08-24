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

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.mixin.accessor.PlayerAccessor;
import fr.loudo.narrativecraft.mixin.accessor.PlayerListAccessor;
import fr.loudo.narrativecraft.narrative.Environnement;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.recording.actions.*;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.util.FakePlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Playback {

    private final int id;
    private final boolean isLooping;
    private final Animation animation;
    private final CharacterRuntime characterRuntime;
    private final Level level;
    private final Environnement environnement;
    private final List<PlaybackData> entityPlaybacks = new ArrayList<>();

    private LivingEntity masterEntity;
    private boolean isPlaying, hasEnded, isUnique;
    private int globalTick;

    public Playback(int id, Animation animation, Level level, Environnement environnement, boolean isLooping) {
        this.id = id;
        this.animation = animation;
        this.level = level;
        this.environnement = environnement;
        this.characterRuntime = new CharacterRuntime(animation.getCharacter(), this);
        this.isPlaying = false;
        this.hasEnded = false;
        this.isLooping = isLooping;
        this.isUnique = false;
    }

    public void start() {

        //        if(environnement == Environnement.PRODUCTION) {
        //            StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        //            if(storyHandler.characterInStory(character)) {
        //                if(needToRespawn(character.getEntity().position(), animation.getFirstLocation().asVec3())) {
        //                    storyHandler.removeCharacter(character);
        //                } else {
        //                    masterEntity = character.getEntity();
        //                }
        //            }
        //        }

        globalTick = 0;
        isPlaying = true;
        hasEnded = false;
        entityPlaybacks.clear();

        ActionsData masterEntityData = animation.getActionsData().getFirst();
        Location firstLoc = masterEntityData.getLocations().getFirst();
        PlaybackData playbackData = new PlaybackData(masterEntityData, this);
        playbackData.setEntity(masterEntity);
        entityPlaybacks.add(playbackData);
        if (masterEntity == null) {
            spawnMasterEntity(firstLoc);
        }

        for (int i = 1; i < animation.getActionsData().size(); i++) {
            ActionsData actionsData = animation.getActionsData().get(i);
            PlaybackData playbackData1 = new PlaybackData(actionsData, this);
            if (actionsData.getSpawnTick() == 0) {
                playbackData1.spawnEntity(actionsData.getLocations().getFirst());
            }
            entityPlaybacks.add(playbackData1);
        }

        //        if (environnement == Environnement.DEVELOPMENT) {
        //            NarrativeCraftMod.getInstance().getCharacterManager().reloadSkin(character);
        //        }
        for (PlaybackData playbackData1 : entityPlaybacks) {
            actionListener(playbackData1);
        }
    }

    public void tick() {
        if (!isPlaying) return;
        for (PlaybackData playbackData : entityPlaybacks) {
            playbackData.tick(globalTick);
        }

        globalTick++;

        boolean allEnded = entityPlaybacks.stream().allMatch(PlaybackData::hasEnded);
        if (allEnded) {
            //            if (environnement == Environnement.DEVELOPMENT) {
            //                PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
            //                if (!(playerSession.getKeyframeControllerBase() instanceof CutsceneController)) {
            //                    finalizePlaybackCycle();
            //                    return;
            //                }
            //            } else {
            //                finalizePlaybackCycle();
            //            }
            finalizePlaybackCycle();
        }
        for (PlaybackData playbackData : entityPlaybacks) {
            actionListener(playbackData);
        }
    }

    public void finalizePlaybackCycle() {
        if (isUnique || environnement == Environnement.RECORDING) {
            stop(true);
            return;
        }
        reset();
        if (isLooping) {
            for (PlaybackData playbackData : entityPlaybacks) {
                if (playbackData.getEntity() == null) continue;

                playbackData.getActionsData().reset(playbackData.getEntity());
                ActionsData actionsData = playbackData.getActionsData();
                List<Location> movementData = actionsData.getLocations();
                if (movementData.isEmpty()) continue;
                if (needToRespawn(
                        movementData.getFirst().asVec3(), movementData.getLast().asVec3())) {
                    if (playbackData.getEntity().getUUID().equals(masterEntity.getUUID())) {
                        playbackData.killEntity();
                        spawnMasterEntity(movementData.getFirst());
                    } else {
                        playbackData.killEntity();
                        playbackData.spawnEntity(movementData.getFirst());
                    }
                }
            }
        } else {
            stop(false);
        }
    }

    private boolean needToRespawn(Vec3 firstPos, Vec3 secondPos) {
        return firstPos.distanceTo(secondPos) >= 0.8;
    }

    private void reset() {
        for (PlaybackData playbackData : entityPlaybacks) {
            playbackData.reset();
        }
        globalTick = 0;
    }

    public void killMasterEntity() {
        PlaybackData playbackData = entityPlaybacks.getFirst();
        masterEntity.remove(Entity.RemovalReason.KILLED);
        if (masterEntity instanceof FakePlayer fakePlayer) {
            ((PlayerListAccessor) level.getServer().getPlayerList())
                    .getPlayersByUUID()
                    .remove(fakePlayer.getUUID());
        }
        playbackData.setEntity(null);
    }

    public void respawnMasterEntity(Location position) {
        spawnMasterEntity(position);
    }

    public void changeLocationByTick(int newTick, boolean seamless) {
        newTick = Math.min(
                newTick, animation.getActionsData().getFirst().getLocations().size() - 1);
        int oldTick = globalTick;
        for (PlaybackData playbackData : entityPlaybacks) {
            ActionsData actionsData = playbackData.getActionsData();
            if (playbackData.getEntity() != null && playbackData.getEntity().equals(masterEntity)) {
                Location location = actionsData.getLocations().get(newTick);
                playbackData.setLocalTick(newTick);
                if (seamless) {
                    moveEntitySilent(masterEntity, location);
                } else {
                    killMasterEntity();
                    spawnMasterEntity(location);
                    if (newTick == 0) {
                        globalTick = 0;
                        actionListener(playbackData);
                    }
                    for (int i = 0; i < newTick; i++) {
                        globalTick = i;
                        actionListener(playbackData);
                    }
                    globalTick = oldTick;
                }
            } else {
                playbackData.changeLocationByTick(newTick, seamless);
            }
        }
        for (PlaybackData playbackData : entityPlaybacks) {
            int tickDiff = newTick - globalTick;
            if (tickDiff > 0) {
                for (int i = globalTick; i < newTick; i++) {
                    globalTick = i;
                    actionListener(playbackData);
                }
            } else {
                for (int i = globalTick; i > newTick; i--) {
                    globalTick = i;
                    actionListenerRewind(playbackData);
                }
            }
            globalTick = oldTick;
        }
        this.globalTick = newTick;
        this.hasEnded = entityPlaybacks.stream().allMatch(PlaybackData::hasEnded);
    }

    public void actionListener(PlaybackData playbackData) {
        if (playbackData.getEntity() == null) return;
        List<Action> actionToBePlayed = playbackData.getActionsData().getActions().stream()
                .filter(action -> globalTick == action.getTick())
                .toList();
        for (Action action : actionToBePlayed) {
            if (action instanceof EmoteAction && !Services.PLATFORM.isModLoaded("emotecraft")) continue;
            action.execute(playbackData);
        }
    }

    public void actionListenerRewind(PlaybackData playbackData) {
        List<Action> actionToBePlayed = playbackData.getActionsData().getActions().stream()
                .filter(action -> globalTick == action.getTick())
                .toList();
        actionToBePlayed = actionToBePlayed.reversed();
        for (Action action : actionToBePlayed) {
            if (action instanceof EmoteAction && !Services.PLATFORM.isModLoaded("emotecraft")) continue;
            if (!(action instanceof DeathAction) && playbackData.getEntity() == null) continue;
            if (action instanceof PoseAction poseAction) {
                poseAction.rewind(playbackData);
                if (poseAction.getPreviousPose() == Pose.SLEEPING) {
                    SleepAction previousSleepAction = (SleepAction) playbackData.getActionsData().getActions().stream()
                            .filter(action1 -> globalTick <= action.getTick() && action1 instanceof SleepAction)
                            .toList()
                            .getLast();
                    if (previousSleepAction != null) {
                        previousSleepAction.execute(playbackData);
                    }
                }
            } else {
                action.rewind(playbackData);
            }
        }
    }

    private void spawnMasterEntity(Location loc) {
        if (masterEntity != null && masterEntity.isAlive()) {
            moveEntitySilent(masterEntity, loc);
            return;
        }

        GameProfile gameProfile = new GameProfile(
                UUID.randomUUID(), characterRuntime.getCharacterStory().getName());

        if (BuiltInRegistries.ENTITY_TYPE.getId(
                        characterRuntime.getCharacterStory().getEntityType())
                == BuiltInRegistries.ENTITY_TYPE.getId(EntityType.PLAYER)) {
            masterEntity = new FakePlayer((ServerLevel) level, gameProfile);
            masterEntity.getEntityData().set(PlayerAccessor.getDATA_PLAYER_MODE_CUSTOMISATION(), (byte) 0b01111111);
        } else {
            masterEntity = (LivingEntity)
                    characterRuntime.getCharacterStory().getEntityType().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (masterEntity instanceof Mob mob) mob.setNoAi(true);
        }

        moveEntitySilent(masterEntity, loc);

        if (masterEntity instanceof FakePlayer fakePlayer) {
            ((PlayerListAccessor) level.getServer().getPlayerList())
                    .getPlayersByUUID()
                    .put(fakePlayer.getUUID(), fakePlayer);
            level.getServer()
                    .getPlayerList()
                    .broadcastAll(new ClientboundPlayerInfoUpdatePacket(
                            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.addNewPlayer(fakePlayer);
            }
        } else {
            level.addFreshEntity(masterEntity);
        }

        entityPlaybacks.getFirst().setEntity(masterEntity);
        //        if(environnement == Environnement.PRODUCTION) {
        //            StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        //            if(storyHandler != null && storyHandler.isRunning()) {
        //                storyHandler.addCharacter(character);
        //            }
        //        }
    }

    public int getMaxTick() {
        return animation.getActionsData().stream()
                .mapToInt(data -> data.getLocations().size())
                .max()
                .orElse(0);
    }

    public void stop(boolean killEntity) {
        isPlaying = false;
        hasEnded = true;
        if (killEntity) {
            for (PlaybackData playbackData : entityPlaybacks) {
                if (playbackData.getEntity() == null) continue;
                playbackData.getActionsData().reset(playbackData.getEntity());
                playbackData.killEntity();
            }
            //        if(environnement == Environnement.PRODUCTION) {
            //            StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
            //            if(storyHandler != null && storyHandler.isRunning()) {
            //                storyHandler.removeCharacter(character);
            //            }
            //        }
        }
    }

    private void moveEntitySilent(Entity entity, Location location) {
        if (entity == null) return;
        entity.setXRot(location.pitch());
        entity.setYRot(location.yaw());
        entity.setYHeadRot(location.yaw());
        entity.setOnGround(location.onGround());
        entity.teleportTo(location.x(), location.y(), location.z());
    }

    public boolean entityInPlayback(Entity entity) {
        for (PlaybackData playbackData : entityPlaybacks) {
            if (playbackData.getEntity() != null) {
                if (playbackData.getEntity().getUUID().equals(entity.getUUID())) return true;
            }
        }
        return false;
    }

    public Entity getEntityByRecordId(int recordingId) {
        for (PlaybackData playbackData : entityPlaybacks) {
            if (playbackData.getActionsData().getEntityIdRecording() == recordingId) {
                return playbackData.getEntity();
            }
        }
        return null;
    }

    public PlaybackData getPlaybackDataByRecordId(int recordingId) {
        for (PlaybackData playbackData : entityPlaybacks) {
            if (playbackData.getActionsData().getEntityIdRecording() == recordingId) {
                return playbackData;
            }
        }
        return null;
    }

    public List<PlaybackData> getEntityPlaybacks() {
        return entityPlaybacks;
    }

    public ActionsData getMasterEntityData() {
        return animation.getActionsData().getFirst();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean hasEnded() {
        return hasEnded;
    }

    public int getTick() {
        return globalTick;
    }

    public void setTick(int tick) {
        this.globalTick = tick;
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        if (playing) this.hasEnded = false;
    }

    public Animation getAnimation() {
        return animation;
    }

    public LivingEntity getMasterEntity() {
        return masterEntity;
    }

    public CharacterStory getCharacter() {
        return characterRuntime.getCharacterStory();
    }

    public int getId() {
        return id;
    }

    public Environnement getEnvironnement() {
        return environnement;
    }

    public Level getLevel() {
        return level;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }
}
