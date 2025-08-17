package fr.loudo.narrativecraft.narrative.chapter;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;

import java.util.ArrayList;
import java.util.List;

public class Chapter extends NarrativeEntry {

    private int index;
    private final List<Scene> scenes = new ArrayList<>();

    public Chapter(String name, String description, int index) {
        super(name, description);
        this.index = index;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
