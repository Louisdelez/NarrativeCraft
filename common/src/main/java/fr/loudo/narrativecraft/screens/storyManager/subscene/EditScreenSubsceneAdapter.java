package fr.loudo.narrativecraft.screens.storyManager.subscene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EditScreenSubsceneAdapter implements EditScreenAdapter<Subscene> {

    private final Scene scene;

    public EditScreenSubsceneAdapter(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void initExtraFields(EditInfoScreen<Subscene> screen, Subscene entry) {}

    @Override
    public void renderExtraFields(EditInfoScreen<Subscene> screen, Subscene entry, int x, int y) {}

    @Override
    public void buildFromScreen(Map<String, Object> extraFields, Minecraft minecraft, @Nullable Subscene existing, String name, String description) {
        if(existing == null) {
            Subscene subscene = new Subscene(name, description, scene);
            try {
                scene.addSubscene(subscene);
                NarrativeCraftFile.updateSubsceneFile(scene);
                minecraft.setScreen(new SubscenesScreen(scene));
            } catch (Exception e) {
                scene.removeSubscene(subscene);
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        }
    }
}
