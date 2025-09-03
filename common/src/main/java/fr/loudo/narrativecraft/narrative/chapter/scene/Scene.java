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

package fr.loudo.narrativecraft.narrative.chapter.scene;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.util.Util;
import java.util.ArrayList;
import java.util.List;

public class Scene extends NarrativeEntry {

    private final Chapter chapter;

    private final List<Animation> animations = new ArrayList<>();
    private final List<Cutscene> cutscenes = new ArrayList<>();
    private final List<Subscene> subscenes = new ArrayList<>();
    private final List<CameraAngle> cameraAngles = new ArrayList<>();

    private final List<CharacterStory> npcs = new ArrayList<>();

    private int rank;

    public Scene(String name, String description, Chapter chapter) {
        super(name, description);
        this.chapter = chapter;
        this.rank = chapter.getScenes().size() + 1;
    }

    public String knotName() {
        return "chapter_" + chapter.getIndex() + "_" + Util.snakeCase(name.toLowerCase());
    }

    public Animation getAnimationByName(String name) {
        for (Animation animation : animations) {
            if (animation.getName().equalsIgnoreCase(name)) {
                return animation;
            }
        }
        return null;
    }

    public Subscene getSubsceneByName(String name) {
        for (Subscene subscene : subscenes) {
            if (subscene.getName().equalsIgnoreCase(name)) {
                return subscene;
            }
        }
        return null;
    }

    public boolean subsceneExists(String name) {
        for (Subscene subscene : subscenes) {
            if (subscene.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean cutsceneExists(String name) {
        for (Cutscene cutscene : cutscenes) {
            if (cutscene.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean animationExists(String name) {
        for (Animation animation : animations) {
            if (animation.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public void addCameraAngleGroup(CameraAngle cameraAngleGroup) {
        if (cameraAngles.contains(cameraAngleGroup)) return;
        cameraAngles.add(cameraAngleGroup);
    }

    public void removeCameraAngleGroup(CameraAngle cameraAngleGroup) {
        cameraAngles.remove(cameraAngleGroup);
    }

    public boolean cameraAngleExists(String name) {
        for (CameraAngle cameraAngleGroup : cameraAngles) {
            if (cameraAngleGroup.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public CharacterStory getNpcByName(String name) {
        for (CharacterStory characterStory : npcs) {
            if (characterStory.getName().equalsIgnoreCase(name)) {
                return characterStory;
            }
        }
        return null;
    }

    public void addSubscene(Subscene subscene) {
        if (subscenes.contains(subscene)) return;
        subscenes.add(subscene);
    }

    public void removeSubscene(Subscene subscene) {
        subscenes.remove(subscene);
    }

    public void addCutscene(Cutscene cutscene) {
        if (cutscenes.contains(cutscene)) return;
        cutscenes.add(cutscene);
    }

    public void removeCutscene(Cutscene cutscene) {
        cutscenes.remove(cutscene);
    }

    public void addAnimation(Animation animation) {
        if (animations.contains(animation)) return;
        animations.add(animation);
    }

    public void removeAnimation(Animation animation) {
        animations.remove(animation);
    }

    public Chapter getChapter() {
        return chapter;
    }

    public List<Animation> getAnimations() {
        return animations;
    }

    public List<Cutscene> getCutscenes() {
        return cutscenes;
    }

    public List<Subscene> getSubscenes() {
        return subscenes;
    }

    public List<CameraAngle> getCameraAngles() {
        return cameraAngles;
    }

    public List<CharacterStory> getNpcs() {

        return npcs;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
