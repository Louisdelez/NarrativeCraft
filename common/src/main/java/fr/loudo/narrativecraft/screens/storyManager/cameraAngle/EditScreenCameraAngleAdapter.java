package fr.loudo.narrativecraft.screens.storyManager.cameraAngle;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EditScreenCameraAngleAdapter implements EditScreenAdapter<CameraAngleGroup> {

    private final Scene scene;

    public EditScreenCameraAngleAdapter(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void initExtraFields(EditInfoScreen<CameraAngleGroup> screen, CameraAngleGroup entry) {}

    @Override
    public void renderExtraFields(EditInfoScreen<CameraAngleGroup> screen, CameraAngleGroup entry, int x, int y) {}

    @Override
    public void buildFromScreen(Map<String, Object> extraFields, Minecraft minecraft, @Nullable CameraAngleGroup existing, String name, String description) {
        if(existing == null) {
            if(scene.cameraAngleExists(name)) {
                ScreenUtils.sendToast(
                        Translation.message("global.error"),
                        Translation.message("camera_angle.already_exists", name, scene.getName())
                );
                return;
            }
            CameraAngleGroup cameraAngleGroup = new CameraAngleGroup(name, description, scene);
            try {
                scene.addCameraAngleGroup(cameraAngleGroup);
                NarrativeCraftFile.updateCameraAngleGroup(scene);
                minecraft.setScreen(new CameraAngleScreen(scene));
            } catch (Exception e) {
                scene.removeCameraAngleGroup(cameraAngleGroup);
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        } else {
            CameraAngleGroup oldCameraAngleGroup = new CameraAngleGroup(existing.getName(), existing.getDescription(), scene);
            try {
                existing.setName(name);
                existing.setDescription(description);
                NarrativeCraftFile.updateCameraAngleGroup(scene);
                minecraft.setScreen(new CameraAngleScreen(scene));
            } catch (Exception e) {
                existing.setName(oldCameraAngleGroup.getName());
                existing.setDescription(oldCameraAngleGroup.getDescription());
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        }
    }
}
