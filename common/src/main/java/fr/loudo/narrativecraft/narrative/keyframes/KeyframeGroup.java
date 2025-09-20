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

package fr.loudo.narrativecraft.narrative.keyframes;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

public class KeyframeGroup<T extends Keyframe> {
    protected final List<T> keyframes = new ArrayList<>();

    public List<T> getKeyframes() {
        return keyframes;
    }

    public boolean isLastKeyframe(T keyframe) {
        if (keyframes.isEmpty()) return false;
        return keyframes.getLast().getId() == keyframe.getId();
    }

    public void hideKeyframes(ServerPlayer player) {
        for (T keyframe : keyframes) {
            keyframe.hideKeyframe(player);
        }
    }

    public void showKeyframes(ServerPlayer player) {
        for (T keyframe : keyframes) {
            keyframe.showKeyframe(player);
        }
    }

    public void addKeyframe(T keyframe) {
        if (keyframes.contains(keyframe)) return;
        keyframes.add(keyframe);
    }

    public void removeKeyframe(T keyframe) {
        keyframes.remove(keyframe);
    }

    public T getKeyframeById(int id) {
        for (T keyframe : keyframes) {
            if (keyframe.id == id) {
                return keyframe;
            }
        }
        return null;
    }
}
