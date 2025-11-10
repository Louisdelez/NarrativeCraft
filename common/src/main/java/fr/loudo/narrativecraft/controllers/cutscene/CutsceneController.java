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

package fr.loudo.narrativecraft.controllers.cutscene;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionRegistry;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.controllers.keyframe.AbstractKeyframeGroupsBase;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.managers.PlaybackManager;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframeGroup;
import fr.loudo.narrativecraft.narrative.keyframes.keyframeTrigger.KeyframeTrigger;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.controller.cutscene.CutsceneControllerScreen;
import fr.loudo.narrativecraft.screens.controller.cutscene.CutsceneKeyframeOptionScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CutsceneController extends AbstractKeyframeGroupsBase<CutsceneKeyframe, CutsceneKeyframeGroup> {

    private final List<Playback> playbacks = new ArrayList<>();
    private final Cutscene cutscene;
    private final CutscenePlayback cutscenePlayback;
    private Runnable onEnd;

    private boolean isPlaying;
    private int currentTick, skipTickCount;
    private int totalTick;
    private CutsceneKeyframeGroup selectedGroup;

    public CutsceneController(Environment environment, Player player, Cutscene cutscene) {
        super(environment, player);
        this.cutscene = cutscene;
        skipTickCount = 20;
        isPlaying = false;
        for (CutsceneKeyframeGroup keyframeGroup : cutscene.getKeyframeGroups()) {
            CutsceneKeyframeGroup keyframeGroup1 = new CutsceneKeyframeGroup(keyframeGroup.getId());
            keyframeGroup1.getKeyframes().addAll(keyframeGroup.getKeyframes());
            keyframeGroups.add(keyframeGroup1);
        }
        keyframeTriggers.addAll(cutscene.getKeyframeTriggers());
        cutscenePlayback = new CutscenePlayback(this, () -> {});
    }

    public void tick() {
        if (!playbacks.isEmpty()) {
            hudMessage = Translation.message(
                            "controller.cutscene.hud_tick", cutscene.getScene().getName(), currentTick, totalTick)
                    .getString();
        } else {
            hudMessage = Translation.message(
                            "controller.cutscene.hud_no_calculated_tick",
                            cutscene.getScene().getName(),
                            currentTick)
                    .getString();
        }
        if (!isPlaying) return;
        List<KeyframeTrigger> keyframeTriggersToExecute = keyframeTriggers.stream()
                .filter(trigger -> trigger.getTick() == currentTick)
                .toList();
        for (KeyframeTrigger keyframeTrigger : keyframeTriggersToExecute) {
            for (String command : keyframeTrigger.getCommandsToList()) {
                InkAction inkAction = InkActionRegistry.findByCommand(command);
                if (inkAction == null) continue;
                InkActionResult result = inkAction.validateAndExecute(command, playerSession);
                if (!result.isOk()) continue;
                playerSession.addInkAction(inkAction);
            }
        }
        currentTick++;
        if (currentTick >= totalTick && environment == Environment.DEVELOPMENT) {
            pause();
            if (Minecraft.getInstance().screen instanceof CutsceneControllerScreen screen) {
                screen.getControllerButton().setMessage(screen.getPlayText());
            }
        } else if (currentTick >= totalTick && environment == Environment.PRODUCTION) {
            if (onEnd != null) {
                onEnd.run();
            }
            isPlaying = false;
        }
    }

    @Override
    public void startSession() {
        stopCurrentSession();
        if (playerSession.getController() instanceof CutsceneController cutsceneController) {
            cutsceneController.getCutscenePlayback().stop();
        }
        playerSession.setController(this);
        playbacks.clear();
        StoryHandler storyHandler = playerSession.getStoryHandler();
        for (Subscene subscene : cutscene.getSubscenes()) {
            if (storyHandler != null) {
                subscene.start(playerSession.getPlayer().level(), environment, false, storyHandler);
            } else {
                subscene.start(playerSession.getPlayer().level(), environment, false);
            }
            playbacks.addAll(subscene.getPlaybacks());
        }
        for (Animation animation : cutscene.getAnimations()) {
            Playback playback = new Playback(
                    PlaybackManager.ID_INCREMENTER.incrementAndGet(),
                    animation,
                    playerSession.getPlayer().level(),
                    environment,
                    false);
            if (storyHandler != null) {
                playback.startFromStory(storyHandler);
            } else {
                playback.start();
            }
            playback.setPlaying(false);
            playbacks.add(playback);
        }
        totalTick = calculateTotalTick();
        for (Playback playback : playbacks) {
            playerSession.getCharacterRuntimes().add(playback.getCharacterRuntime());
        }
        playerSession.clearKilledCharacters();
        playerSession.getPlaybackManager().getPlaybacks().addAll(playbacks);
        if (environment != Environment.DEVELOPMENT) return;
        if (!keyframeGroups.isEmpty()) {
            selectedGroup = keyframeGroups.getFirst();
            KeyframeLocation keyframeLocation =
                    selectedGroup.getKeyframes().getFirst().getKeyframeLocation();
            playerSession
                    .getPlayer()
                    .teleportTo(keyframeLocation.getX(), keyframeLocation.getY(), keyframeLocation.getZ());
        } else if (!playbacks.isEmpty()) {
            Location location = playbacks.getFirst().getAnimation().getFirstLocation();
            playerSession.getPlayer().teleportTo(location.x(), location.y(), location.z());
        }
        if (!keyframeGroups.isEmpty()) {
            CutsceneKeyframeGroup keyframeGroup = keyframeGroups.getLast();
            keyframeGroupsCounter.set(keyframeGroup.getId());
            for (CutsceneKeyframeGroup keyframeGroup1 : keyframeGroups) {
                keyframeGroup1.showKeyframes(playerSession.getPlayer());
                keyframeGroup1.showGroupText(playerSession.getPlayer());
            }
            updateSelectedGroupGlow();
            CutsceneKeyframe cutsceneKeyframe = getLatestKeyframeFromId();
            if (cutsceneKeyframe == null) return;
            keyframesCounter.set(cutsceneKeyframe.getId());
        }
        for (KeyframeTrigger keyframeTrigger : keyframeTriggers) {
            keyframeTrigger.showKeyframe(playerSession.getPlayer());
        }
        pause();
    }

    @Override
    public void stopSession(boolean save) {
        for (Playback playback : playbacks) {
            if (playback.getCharacterRuntime().getEntity() == null
                    || playback.getCharacterRuntime().getEntity().isRemoved()) {
                playerSession.getCharacterRuntimes().remove(playback.getCharacterRuntime());
            }
            playback.stop(environment == Environment.DEVELOPMENT);
            if (environment == Environment.DEVELOPMENT) { // Characters not killed on PRODUCTION when cutscene end.
                playerSession.getCharacterRuntimes().remove(playback.getCharacterRuntime());
            }
        }
        playerSession.setController(null);
        Minecraft.getInstance().options.hideGui = false;
        if (environment == Environment.DEVELOPMENT) {
            playerSession.setCurrentCamera(null);
        } else if (environment == Environment.PRODUCTION) {
            playerSession.setCurrentCamera(
                    keyframeGroups.getLast().getKeyframes().getLast().getKeyframeLocation());
        }
        playerSession.getPlaybackManager().getPlaybacks().removeAll(playbacks);
        if (environment != Environment.DEVELOPMENT) return;
        playerSession.getCharacterRuntimes().clear();
        for (CutsceneKeyframeGroup keyframeGroup : keyframeGroups) {
            keyframeGroup.hideKeyframes(playerSession.getPlayer());
        }
        List<CutsceneKeyframeGroup> oldData = cutscene.getKeyframeGroups();
        List<KeyframeTrigger> oldKeyframeTriggers = cutscene.getKeyframeTriggers();
        if (save) {
            cutscene.getKeyframeGroups().clear();
            cutscene.getKeyframeTriggers().clear();
            try {
                cutscene.getKeyframeGroups().addAll(keyframeGroups);
                cutscene.getKeyframeTriggers().addAll(keyframeTriggers);
                NarrativeCraftFile.updateCutsceneFile(cutscene.getScene());
                for (Playback playback : playbacks) {
                    playback.getAnimation()
                            .setSkinName(playback.getCharacterRuntime().getSkinName());
                }
                playerSession.getPlayer().sendSystemMessage(Translation.message("controller.saved"));
            } catch (IOException e) {
                cutscene.getKeyframeGroups().removeAll(keyframeGroups);
                cutscene.getKeyframeGroups().addAll(oldData);
                cutscene.getKeyframeTriggers().removeAll(keyframeTriggers);
                cutscene.getKeyframeTriggers().addAll(oldKeyframeTriggers);
                for (Playback playback : playbacks) {
                    playback.getAnimation()
                            .setSkinName(playback.getCharacterRuntime().getOldSkinName());
                }
                playerSession.getPlayer().sendSystemMessage(Translation.message("crash.global-message"));
                NarrativeCraftMod.LOGGER.error("Impossible to save the cutscene: ", e);
            }
        } else {
            for (Playback playback : playbacks) {
                playback.getAnimation()
                        .setSkinName(playback.getCharacterRuntime().getOldSkinName());
            }
        }
        for (KeyframeTrigger keyframeTrigger : keyframeTriggers) {
            keyframeTrigger.hideKeyframe(playerSession.getPlayer());
        }
    }

    public void skip() {
        CutsceneKeyframe keyframe = getLastKeyframeLastGroup();
        if (keyframe == null) return;
        currentTick = totalTick;
        for (Playback playback : playbacks) {
            playback.changeLocationByTick(currentTick, false);
            playerSession.getCharacterRuntimes().remove(playback.getCharacterRuntime());
            playerSession.getCharacterRuntimes().add(playback.getCharacterRuntime());
        }
        playerSession.clearKilledCharacters();
    }

    @Override
    public Screen getControllerScreen() {
        if (cutscenePlayback.isPlaying()) return null;
        return new CutsceneControllerScreen(this);
    }

    @Override
    public void setCamera(Keyframe keyframe) {
        super.setCamera(keyframe);
        CutsceneKeyframe cutsceneKeyframe = (CutsceneKeyframe) keyframe;
        pause();
        Minecraft.getInstance().execute(cutscenePlayback::stop);
        if (environment == Environment.DEVELOPMENT) {
            for (InkAction inkAction : playerSession.getInkActions()) {
                inkAction.stop();
            }
            playerSession.getInkActions().clear();
        }
        if (keyframe == null) {
            updateSelectedGroupGlow();
            showTextGroups();
        } else {
            changeTimePosition(cutsceneKeyframe.getTick(), false);
        }
    }

    private void showTextGroups() {
        for (CutsceneKeyframeGroup keyframeGroup : keyframeGroups) {
            keyframeGroup.showGroupText(playerSession.getPlayer());
        }
    }

    @Override
    public Screen keyframeOptionScreen(Keyframe keyframe, boolean hide) {
        return new CutsceneKeyframeOptionScreen((CutsceneKeyframe) keyframe, playerSession, hide);
    }

    @Override
    public void removeKeyframe(CutsceneKeyframe keyframe) {
        CutsceneKeyframeGroup keyframeGroup = getKeyframeGroupOfKeyframe(keyframe);
        List<CutsceneKeyframe> keyframes = keyframeGroup.getKeyframes();
        super.removeKeyframe(keyframe);
        if (!keyframes.isEmpty()) {
            // If the user delete the first keyframe from the group, then set the first keyframe group to the next
            // keyframe
            keyframeGroup.showGroupText(playerSession.getPlayer());
        } else {
            // Re-assign automatically the group id if the user delete a keyframe group without any child
            keyframeGroupsCounter.decrementAndGet();
            keyframeGroups.remove(keyframeGroup);
            for (int i = 0; i < keyframeGroups.size(); i++) {
                CutsceneKeyframeGroup keyframeGroup1 = keyframeGroups.get(i);
                keyframeGroup1.showGroupText(playerSession.getPlayer());
                keyframeGroup1.setId(i + 1);
                keyframeGroup1.showGroupText(playerSession.getPlayer());
            }
            if (!keyframeGroups.isEmpty()) {
                selectedGroup = keyframeGroups.getLast();
                updateSelectedGroupGlow();
            } else {
                selectedGroup = null;
            }
        }
    }

    public void resume() {
        if (atMaxTick()) return;
        isPlaying = true;
        for (Playback playback : playbacks) {
            playback.setPlaying(true);
        }
    }

    public void pause() {
        isPlaying = false;
        for (Playback playback : playbacks) {
            playback.setPlaying(false);
        }
    }

    public CutsceneKeyframeGroup createKeyframeGroup() {
        if (environment != Environment.DEVELOPMENT) return null;
        CutsceneKeyframe lastKeyframe = getLastKeyframeLastGroup();
        if (lastKeyframe != null && currentTick < lastKeyframe.getTick()) {
            playerSession
                    .getPlayer()
                    .sendSystemMessage(Translation.message("controller.cutscene.cant_create_keyframe_group"));
            return null;
        }
        CutsceneKeyframeGroup keyframeGroup = new CutsceneKeyframeGroup(keyframeGroupsCounter.incrementAndGet());
        selectedGroup = keyframeGroup;
        keyframeGroups.add(keyframeGroup);
        createKeyframe();
        if (lastKeyframe != null) {
            lastKeyframe.setTransitionDelayTick(currentTick - lastKeyframe.getTick());
        }
        keyframeGroup.showGroupText(playerSession.getPlayer());
        updateSelectedGroupGlow();
        return keyframeGroup;
    }

    public void updateSelectedGroupGlow() {
        for (CutsceneKeyframeGroup keyframeGroup : keyframeGroups) {
            if (keyframeGroup.getId() == selectedGroup.getId()) {
                keyframeGroup.showGlow(playerSession.getPlayer());
            } else {
                keyframeGroup.hideGlow(playerSession.getPlayer());
            }
        }
    }

    public CutsceneKeyframe createKeyframe() {
        if (environment != Environment.DEVELOPMENT) return null;
        int pathTime = 0;
        CutsceneKeyframe lastKeyframe = getLastKeyframeLastGroup();
        if (lastKeyframe != null) {
            pathTime = currentTick - lastKeyframe.getTick();
        }
        CutsceneKeyframe keyframe = new CutsceneKeyframe(
                keyframesCounter.incrementAndGet(), getKeyframeLocationFromPlayer(), currentTick, 0, pathTime);
        boolean insertedKeyframe = false;
        outer:
        for (CutsceneKeyframeGroup keyframeGroup : keyframeGroups) {
            for (int i = 0; i < keyframeGroup.getKeyframes().size(); i++) {
                if (keyframeGroup.getKeyframes().get(i).getTick() > currentTick) {
                    CutsceneKeyframe before = i > 0
                            ? keyframeGroup.getKeyframes().get(i - 1)
                            : keyframeGroup.getKeyframes().get(i);
                    CutsceneKeyframe after = keyframeGroup.getKeyframes().get(i);
                    keyframe.setPathTick(currentTick - before.getTick());
                    after.setPathTick(after.getTick() - currentTick);
                    selectedGroup = keyframeGroup;
                    selectedGroup.getKeyframes().add(i, keyframe);
                    updateCurrentTick(keyframe.getTick());
                    insertedKeyframe = true;
                    break outer;
                }
            }
        }
        CutsceneKeyframe lastKeyframeGroup = getLastKeyframeLastGroup();
        if (lastKeyframeGroup != null && currentTick > lastKeyframeGroup.getTick()) {
            selectedGroup = keyframeGroups.getLast();
        }
        if (!insertedKeyframe) {
            selectedGroup.addKeyframe(keyframe);
        }
        keyframe.setParentGroup(selectedGroup.getKeyframes().getFirst().getId() == keyframe.getId());
        keyframe.showKeyframe(playerSession.getPlayer());
        keyframe.getCamera().setGlowingTag(true);
        keyframe.updateEntityData(playerSession.getPlayer());
        updateSelectedGroupGlow();
        return keyframe;
    }

    public void drawLinesBetweenKeyframes(PoseStack poseStack) {
        if (playerSession.getCurrentCamera() != null || environment != Environment.DEVELOPMENT) return;
        Minecraft client = Minecraft.getInstance();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack.Pose matrix4f = poseStack.last();

        for (CutsceneKeyframeGroup keyframeGroup : keyframeGroups) {
            VertexConsumer vertexConsumer =
                    client.renderBuffers().bufferSource().getBuffer(RenderType.debugLineStrip(1.0F));
            for (int i = 0; i < keyframeGroup.getKeyframes().size() - 1; i++) {
                CutsceneKeyframe firstKeyFrame = keyframeGroup.getKeyframes().get(i);
                CutsceneKeyframe secondKeyFrame = keyframeGroup.getKeyframes().get(i + 1);

                KeyframeLocation startPos = firstKeyFrame.getKeyframeLocation();
                KeyframeLocation endPos = secondKeyFrame.getKeyframeLocation();
                double x1 = startPos.getX() - cameraPos.x;
                double y1 = startPos.getY() - cameraPos.y;
                double z1 = startPos.getZ() - cameraPos.z;
                double x2 = endPos.getX() - cameraPos.x;
                double y2 = endPos.getY() - cameraPos.y;
                double z2 = endPos.getZ() - cameraPos.z;

                vertexConsumer
                        .addVertex(matrix4f, new Vector3f((float) x1, (float) y1, (float) z1))
                        .setColor(1.0F, 1.0F, 0.0F, 1.0F)
                        .setNormal(0, 1, 0);
                vertexConsumer
                        .addVertex(matrix4f, new Vector3f((float) x2, (float) y2, (float) z2))
                        .setColor(1.0F, 1.0F, 0.0F, 1.0F)
                        .setNormal(0, 1, 0);
            }
            client.renderBuffers().bufferSource().endBatch();
        }
    }

    public CutsceneKeyframe getLastKeyframeLastGroup() {
        if (keyframeGroups.isEmpty()) return null;
        List<CutsceneKeyframe> keyframes = keyframeGroups.getLast().getKeyframes();
        if (keyframes.isEmpty()) return null;
        return keyframes.getLast();
    }

    public CutsceneKeyframe getLatestKeyframeFromId() {
        if (keyframeGroups.isEmpty()) return null;

        CutsceneKeyframe latest = null;
        for (CutsceneKeyframeGroup group : keyframeGroups) {
            for (CutsceneKeyframe keyframe : group.getKeyframes()) {
                if (latest == null || keyframe.getId() > latest.getId()) {
                    latest = keyframe;
                }
            }
        }
        return latest;
    }

    public void updateCurrentTick(int tick) {
        List<CutsceneKeyframeGroup> groups = keyframeGroups;

        int initialFirstTick = 0;
        if (!groups.isEmpty() && !groups.getFirst().getKeyframes().isEmpty()) {
            initialFirstTick = groups.getFirst().getKeyframes().getFirst().getTick();
        }

        int referenceTick = 0;

        for (int i = 0; i < groups.size(); i++) {
            CutsceneKeyframeGroup group = groups.get(i);
            List<CutsceneKeyframe> keyframes = group.getKeyframes();
            if (keyframes.isEmpty()) continue;

            if (i > 0) {
                List<CutsceneKeyframe> prevGroup = groups.get(i - 1).getKeyframes();
                if (!prevGroup.isEmpty()) {
                    CutsceneKeyframe lastPrev = prevGroup.getLast();
                    referenceTick += lastPrev.getTransitionDelayTick();
                }
            }

            for (int j = 0; j < keyframes.size(); j++) {
                CutsceneKeyframe current = keyframes.get(j);
                int newTick;

                if (i == 0 && j == 0) {
                    newTick = initialFirstTick + current.getStartDelayTick();
                } else if (j > 0) {
                    CutsceneKeyframe previous = keyframes.get(j - 1);
                    newTick = previous.getTick() + previous.getStartDelayTick() + current.getPathTick();
                } else {
                    newTick = referenceTick + current.getStartDelayTick();
                }

                current.setTick(newTick);
                referenceTick = newTick;
            }
        }
        changeTimePosition(tick, true);
    }

    public boolean atMaxTick() {
        return currentTick >= totalTick;
    }

    public void nextSecondSkip() {
        if (playbacks.isEmpty()) {
            currentTick += skipTickCount;
        } else {
            changeTimePosition(currentTick + skipTickCount, true);
        }
    }

    public void previousSecondSkip() {
        if (playbacks.isEmpty()) {
            currentTick = Math.max(0, currentTick - skipTickCount);
        } else {
            changeTimePosition(Math.max(0, currentTick - skipTickCount), true);
        }
    }

    public void changeTimePosition(int newTick, boolean seamless) {
        currentTick = Math.min(newTick, totalTick);
        if (playbacks.isEmpty()) {
            currentTick = newTick;
        }
        for (Playback playback : playbacks) {
            playback.changeLocationByTick(newTick, seamless);
        }
    }

    public boolean isLastKeyframe(CutsceneKeyframe keyframe) {
        if (keyframeGroups.isEmpty()) return false;
        if (keyframeGroups.getLast().getKeyframes().isEmpty()) return false;
        return keyframeGroups.getLast().isLastKeyframe(keyframe);
    }

    private int calculateTotalTick() {
        int totalTick = 0;

        if (!playbacks.isEmpty()) {
            int maxPlaybackTick = 0;

            for (Subscene subscene : cutscene.getSubscenes()) {
                for (Playback p : subscene.getPlaybacks()) {
                    maxPlaybackTick = Math.max(maxPlaybackTick, p.getMaxTick());
                }
            }

            for (Playback p : playbacks) {
                maxPlaybackTick = Math.max(maxPlaybackTick, p.getMaxTick());
            }

            int additionalTick = 0;
            for (CutsceneKeyframeGroup group : keyframeGroups) {
                for (CutsceneKeyframe kf : group.getKeyframes()) {
                    if (kf.getTick() >= maxPlaybackTick) {
                        additionalTick += kf.getPathTick() + kf.getTransitionDelayTick();
                    }
                }
            }

            totalTick = maxPlaybackTick + additionalTick;
        } else {
            CutsceneKeyframe keyframe = getLastKeyframeLastGroup();
            if (keyframe != null) {
                totalTick = keyframe.getTick();
            }
        }

        this.totalTick = totalTick;
        return totalTick;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
        for (Playback playback : playbacks) {
            playback.setPlaying(playing);
        }
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public int getSkipTickCount() {
        return skipTickCount;
    }

    public void setSkipTickCount(int skipTickCount) {
        this.skipTickCount = skipTickCount;
    }

    public void setSelectedGroup(CutsceneKeyframeGroup selectedGroup) {
        this.selectedGroup = selectedGroup;
        updateSelectedGroupGlow();
    }

    public List<Playback> getPlaybacks() {
        return playbacks;
    }

    public Cutscene getCutscene() {
        return cutscene;
    }

    public int getTotalTick() {
        return totalTick;
    }

    public CutsceneKeyframeGroup getSelectedGroup() {
        return selectedGroup;
    }

    public CutscenePlayback getCutscenePlayback() {
        return cutscenePlayback;
    }

    public Runnable getOnEnd() {
        return onEnd;
    }

    public void setOnEnd(Runnable onEnd) {
        this.onEnd = onEnd;
    }
}
