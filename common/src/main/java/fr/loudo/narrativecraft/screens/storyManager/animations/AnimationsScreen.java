package fr.loudo.narrativecraft.screens.storyManager.animations;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesMenuScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AnimationsScreen extends StoryElementScreen {

    private final Scene scene;

    public AnimationsScreen(Scene scene) {
        super(Translation.message("screen.story_manager.animation_list", scene.getName()));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initFolderButton();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new ScenesMenuScreen(scene));
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = scene.getAnimations().stream()
                .map(animation -> {
                    Button button = Button.builder(Component.literal(animation.getName()), button1 -> {}).build();
                    button.active = false;

                    return new StoryElementList.StoryEntryData(button, () -> {
                        minecraft.setScreen(new EditInfoScreen<>(this, animation, new EditScreenAnimationAdapter(scene)));
                    }, () -> {
                        minecraft.setScreen(new AnimationsScreen(scene));
                        scene.removeAnimation(animation);
                        NarrativeCraftFile.deleteAnimationFile(animation);
                        minecraft.setScreen(new AnimationsScreen(scene));
                    });
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    @Override
    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.getAnimationsFolder(scene).toPath());
    }
}
