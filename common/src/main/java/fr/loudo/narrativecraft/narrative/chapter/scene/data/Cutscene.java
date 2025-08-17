package fr.loudo.narrativecraft.narrative.chapter.scene.data;

import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;

import java.util.ArrayList;
import java.util.List;

public class Cutscene extends SceneData {

    private transient List<Subscene> subscenes = new ArrayList<>();
    private transient List<Animation> animations = new ArrayList<>(); // For individual animations.

    public Cutscene(String name, String description, Scene scene) {
        super(name, description, scene);
    }

    public List<String> getSubscenesName() {
        return subscenes.stream().map(Subscene::getName).toList();
    }

    public List<Subscene> getSubscenes() {
        if (subscenes == null) {
            subscenes = new ArrayList<>();
        }
        return subscenes;
    }

    public List<String> getAnimationsName() {
        return animations.stream().map(Animation::getName).toList();
    }

    public List<Animation> getAnimations() {
        if (animations == null) {
            animations = new ArrayList<>();
        }
        return animations;
    }
}
