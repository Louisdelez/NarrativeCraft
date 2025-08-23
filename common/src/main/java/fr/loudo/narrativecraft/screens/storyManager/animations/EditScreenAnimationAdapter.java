package fr.loudo.narrativecraft.screens.storyManager.animations;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EditScreenAnimationAdapter implements EditScreenAdapter<Animation> {

    private final Scene scene;

    public EditScreenAnimationAdapter(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void initExtraFields(EditInfoScreen<Animation> screen, Animation entry) {}

    @Override
    public void renderExtraFields(EditInfoScreen<Animation> screen, Animation entry, int x, int y) {}

    @Override
    public void buildFromScreen(Map<String, Object> extraFields, Minecraft minecraft, @Nullable Animation existing, String name, String description) {
        if(existing == null) return;
        Animation oldAnimation = new Animation(existing.getName(), scene);
        try {
            existing.setName(name);
            existing.setDescription(description);
            NarrativeCraftFile.updateAnimationFile(oldAnimation, existing);
            minecraft.setScreen(new AnimationsScreen(scene));
        } catch (Exception e) {
            existing.setName(oldAnimation.getName());
            existing.setDescription(oldAnimation.getDescription());
            Util.sendCrashMessage(minecraft.player, e);
            minecraft.setScreen(null);
        }
    }
}
