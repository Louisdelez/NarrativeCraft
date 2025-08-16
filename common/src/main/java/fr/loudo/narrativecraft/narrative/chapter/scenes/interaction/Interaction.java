package fr.loudo.narrativecraft.narrative.chapter.scenes.interaction;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.screens.storyManager.scenes.interactions.InteractionScreen;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public abstract class Interaction extends NarrativeEntry {

    protected transient Scene scene;

    public Interaction(String name, String description, Scene scene) {
        super(name, description);
        this.scene = scene;
    }

    public abstract InteractionType getType();

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void remove() {
        scene.removeInteraction(this);
        NarrativeCraftFile.updateInteractionsFile(scene);
    }

    @Override
    public void update(String name, String description) {
        String oldName = this.name;
        String oldDescription = this.description;
        this.name = name;
        this.description = description;
        if(!NarrativeCraftFile.updateInteractionsFile(scene)) {
            this.name = oldName;
            this.description = oldDescription;
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.interaction_manager.update.failed", name));
            return;
        }
        ScreenUtils.sendToast(Translation.message("global.info"), Translation.message("toast.description.updated"));
        Minecraft.getInstance().setScreen(reloadScreen());
    }

    @Override
    public Screen reloadScreen() {
        return new InteractionScreen(scene);
    }
}
