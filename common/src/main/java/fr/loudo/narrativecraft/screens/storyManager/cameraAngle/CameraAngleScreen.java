package fr.loudo.narrativecraft.screens.storyManager.cameraAngle;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesMenuScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CameraAngleScreen extends StoryElementScreen {

    private final Scene scene;

    public CameraAngleScreen(Scene scene) {
        super(Translation.message("screen.story_manager.camera_angle_list", scene.getName()));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<CameraAngleGroup> screen = new EditInfoScreen<>(this, null, new EditScreenCameraAngleAdapter(scene));
            this.minecraft.setScreen(screen);
        });
        initFolderButton();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new ScenesMenuScreen(scene));
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = scene.getCameraAngleGroups().stream()
                .map(cameraAngleGroup -> {
                    Button button = Button.builder(Component.literal(cameraAngleGroup.getName()), button1 -> {}).build();
                    button.active = false;

                    return new StoryElementList.StoryEntryData(button, () -> {
                        minecraft.setScreen(new EditInfoScreen<>(this, cameraAngleGroup, new EditScreenCameraAngleAdapter(scene)));
                    }, () -> {
                        minecraft.setScreen(new CameraAngleScreen(scene));
                        try {
                            scene.removeCameraAngleGroup(cameraAngleGroup);
                            NarrativeCraftFile.updateCameraAngleGroup(scene);
                            minecraft.setScreen(new CameraAngleScreen(scene));
                        } catch (Exception e) {
                            scene.addCameraAngleGroup(cameraAngleGroup);
                            fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                        }
                    });
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    @Override
    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.getSceneFolder(scene).toPath());
    }
}
