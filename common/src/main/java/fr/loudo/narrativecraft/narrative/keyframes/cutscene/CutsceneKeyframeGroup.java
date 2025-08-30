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

package fr.loudo.narrativecraft.narrative.keyframes.cutscene;

import fr.loudo.narrativecraft.narrative.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.server.level.ServerPlayer;

public class CutsceneKeyframeGroup extends KeyframeGroup<CutsceneKeyframe> {

    private int id;

    public CutsceneKeyframeGroup(int id) {
        this.id = id;
    }

    public void showGroupText(ServerPlayer player) {
        if (keyframes.isEmpty()) return;
        CutsceneKeyframe keyframe = keyframes.getFirst();
        keyframe.getCamera().setCustomNameVisible(true);
        keyframe.getCamera().setCustomName(Translation.message("controller.cutscene.keyframe_group.start_text", id));
        keyframe.updateEntityData(player);
    }

    public void showGlow(ServerPlayer player) {
        for (CutsceneKeyframe keyframe : keyframes) {
            keyframe.getCamera().setGlowingTag(true);
            keyframe.updateEntityData(player);
        }
    }

    public void hideGlow(ServerPlayer player) {
        for (CutsceneKeyframe keyframe : keyframes) {
            keyframe.getCamera().setGlowingTag(false);
            keyframe.updateEntityData(player);
        }
    }

    public long getTotalDuration() {
        long totalDuration = 0;
        for (CutsceneKeyframe keyframe : keyframes) {
            totalDuration += keyframe.getPathTime();
        }
        return totalDuration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
