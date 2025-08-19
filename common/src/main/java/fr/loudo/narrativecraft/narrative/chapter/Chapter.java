package fr.loudo.narrativecraft.narrative.chapter;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Chapter extends NarrativeEntry {

    private int index;
    private final List<Scene> scenes = new ArrayList<>();

    public Chapter(String name, String description, int index) {
        super(name, description);
        this.index = index;
    }

    public String knotName() {
        return "chapter_" + index;
    }

    public void addScene(Scene scene) {
        if(scenes.contains(scene)) return;
        scenes.add(scene);
    }

    public void removeScene(Scene scene) {
        scenes.remove(scene);
        sortScenesByRank();
    }

    public Scene getSceneByName(String name) {
        for(Scene scene : scenes) {
            if(scene.getName().equalsIgnoreCase(name)) {
                return scene;
            }
        }
        return null;
    }

    public boolean sceneExists(String name) {
        for(Scene scene : scenes) {
            if(scene.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public void setSceneRank(Scene scene, int newRank) {
        int oldRank = scene.getRank();
        if (newRank == oldRank) {
            return;
        }

        if (newRank < oldRank) {
            for (Scene s : scenes) {
                int r = s.getRank();
                if (r >= newRank && r < oldRank) {
                    s.setRank(r + 1);
                }
            }
        } else {
            for (Scene s : scenes) {
                int r = s.getRank();
                if (r <= newRank && r > oldRank) {
                    s.setRank(r - 1);
                }
            }
        }

        scene.setRank(newRank);
    }

    public List<Scene> getSortedSceneList() {
        return scenes.stream()
                .sorted(Comparator.comparingInt(Scene::getRank))
                .toList();
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

    private void sortScenesByRank() {
        List<Scene> sortedScenes = getSortedSceneList();
        for(int i = 0; i < sortedScenes.size(); i++) {
            sortedScenes.get(i).setRank(i + 1);
        }
    }

}
