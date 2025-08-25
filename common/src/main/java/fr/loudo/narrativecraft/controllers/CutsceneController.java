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

package fr.loudo.narrativecraft.controllers;

import fr.loudo.narrativecraft.managers.PlaybackManager;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframeGroup;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.screens.controller.cutscene.CutsceneControllerScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

public class CutsceneController extends AbstractController {

    private final List<Playback> playbacks = new ArrayList<>();
    private final Cutscene cutscene;

    private boolean isPlaying;
    private int currentTick, skipTickCount;
    private int totalTick;
    private CutsceneKeyframeGroup selectedGroup;

    public CutsceneController(Environment environment, Player player, Cutscene cutscene) {
        super(environment, player);
        this.cutscene = cutscene;
        skipTickCount = 20;
        isPlaying = false;
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
        if (!cutscene.getKeyframeGroups().isEmpty()) {
            selectedGroup = cutscene.getKeyframeGroups().getFirst();
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

    @Override
    public void stopSession(boolean save) {
        for (Playback playback : playbacks) {
            playback.stop(true);
        }
        playerSession.setController(null);
    }

    @Override
    public Screen getControllerScreen() {
        return new CutsceneControllerScreen(this);
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
    }
}
