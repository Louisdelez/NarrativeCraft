package fr.loudo.narrativecraft.screens.storyManager.chapter;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EditScreenChapterAdapter implements EditScreenAdapter<Chapter> {
    @Override
    public void initExtraFields(EditInfoScreen<Chapter> screen, Chapter entry) {}

    @Override
    public void renderExtraFields(EditInfoScreen<Chapter> screen, Chapter entry, int startY, int centerX) {}

    @Override
    public void buildFromScreen(Map<String, Object> extraFields, Minecraft minecraft, @Nullable Chapter existing, String name, String description) {

        if(existing == null) {
            try {
                ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
                Chapter chapter = new Chapter(name, description, chapterManager.getChapters().size() + 1);
                chapterManager.addChapter(chapter);
                NarrativeCraftFile.createChapterDirectory(chapter);
                minecraft.setScreen(new ChaptersScreen());
            } catch (Exception e) {
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        } else {
            Chapter newChapter = new Chapter(name, description, existing.getIndex());
            Chapter oldChapter = new Chapter(existing.getName(), existing.getDescription(), existing.getIndex());
            try {
                NarrativeCraftFile.updateChapterData(newChapter);

                existing.setName(name);
                existing.setDescription(description);
                NarrativeCraftFile.updateInkIncludes();
                minecraft.setScreen(new ChaptersScreen());
            } catch (Exception e) {
                existing.setName(oldChapter.getName());
                existing.setDescription(oldChapter.getDescription());
                Util.sendCrashMessage(minecraft.player, e);
                minecraft.setScreen(null);
            }
        }

    }
}
