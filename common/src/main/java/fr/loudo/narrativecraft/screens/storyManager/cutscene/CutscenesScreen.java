package fr.loudo.narrativecraft.screens.storyManager.cutscene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesMenuScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CutscenesScreen extends StoryElementScreen {

    private final Scene scene;

    public CutscenesScreen(Scene scene) {
        super(Translation.message("screen.story_manager.cutscene_list", scene.getName()));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<Cutscene> screen = new EditInfoScreen<>(this, null, new EditScreenCutsceneAdapter(scene));
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
        List<StoryElementList.StoryEntryData> entries = scene.getCutscenes().stream()
                .map(cutscene -> {
                    Button button = Button.builder(Component.literal(cutscene.getName()), button1 -> {
                        //TODO: enter cutscene controller
                    }).build();

                    return new StoryElementList.StoryEntryData(button, () -> {
                        minecraft.setScreen(new EditInfoScreen<>(this, cutscene, new EditScreenCutsceneAdapter(scene)));
                    }, () -> {
                        minecraft.setScreen(new CutscenesScreen(scene));
                        try {
                            scene.removeCutscene(cutscene);
                            NarrativeCraftFile.updateCutsceneFile(scene);
                            minecraft.setScreen(new CutscenesScreen(scene));
                        } catch (Exception e) {
                            scene.addCutscene(cutscene);
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
