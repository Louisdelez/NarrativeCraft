package fr.loudo.narrativecraft.screens.storyManager.chapter;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesScreen;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ChaptersScreen extends StoryElementScreen {

    public ChaptersScreen() {
        super(Translation.message("screen.story_manager.chapter_list"));
    }

    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<Chapter> screen = new EditInfoScreen<>(this, null, new EditScreenChapterAdapter());
            this.minecraft.setScreen(screen);
        });
        initFolderButton();
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (p_345997_) -> this.onClose()).width(200).build());
    }

    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.chaptersDirectory.toPath());
    }

    @Override
    protected void addContents() {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();

        List<StoryElementList.StoryEntryData> entries = chapterManager.getChapters().stream()
                .map(chapter -> {
                    String label = String.valueOf(chapter.getIndex());
                    if (!chapter.getName().isEmpty()) {
                        label += " - " + chapter.getName();
                    }

                    Button button = Button.builder(Component.literal(label), b -> {
                        this.minecraft.setScreen(new ScenesScreen(chapter));
                    }).build();

                    return new StoryElementList.StoryEntryData(button, () -> {
                       minecraft.setScreen(new EditInfoScreen<>(this, chapter, new EditScreenChapterAdapter()));
                    }, () -> {
                        try {
                            chapterManager.removeChapter(chapter);
                            NarrativeCraftFile.deleteChapterDirectory(chapter);
                            minecraft.setScreen(new ChaptersScreen());
                        } catch (Exception e) {
                            chapterManager.addChapter(chapter);
                            fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                        }
                    });
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

}