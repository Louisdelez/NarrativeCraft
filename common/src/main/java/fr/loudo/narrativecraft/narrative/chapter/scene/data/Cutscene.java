package fr.loudo.narrativecraft.narrative.chapter.scene.data;

import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;

import java.util.ArrayList;
import java.util.List;

public class Cutscene extends SceneData {

    private final List<Subscene> subscenes = new ArrayList<>();
    private final List<Animation> animations = new ArrayList<>(); // For individual animations.

    public Cutscene(String name, String description, Scene scene) {
        super(name, description, scene);
    }

    public List<String> getSubscenesName() {
        return subscenes.stream().map(Subscene::getName).toList();
    }

    public List<Subscene> getSubscenes() {
        return subscenes;
    }

    public List<String> getAnimationsName() {
        return animations.stream().map(Animation::getName).toList();
    }

    public List<Animation> getAnimations() {
        return animations;
    }
}
