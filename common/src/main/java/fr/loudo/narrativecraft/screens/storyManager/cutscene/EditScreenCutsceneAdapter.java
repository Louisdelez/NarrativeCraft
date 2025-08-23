package fr.loudo.narrativecraft.screens.storyManager.cutscene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EditScreenCutsceneAdapter implements EditScreenAdapter<Cutscene> {

    private final Scene scene;

    public EditScreenCutsceneAdapter(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void initExtraFields(EditInfoScreen<Cutscene> screen, Cutscene entry) {}

    @Override
    public void renderExtraFields(EditInfoScreen<Cutscene> screen, Cutscene entry, int x, int y) {}

    @Override
    public void buildFromScreen(Map<String, Object> extraFields, Minecraft minecraft, @Nullable Cutscene existing, String name, String description) {
        if(existing == null) {
            if(scene.cutsceneExists(name)) {
                ScreenUtils.sendToast(
                        Translation.message("global.error"),
                        Translation.message("cutscene.already_exists", name, scene.getName())
                );
                return;
            }
            Cutscene cutscene = new Cutscene(name, description, scene);
            try {
                scene.addCutscene(cutscene);
                NarrativeCraftFile.updateCutsceneFile(scene);
                minecraft.setScreen(new CutscenesScreen(scene));
            } catch (Exception e) {
                scene.removeCutscene(cutscene);
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        } else {
            Cutscene oldCutscene = new Cutscene(existing.getName(), existing.getDescription(), scene);
            try {
                existing.setName(name);
                existing.setDescription(description);
                NarrativeCraftFile.updateCutsceneFile(scene);
                minecraft.setScreen(new CutscenesScreen(scene));
            } catch (Exception e) {
                existing.setName(oldCutscene.getName());
                existing.setDescription(oldCutscene.getDescription());
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        }
    }
}
