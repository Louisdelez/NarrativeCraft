package fr.loudo.narrativecraft.screens.storyManager.subscene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesMenuScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SubscenesScreen extends StoryElementScreen {

    private final Scene scene;

    public SubscenesScreen(Scene scene) {
        super(Translation.message("screen.story_manager.subscene_list", scene.getName()));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<Subscene> screen = new EditInfoScreen<>(this, null, new EditScreenSubsceneAdapter(scene));
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
        List<StoryElementList.StoryEntryData> entries = scene.getSubscenes().stream()
                .map(subscene -> {
                    Button button = Button.builder(Component.literal(subscene.getName()), button1 -> {}).build();
                    button.active = false;

                    return new StoryElementList.StoryEntryData(button, () -> {
                        minecraft.setScreen(new EditInfoScreen<>(this, subscene, new EditScreenSubsceneAdapter(scene)));
                    }, () -> {
                        minecraft.setScreen(new SubscenesScreen(scene));
                        try {
                            scene.removeSubscene(subscene);
                            NarrativeCraftFile.updateSubsceneFile(scene);
                            minecraft.setScreen(new SubscenesScreen(scene));
                        } catch (Exception e) {
                            scene.addSubscene(subscene);
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
