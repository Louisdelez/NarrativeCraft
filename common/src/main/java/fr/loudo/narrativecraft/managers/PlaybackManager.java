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

package fr.loudo.narrativecraft.managers;

import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.entity.Entity;

public class PlaybackManager {

    public static final AtomicInteger ID_INCREMENTER = new AtomicInteger();
    private final List<Playback> playbacks = new ArrayList<>();

    public List<Playback> getPlaybacksPlaying() {
        return playbacks.stream().filter(Playback::isPlaying).toList();
    }

    public List<Playback> getAnimationsByNamePlaying() {
        return playbacks;
    }

    public Playback createAndStart(Animation animation, Environment env, PlayerSession session) {
        Playback playback = new Playback(
                ID_INCREMENTER.incrementAndGet(), animation, session.getPlayer().level(), env, false);
        playback.start();
        this.addPlayback(playback);
        return playback;
    }

    public void addPlayback(Playback playback) {
        for (Playback playback1 : playbacks) {
            if (playback1.getId() == playback.getId()) {
                return;
            }
        }
        playbacks.add(playback);
    }

    public void removePlayback(Playback playback) {
        playbacks.remove(playback);
    }

    public Playback getPlayback(int id) {
        for (Playback playback : playbacks) {
            if (playback.getId() == id) {
                return playback;
            }
        }
        return null;
    }

    public Playback getPlayback(String animationName) {
        for (Playback playback : playbacks) {
            if (playback.getAnimation().getName().equalsIgnoreCase(animationName)) {
                return playback;
            }
        }
        return null;
    }

    public List<Playback> getAnimationsByNamePlaying(String animationName) {
        List<Playback> animationsPlaying = new ArrayList<>();
        for (Playback playback : playbacks) {
            if (playback.getAnimation().getName().equalsIgnoreCase(animationName)) {
                animationsPlaying.add(playback);
            }
        }
        return animationsPlaying;
    }

    public boolean entityInPlayback(Entity entity) {
        for (Playback playback : playbacks) {
            if (playback.entityInPlayback(entity)) return true;
        }
        return false;
    }

    public List<Playback> getPlaybacks() {
        return playbacks;
    }

    public void stopAll() {
        for (Playback playback : playbacks) {
            playback.stop(true);
        }
        playbacks.clear();
    }
}
