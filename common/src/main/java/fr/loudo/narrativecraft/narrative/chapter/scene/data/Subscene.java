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

package fr.loudo.narrativecraft.narrative.chapter.scene.data;

import fr.loudo.narrativecraft.managers.PlaybackManager;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.Level;

public class Subscene extends SceneData {

    private transient List<Animation> animations = new ArrayList<>();
    private transient List<Playback> playbacks = new ArrayList<>();

    public Subscene(String name, String description, Scene scene) {
        super(name, description, scene);
    }

    public void start(Level level, Environment environment, boolean looping) {
        playbacks = getPlaybacks();
        playbacks.clear();
        for (Animation animation : animations) {
            Playback playback = new Playback(
                    PlaybackManager.ID_INCREMENTER.incrementAndGet(), animation, level, environment, looping);
            playback.start();
            playbacks.add(playback);
        }
    }

    public void start(Level level, Environment environment, boolean looping, StoryHandler storyHandler) {
        playbacks = getPlaybacks();
        playbacks.clear();
        for (Animation animation : animations) {
            Playback playback = new Playback(
                    PlaybackManager.ID_INCREMENTER.incrementAndGet(), animation, level, environment, looping);
            if (storyHandler != null) {
                playback.startFromStory(storyHandler);
            } else {
                playback.start();
            }
            playbacks.add(playback);
        }
    }

    public void stop(boolean killEntity) {
        for (Playback playback : playbacks) {
            playback.stop(killEntity);
        }
        getPlaybacks().clear();
    }

    public boolean isPlaying() {
        for (Playback playback : playbacks) {
            if (!playback.isPlaying()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasEnded() {
        for (Playback playback : playbacks) {
            if (!playback.hasEnded()) {
                return false;
            }
        }
        return true;
    }

    public List<Playback> getPlaybacks() {
        if (playbacks == null) {
            playbacks = new ArrayList<>();
        }
        return playbacks;
    }

    public List<Animation> getAnimations() {
        if (animations == null) {
            animations = new ArrayList<>();
        }
        return animations;
    }

    public List<String> getAnimationsName() {
        return getAnimations().stream().map(Animation::getName).toList();
    }
}
