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

import fr.loudo.narrativecraft.mixin.accessor.PlayerListAccessor;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.recording.actions.*;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.util.FakePlayer;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Playback {

    private final int id;
    private final boolean isLooping;
    private final Animation animation;
    private final CharacterRuntime characterRuntime;
    private final Level level;
    private final Environment environment;
    private final List<PlaybackData> entityPlaybacks = new ArrayList<>();
    private Runnable onStop;

    private LivingEntity masterEntity;
    private boolean isPlaying, hasEnded, isUnique;
    private int globalTick;

    public Playback(int id, Animation animation, Level level, Environment environment, boolean isLooping) {
        this.id = id;
        this.animation = animation;
        this.level = level;
        this.environment = environment;
        this.characterRuntime = new CharacterRuntime(
                animation.getCharacter(), animation.getSkinName(), masterEntity, animation.getScene());
        this.isPlaying = false;
        this.hasEnded = false;
        this.isLooping = isLooping;
        this.isUnique = false;
    }

    public void startFromStory(StoryHandler storyHandler) {
        if (storyHandler == null) {
            start();
            return;
        }
        List<CharacterRuntime> characterRuntimes = storyHandler.getCharacterRuntimeFromCharacter(this.getCharacter());
        for (CharacterRuntime characterRuntime1 : characterRuntimes) {
            if (characterRuntime1.getEntity() != null
                    && !characterRuntime1.getEntity().isRemoved()) {
                if (needToRespawn(
                        characterRuntime1.getEntity().position(),
                        animation.getFirstLocation().asVec3())) {
                    storyHandler.killCharacter(animation.getCharacter());
                } else {
                    masterEntity = characterRuntime1.getEntity();
                    characterRuntime.setEntity(masterEntity);
                }
            }
        }
        start();
    }

    public void start() {

        characterRuntime.getCharacterSkinController().cacheSkins();
        if (animation.getCharacter() == null) {
            Minecraft.getInstance()
                    .player
                    .displayClientMessage(
                            Translation.message("animation.no_character_linked", animation.getName())
                                    .withStyle(ChatFormatting.RED),
                            false);
            return;
        }

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

        for (PlaybackData playbackData1 : entityPlaybacks) {
            playbackData1.reset();
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
            finalizePlaybackCycle();
        }
        for (PlaybackData playbackData : entityPlaybacks) {
            actionListener(playbackData);
        }
    }

    public void finalizePlaybackCycle() {
        if (isUnique || environment == Environment.RECORDING) {
            stop(true);
            return;
        }
        if (isLooping) {
            reset();
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

    public boolean needToRespawn(Vec3 firstPos, Vec3 secondPos) {
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
        globalTick = newTick;
        hasEnded = false;
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
        if (masterEntity != null && !masterEntity.isRemoved()) {
            moveEntitySilent(masterEntity, loc);
            return;
        }

        masterEntity = Util.createEntityFromCharacter(characterRuntime.getCharacterStory(), level);
        characterRuntime.setEntity(masterEntity);
        moveEntitySilent(masterEntity, loc);

        entityPlaybacks.getFirst().setEntity(masterEntity);
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
                if (playbackData.getEntity() instanceof ServerPlayer serverPlayer) {
                    serverPlayer.doCloseContainer();
                }
                playbackData.getActionsData().reset(playbackData.getEntity());
                playbackData.killEntity();
            }
        }
        if (onStop != null) onStop.run();
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

    public CharacterRuntime getCharacterRuntime() {
        return characterRuntime;
    }

    public CharacterStory getCharacter() {
        return characterRuntime.getCharacterStory();
    }

    public int getId() {
        return id;
    }

    public Environment getEnvironment() {
        return environment;
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

    public Runnable getOnStop() {
        return onStop;
    }

    public void setOnStop(Runnable onStop) {
        this.onStop = onStop;
    }
}
