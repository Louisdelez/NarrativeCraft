package fr.loudo.narrativecraft.narrative.chapter.scenes.interaction;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import net.minecraft.client.gui.screens.Screen;

public abstract class Interaction extends NarrativeEntry {

    protected final Scene scene;

    public Interaction(String name, String description, Scene scene) {
        super(name, description);
        this.scene = scene;
    }

    public abstract InteractionType getType();

    public Scene getScene() {
        return scene;
    }

    @Override
    public void remove() {
        scene.removeInteraction(this);
    }

    @Override
    public void update(String name, String description) {

    }

    @Override
    public Screen reloadScreen() {
        return null;
    }
}
