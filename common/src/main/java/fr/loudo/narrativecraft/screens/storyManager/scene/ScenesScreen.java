package fr.loudo.narrativecraft.screens.storyManager.scene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.chapter.ChaptersScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ScenesScreen extends StoryElementScreen {

    private final Chapter chapter;

    public ScenesScreen(Chapter chapter) {
        super(Translation.message("screen.story_manager.scene_list", chapter.getIndex()));
        this.chapter = chapter;
    }

    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<Scene> screen = new EditInfoScreen<>(this, null, new EditScreenSceneAdapter(chapter));
            this.minecraft.setScreen(screen);
        });
        initFolderButton();
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, p_345997_ -> this.onClose()).width(200).build());
    }

    @Override
    public void onClose() {
        ChaptersScreen screen = new ChaptersScreen();
        this.minecraft.setScreen(screen);
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = chapter.getSortedSceneList().stream()
                .map(scene -> {
                    Button button = Button.builder(Component.literal(scene.getName()), b -> {
                        this.minecraft.setScreen(new ScenesMenuScreen(scene));
                    }).build();
                    return new StoryElementList.StoryEntryData(button, () -> {
                        minecraft.setScreen(new EditInfoScreen<>(this, scene, new EditScreenSceneAdapter(chapter)));
                    }, () -> {
                        try {
                             chapter.removeScene(scene);
                             NarrativeCraftFile.deleteSceneDirectory(scene);
                             if(scene.getRank() == 1 && chapter.getSortedSceneList().size() > 1) {
                                 NarrativeCraftFile.updateMasterSceneKnot(chapter.getSortedSceneList().getFirst());
                             }
                             minecraft.setScreen(new ScenesScreen(chapter));
                        } catch (Exception e) {
                            chapter.addScene(scene);
                            chapter.setSceneRank(scene, scene.getRank());
                            fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                            minecraft.setScreen(null);
                        }
                    });
                }).toList();
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    @Override
    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.getScenesFolder(chapter).toPath());
    }

}