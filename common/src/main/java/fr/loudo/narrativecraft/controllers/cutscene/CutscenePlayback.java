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
import fr.loudo.narrativecraft.util.Easing;
import fr.loudo.narrativecraft.util.MathHelper;
import java.util.List;
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
        if (Minecraft.getInstance().options.keyShift.isDown()
                && cutsceneController.getEnvironment() == Environment.DEVELOPMENT) {
            stop();
            playerSession.setCurrentCamera(keyframeA.getKeyframeLocation());
            Minecraft.getInstance().setScreen(new CutsceneKeyframeOptionScreen(keyframeA, playerSession, false));
            cutsceneController.changeTimePosition(keyframeA.getTick(), true);
        }
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
            playerSession.setCurrentCamera(interpolate(
                    totalDelta / keyframeB.getSpeed(),
                    keyframeA.getKeyframeLocation(),
                    keyframeB.getKeyframeLocation()));
        } else {
            KeyframeLocation location = interpolateByCatmull(partialTick);
            playerSession.setCurrentCamera(location);
        }

        double extraDelay = 0;
        if (currentKeyframeGroup.isLastKeyframe(keyframeB)) {
            extraDelay += keyframeB.getTransitionDelayTick();
        }
        if (totalDelta >= 1.0
                && segmentTick >= (keyframeB.getPathTick() + keyframeA.getStartDelayTick() + extraDelay)) {
            if (cutsceneController.isLastKeyframe(keyframeB)) {
                onEnd();
            } else {
                pickNextKeyframes();
            }
        }
    }

    private KeyframeLocation interpolateByCatmull(double partialTick) {
        List<CutsceneKeyframe> keyframes = currentKeyframeGroup.getKeyframes();
        if (keyframes.size() < 2) return keyframes.getLast().getKeyframeLocation();

        int startIndex = 0;
        for (CutsceneKeyframe keyframe : keyframes) {
            if (keyframe.getId() == keyframeA.getId()) {
                break;
            }
            startIndex++;
        }

        double elapsedTick = (totalTick + partialTick) - keyframeA.getTick();
        int accumulatedTick = 0;
        for (int i = startIndex; i < keyframes.size() - 1; i++) {
            CutsceneKeyframe k1 = keyframes.get(i);
            CutsceneKeyframe k2 = keyframes.get(i + 1);

            int segmentDuration = (int) (k2.getPathTick() / k2.getSpeed());
            if (elapsedTick < accumulatedTick + segmentDuration) {
                double t = (elapsedTick - accumulatedTick) / segmentDuration;

                CutsceneKeyframe p0 = keyframes.get(Math.max(i - 1, 0));
                CutsceneKeyframe p1 = k1;
                CutsceneKeyframe p2 = k2;
                CutsceneKeyframe p3 = keyframes.get(Math.min(i + 2, keyframes.size() - 1));

                return catmullRom(
                        p0.getKeyframeLocation(),
                        p1.getKeyframeLocation(),
                        p2.getKeyframeLocation(),
                        p3.getKeyframeLocation(),
                        t);
            }
            accumulatedTick += segmentDuration;
        }
        return keyframes.getLast().getKeyframeLocation();
    }

    private KeyframeLocation catmullRom(
            KeyframeLocation p0, KeyframeLocation p1, KeyframeLocation p2, KeyframeLocation p3, double t) {

        double x = MathHelper.catmullRom(
                (float) p0.getX(), (float) p1.getX(), (float) p2.getX(), (float) p3.getX(), (float) t);

        double y = MathHelper.catmullRom(
                (float) p0.getY(), (float) p1.getY(), (float) p2.getY(), (float) p3.getY(), (float) t);

        double z = MathHelper.catmullRom(
                (float) p0.getZ(), (float) p1.getZ(), (float) p2.getZ(), (float) p3.getZ(), (float) t);

        float pitch = MathHelper.catmullRom(p0.getPitch(), p1.getPitch(), p2.getPitch(), p3.getPitch(), (float) t);

        float yaw = MathHelper.catmullRom(
                Mth.wrapDegrees(p0.getYaw()),
                Mth.wrapDegrees(p1.getYaw()),
                Mth.wrapDegrees(p2.getYaw()),
                Mth.wrapDegrees(p3.getYaw()),
                (float) t);

        float fov = MathHelper.catmullRom(p0.getFov(), p1.getFov(), p2.getFov(), p3.getFov(), (float) t);

        float roll = MathHelper.catmullRom(
                Mth.wrapDegrees(p0.getRoll()),
                Mth.wrapDegrees(p1.getRoll()),
                Mth.wrapDegrees(p2.getRoll()),
                Mth.wrapDegrees(p3.getRoll()),
                (float) t);

        return new KeyframeLocation(x, y, z, pitch, yaw, roll, fov);
    }

    private void onEnd() {
        stop();
        playerSession.setCurrentCamera(keyframeB.getKeyframeLocation());
        if (cutsceneController.getEnvironment() == Environment.DEVELOPMENT) {
            CutsceneKeyframeOptionScreen screen = new CutsceneKeyframeOptionScreen(keyframeB, playerSession, false);
            Minecraft.getInstance().setScreen(screen);
        } else if (cutsceneController.getEnvironment() == Environment.PRODUCTION) {
            onCutsceneEnd.run();
        }
    }

    private void pickNextKeyframes() {
        if (currentKeyframeGroup.getKeyframes().size() == 1) {
            keyframeB = cutsceneController.getNextKeyframe(keyframeB);
        }
        CutsceneKeyframeGroup cutsceneKeyframeGroupB = cutsceneController.getKeyframeGroupOfKeyframe(keyframeB);
        if (cutsceneKeyframeGroupB.getKeyframes().size() == 1) {
            keyframeA = cutsceneKeyframeGroupB.getKeyframes().getFirst();
        } else if (currentKeyframeGroup.getId() != cutsceneKeyframeGroupB.getId()) {
            keyframeA = cutsceneKeyframeGroupB.getKeyframes().getFirst();
            keyframeB = cutsceneController.getNextKeyframe(keyframeA);
        } else {
            keyframeA = keyframeB;
            keyframeB = cutsceneController.getNextKeyframe(keyframeB);
        }
        segmentTick = 0;
        currentKeyframeGroup = cutsceneKeyframeGroupB;
    }

    public KeyframeLocation interpolate(double delta, KeyframeLocation a, KeyframeLocation b) {
        if (!isPlaying) return null;
        Vec3 position = Mth.lerp(delta, a.getPosition(), b.getPosition());
        float pitch = (float) Mth.lerp(delta, a.getPitch(), b.getPitch());
        float yaw = (float) Mth.lerp(delta, Mth.wrapDegrees(a.getYaw()), Mth.wrapDegrees(b.getYaw()));
        float roll = (float) Mth.lerp(delta, Mth.wrapDegrees(a.getRoll()), Mth.wrapDegrees(b.getRoll()));
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

    public int getTotalTick() {
        return totalTick;
    }

    public void setTotalTick(int totalTick) {
        this.totalTick = totalTick;
    }
}
