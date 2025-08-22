package fr.loudo.narrativecraft.screens.storyManager.scene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.subscene.SubscenesScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

import java.util.List;

public class ScenesMenuScreen extends StoryElementScreen {

    private final Scene scene;

    public ScenesMenuScreen(Scene scene) {
        super(Translation.message("screen.story_manager.scene_menu", scene.getName()));
        this.scene = scene;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new ScenesScreen(scene.getChapter()));
    }

    protected void addTitle() {
        super.addTitle();
        initFolderButton();
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, p_345997_ -> this.onClose()).width(200).build());
    }

    @Override
    protected void addContents() {
        StoryElementList.StoryEntryData animation = new StoryElementList.StoryEntryData(
            Button.builder(Translation.message("global.animation"), button -> {

            }).build()
        );
        StoryElementList.StoryEntryData cutscene = new StoryElementList.StoryEntryData(
                Button.builder(Translation.message("global.cutscene"), button -> {

                }).build()
        );
        StoryElementList.StoryEntryData interaction = new StoryElementList.StoryEntryData(
                Button.builder(Translation.message("global.interaction"), button -> {

                }).build()
        );
        StoryElementList.StoryEntryData npc = new StoryElementList.StoryEntryData(
                Button.builder(Translation.message("global.npc"), button -> {

                }).build()
        );
        StoryElementList.StoryEntryData subscene = new StoryElementList.StoryEntryData(
                Button.builder(Translation.message("global.subscene"), button -> {
                    minecraft.setScreen(new SubscenesScreen(scene));
                }).build()
        );
        List<StoryElementList.StoryEntryData> entries = List.of(animation, cutscene, interaction, npc, subscene);
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    @Override
    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.getSceneFolder(scene).toPath());
    }

}
