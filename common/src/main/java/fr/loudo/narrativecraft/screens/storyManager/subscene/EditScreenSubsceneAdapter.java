package fr.loudo.narrativecraft.screens.storyManager.subscene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
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
            if(scene.subsceneExists(name)) {
                ScreenUtils.sendToast(
                        Translation.message("global.error"),
                        Translation.message("subscene.already_exists", name, scene.getName())
                );
                return;
            }
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
        } else {
            Subscene oldSubscene = new Subscene(existing.getName(), existing.getDescription(), scene);
            try {
                existing.setName(name);
                existing.setDescription(description);
                NarrativeCraftFile.updateSubsceneFile(scene);
                minecraft.setScreen(new SubscenesScreen(scene));
            } catch (Exception e) {
                existing.setName(oldSubscene.getName());
                existing.setDescription(oldSubscene.getDescription());
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        }
    }
}
