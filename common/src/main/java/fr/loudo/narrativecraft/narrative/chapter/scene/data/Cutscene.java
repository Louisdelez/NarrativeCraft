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

import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframeGroup;
import java.util.ArrayList;
import java.util.List;

public class Cutscene extends SceneData {

    private transient List<Subscene> subscenes = new ArrayList<>();
    private transient List<Animation> animations = new ArrayList<>(); // For individual animations.
    private List<CutsceneKeyframeGroup> keyframeGroups = new ArrayList<>();

    public Cutscene(String name, String description, Scene scene) {
        super(name, description, scene);
    }

    public List<String> getSubscenesName() {
        return getSubscenes().stream().map(Subscene::getName).toList();
    }

    public List<Subscene> getSubscenes() {
        if (subscenes == null) {
            subscenes = new ArrayList<>();
        }
        return subscenes;
    }

    public List<String> getAnimationsName() {
        return getAnimations().stream().map(Animation::getName).toList();
    }

    public List<Animation> getAnimations() {
        if (animations == null) {
            animations = new ArrayList<>();
        }
        return animations;
    }

    public void addKeyframeGroup(CutsceneKeyframeGroup keyframeGroup) {
        if (keyframeGroups.contains(keyframeGroup)) return;
        keyframeGroups.add(keyframeGroup);
    }

    public void removeKeyframeGroup(CutsceneKeyframeGroup keyframeGroup) {
        keyframeGroups.remove(keyframeGroup);
    }

    public List<CutsceneKeyframeGroup> getKeyframeGroups() {
        if (keyframeGroups == null) {
            keyframeGroups = new ArrayList<>();
        }
        return keyframeGroups;
    }
}
