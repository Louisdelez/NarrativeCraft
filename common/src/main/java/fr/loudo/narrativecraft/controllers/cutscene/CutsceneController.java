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
import fr.loudo.narrativecraft.controllers.keyframe.AbstractKeyframeGroupsBase;
import fr.loudo.narrativecraft.managers.PlaybackManager;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframeGroup;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.screens.controller.cutscene.CutsceneControllerScreen;
import fr.loudo.narrativecraft.screens.controller.cutscene.CutsceneKeyframeOptionScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CutsceneController extends AbstractKeyframeGroupsBase<CutsceneKeyframe, CutsceneKeyframeGroup> {

    private final AtomicInteger keyframeGroupCounter = new AtomicInteger();
    private final AtomicInteger keyframeCounter = new AtomicInteger();

    private final List<Playback> playbacks = new ArrayList<>();
    private final Cutscene cutscene;
    private final CutscenePlayback cutscenePlayback;

    private boolean isPlaying;
    private int currentTick, skipTickCount;
    private int totalTick;
    private CutsceneKeyframeGroup selectedGroup;

    public CutsceneController(Environment environment, Player player, Cutscene cutscene) {
        super(environment, player);
        this.cutscene = cutscene;
        skipTickCount = 20;
        isPlaying = false;
        keyframeGroups.addAll(cutscene.getKeyframeGroups());
        cutscenePlayback = new CutscenePlayback(this, () -> {});
    }

    public void tick() {
        hudMessage = Translation.message("controller.cutscene.hud", currentTick, totalTick)
                .getString();
        if (!isPlaying) return;
        currentTick++;
        for (Playback playback : playbacks) {
            playback.tick();
        }
        if (currentTick >= totalTick) {
            pause();
            if (Minecraft.getInstance().screen instanceof CutsceneControllerScreen screen) {
                screen.getControllerButton().setMessage(screen.getPlayText());
            }
        }
    }

    @Override
    public void startSession() {
        stopSession(false);
        playerSession.setController(this);
        for (Subscene subscene : cutscene.getSubscenes()) {
            subscene.start(playerSession.getPlayer().level(), environment, false);
            playbacks.addAll(subscene.getPlaybacks());
        }
        for (Animation animation : cutscene.getAnimations()) {
            Playback playback = new Playback(
                    PlaybackManager.ids.incrementAndGet(),
                    animation,
                    playerSession.getPlayer().level(),
                    environment,
                    false);
            playback.start();
            playback.setPlaying(false);
            playbacks.add(playback);
        }
        if (!keyframeGroups.isEmpty()) {
            selectedGroup = keyframeGroups.getFirst();
            KeyframeLocation keyframeLocation =
                    selectedGroup.getKeyframes().getFirst().getKeyframeLocation();
            playerSession
                    .getPlayer()
                    .teleportTo(keyframeLocation.getX(), keyframeLocation.getY(), keyframeLocation.getY());
        } else {
            Location location = playbacks.getFirst().getAnimation().getFirstLocation();
            playerSession.getPlayer().teleportTo(location.x(), location.y(), location.z());
        }
        totalTick = calculateTotalTick();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.setScreen(new CutsceneControllerScreen(this)));
        if (!keyframeGroups.isEmpty()) {
            CutsceneKeyframeGroup keyframeGroup = keyframeGroups.getLast();
            keyframeGroupCounter.set(keyframeGroup.getId());
            CutsceneKeyframe cutsceneKeyframe = getLastKeyframeLastGroup();
            if (cutsceneKeyframe == null) return;
            keyframeCounter.set(keyframeGroup.getId());
        }
    }

    @Override
    public void stopSession(boolean save) {
        for (Playback playback : playbacks) {
            playback.stop(true);
        }
        for (CutsceneKeyframeGroup keyframeGroup : keyframeGroups) {
            keyframeGroup.hideKeyframes(playerSession.getPlayer());
        }
        playerSession.setController(null);
        playerSession.setCurrentCamera(null);
    }

    @Override
    public Screen getControllerScreen() {
        return new CutsceneControllerScreen(this);
    }

    @Override
    public void setCamera(Keyframe keyframe) {
        super.setCamera(keyframe);
        CutsceneKeyframe cutsceneKeyframe = (CutsceneKeyframe) keyframe;
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
    public Screen keyframeOptionScreen(Keyframe keyframe) {
        return new CutsceneKeyframeOptionScreen((CutsceneKeyframe) keyframe, playerSession.getPlayer(), false);
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
            keyframeGroupCounter.decrementAndGet();
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
        CutsceneKeyframeGroup keyframeGroup = new CutsceneKeyframeGroup(keyframeGroupCounter.incrementAndGet());
        selectedGroup = keyframeGroup;
        keyframeGroups.add(keyframeGroup);
        CutsceneKeyframe keyframe = createKeyframe();
        if (lastKeyframe != null) {
            keyframe.setTransitionDelayTick(currentTick - keyframe.getTick());
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
        ServerPlayer player = playerSession.getPlayer();
        KeyframeLocation location = new KeyframeLocation(
                player.position().add(0, player.getEyeHeight(), 0), player.getXRot(), player.getYRot(), 0, 85.0f);
        int pathTime = 0;
        CutsceneKeyframe lastKeyframe = getLastKeyframeLastGroup();
        if (lastKeyframe != null) {
            pathTime = currentTick - lastKeyframe.getTick();
        }
        CutsceneKeyframe keyframe =
                new CutsceneKeyframe(keyframeCounter.incrementAndGet(), location, currentTick, 0, pathTime);
        for (CutsceneKeyframeGroup keyframeGroup : keyframeGroups) {
            for (int i = 0; i < keyframeGroup.getKeyframes().size(); i++) {
                if (keyframeGroup.getKeyframes().get(i).getTick() > currentTick) {
                    CutsceneKeyframe before = keyframeGroup.getKeyframes().get(i - 1);
                    CutsceneKeyframe after = keyframeGroup.getKeyframes().get(i);
                    keyframe.setPathTick((before.getTick() + after.getTick()) - currentTick);
                    selectedGroup.getKeyframes().add(i, keyframe);
                    updateCurrentTick(keyframe.getTick());
                    break;
                }
            }
        }
        selectedGroup.addKeyframe(keyframe);
        keyframe.setParentGroup(selectedGroup.getKeyframes().getFirst().getId() == keyframe.getId());
        keyframe.showKeyframe(player);
        keyframe.getCamera().setGlowingTag(true);
        keyframe.updateEntityData(player);
        return keyframe;
    }

    public void drawLinesBetweenKeyframes(PoseStack poseStack) {
        if (playerSession.getCurrentCamera() != null) return;
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
        changeTimePosition(currentTick + skipTickCount, true);
    }

    public void previousSecondSkip() {
        changeTimePosition(Math.max(0, currentTick - skipTickCount), true);
    }

    public void changeTimePosition(int newTick, boolean seamless) {
        currentTick = Math.min(newTick, totalTick);
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
        if (totalTick == 0) {
            int total = 0;
            int count = 0;

            for (Subscene subscene : cutscene.getSubscenes()) {
                for (Playback playback : subscene.getPlaybacks()) {
                    total += playback.getMaxTick();
                    count++;
                }
            }

            for (Playback playback : playbacks) {
                total += playback.getMaxTick();
                count++;
            }

            if (count == 0) return 0;
            totalTick = total / count;
        }
        return totalTick;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
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
}
