package fr.loudo.narrativecraft.narrative.chapter.scene.data;

import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;

import java.util.ArrayList;
import java.util.List;

public class Subscene extends SceneData {

    private transient List<Animation> animations = new ArrayList<>();

    public Subscene(String name, String description, Scene scene) {
        super(name, description, scene);
    }

    public List<Animation> getAnimations() {
        if (animations == null) {
            animations = new ArrayList<>();
        }
        return animations;
    }

    public List<String> getAnimationsName() {
        return getAnimations().stream().map(Animation::getName).toList();
    }
}
