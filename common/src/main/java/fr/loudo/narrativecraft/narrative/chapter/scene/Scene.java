package fr.loudo.narrativecraft.narrative.chapter.scene;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class Scene extends NarrativeEntry {

    private final Chapter chapter;

    private final List<Animation> animations = new ArrayList<>();
    private final List<Cutscene> cutscenes = new ArrayList<>();
    private final List<Subscene> subscenes = new ArrayList<>();
    private final List<CameraAngleGroup> cameraAngleGroups = new ArrayList<>();

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
        for(Animation animation : animations) {
            if(animation.getName().equalsIgnoreCase(name)) {
                return animation;
            }
        }
        return null;
    }

    public Subscene getSubsceneByName(String name) {
        for(Subscene subscene : subscenes) {
            if(subscene.getName().equalsIgnoreCase(name)) {
                return subscene;
            }
        }
        return null;
    }

    public CharacterStory getNpcByName(String name) {
        for(CharacterStory characterStory : npcs) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return characterStory;
            }
        }
        return null;
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

    public List<CameraAngleGroup> getCameraAngleGroups() {
        return cameraAngleGroups;
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
