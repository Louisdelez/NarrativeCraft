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

import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframeGroup;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.controller.cutscene.CutsceneKeyframeOptionScreen;
import fr.loudo.narrativecraft.util.CatmullRoll;
import fr.loudo.narrativecraft.util.Easing;
import fr.loudo.narrativecraft.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CutscenePlayback {

    private final CutsceneController cutsceneController;
    private final PlayerSession playerSession;
    private final Runnable onCutsceneEnd;
    private CutsceneKeyframe keyframeA;
    private CutsceneKeyframe keyframeB;
    private CutsceneKeyframeGroup currentKeyframeGroup;
    private int segmentTick;
    private int totalTick;
    private boolean isPlaying;

    public CutscenePlayback(CutsceneController cutsceneController, Runnable onCutsceneEnd) {
        this.cutsceneController = cutsceneController;
        this.playerSession = cutsceneController.getPlayerSession();
        this.onCutsceneEnd = onCutsceneEnd;
    }

    public void setupAndPlay(CutsceneKeyframe keyframeA, CutsceneKeyframe keyframeB) {
        this.keyframeA = keyframeA;
        this.keyframeB = keyframeB;
        currentKeyframeGroup = cutsceneController.getKeyframeGroupOfKeyframe(keyframeA);
        cutsceneController.setPlaying(true);
        segmentTick = 0;
        totalTick = keyframeA.getTick();
        play();
    }

    public void play() {
        isPlaying = true;
    }

    public void stop() {
        isPlaying = false;
        cutsceneController.setPlaying(false);
    }

    public void tick() {
        if (!isPlaying) return;
        segmentTick++;
        if (segmentTick >= keyframeA.getStartDelayTick()) {
            totalTick++;
        }
    }

    public void cameraInterpolation(double partialTick) {
        if (!isPlaying) return;

        double totalDelta;
        if (segmentTick < keyframeA.getStartDelayTick()) {
            playerSession.setCurrentCamera(keyframeA.getKeyframeLocation());
            return;
        } else {
            totalDelta = Math.clamp(
                    (segmentTick + partialTick - keyframeA.getStartDelayTick()) / keyframeB.getPathTick(), 0.0, 1.0);
        }

        if (keyframeB.getEasing() != Easing.SMOOTH
                || currentKeyframeGroup.getKeyframes().size() < 2) {
            totalDelta = keyframeB.getEasing().interpolate(totalDelta);
            playerSession.setCurrentCamera(
                    interpolate(totalDelta, keyframeA.getKeyframeLocation(), keyframeB.getKeyframeLocation()));
        } else {
            KeyframeLocation location = CatmullRoll.interpolate(partialTick, currentKeyframeGroup, totalTick);
            float roll = MathHelper.normalRotation(
                    Easing.SMOOTH.interpolate(totalDelta),
                    keyframeA.getKeyframeLocation().getRoll(),
                    keyframeB.getKeyframeLocation().getRoll()); // Roll separated from main catmull roll
            location.setRoll(roll);
            playerSession.setCurrentCamera(location);
        }

        if (totalDelta >= 1.0
                && segmentTick
                        >= (keyframeB.getPathTick()
                                + keyframeB.getTransitionDelayTick()
                                + keyframeA.getStartDelayTick())) {
            if (cutsceneController.isLastKeyframe(keyframeB)) {
                onEnd();
            } else {
                pickNextKeyframes();
            }
        }
    }

    private void onEnd() {
        stop();
        if (cutsceneController.getEnvironment() == Environment.DEVELOPMENT) {
            CutsceneKeyframeOptionScreen screen =
                    new CutsceneKeyframeOptionScreen(keyframeB, playerSession.getPlayer(), false);
            Minecraft.getInstance().setScreen(screen);
        } else if (cutsceneController.getEnvironment() == Environment.PRODUCTION) {
            onCutsceneEnd.run();
        }
    }

    private void pickNextKeyframes() {
        keyframeA = keyframeB;
        keyframeB = cutsceneController.getNextKeyframe(keyframeB);
        segmentTick = 0;
        currentKeyframeGroup = cutsceneController.getKeyframeGroupOfKeyframe(keyframeA);
    }

    public KeyframeLocation interpolate(double delta, KeyframeLocation a, KeyframeLocation b) {
        if (!isPlaying) return null;
        Vec3 position = Mth.lerp(delta, a.getPosition(), b.getPosition());
        float pitch = (float) Mth.lerp(delta, a.getPitch(), b.getPitch());
        float yaw = MathHelper.normalYaw(delta, a.getYaw(), b.getYaw());
        float roll = MathHelper.normalRotation(delta, a.getRoll(), b.getRoll());
        float fov = (float) Mth.lerp(delta, a.getFov(), b.getFov());
        return new KeyframeLocation(position, pitch, yaw, roll, fov);
    }

    public CutsceneKeyframe getKeyframeA() {
        return keyframeA;
    }

    public void setKeyframeA(CutsceneKeyframe keyframeA) {
        this.keyframeA = keyframeA;
    }

    public CutsceneKeyframe getKeyframeB() {
        return keyframeB;
    }

    public void setKeyframeB(CutsceneKeyframe keyframeB) {
        this.keyframeB = keyframeB;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getSegmentTick() {
        return segmentTick;
    }

    public void setSegmentTick(int segmentTick) {
        this.segmentTick = segmentTick;
    }
}
