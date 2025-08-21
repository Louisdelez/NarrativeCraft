package fr.loudo.narrativecraft.narrative;

import com.google.gson.Gson;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;

import java.io.File;
import java.nio.file.Files;

public class NarrativeEntryInit {

    public static boolean hasError;

    public static void init() {
        hasError = false;
        NarrativeCraftMod.getInstance().clearManagers();
        try {
            initCharacters();
            initChapters();
            NarrativeCraftFile.updateInkIncludes();
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Couldn't init story data", e);
            hasError = true;
        }
    }

    private static void initChapters() throws Exception {
        File chaptersFolder = NarrativeCraftFile.chaptersDirectory;
        if (chaptersFolder == null || !chaptersFolder.exists() || !chaptersFolder.isDirectory()) {
            NarrativeCraftMod.LOGGER.warn("Chapter directory is null, doesn't exist, or is not a directory");
            return;
        }
        File[] chaptersSubFolder = chaptersFolder.listFiles();
        if(chaptersSubFolder == null) return;
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        Gson gson = new Gson();
        for(File chapterFolder : chaptersSubFolder) {
            String dataContent = Files.readString(NarrativeCraftFile.getDataFile(chapterFolder).toPath());
            Chapter chapterData = gson.fromJson(dataContent, Chapter.class);
            int index = Integer.parseInt(chapterFolder.getName());
            Chapter chapter = new Chapter(chapterData.getName(), chapterData.getDescription(), index);
            chapterManager.addChapter(chapter);
            initScenesOfChapter(chapter);
        }
    }

    private static void initScenesOfChapter(Chapter chapter) throws Exception {
        File scenesDirectory = NarrativeCraftFile.getScenesFolder(chapter);
        File[] scenesFolder = scenesDirectory.listFiles();
        if(scenesFolder == null) return;
        Gson gson = new Gson();
        for(File sceneFolder : scenesFolder) {
            File dataFile = NarrativeCraftFile.getDataFileFromSceneFolder(sceneFolder);
            String dataContent = Files.readString(dataFile.toPath());
            Scene sceneData = gson.fromJson(dataContent, Scene.class);
            if(sceneData.getRank() <= 0) {
                throw new Exception(String.format("Scene %s from chapter %s rank is equal to or less than 0.", sceneData.name, chapter.getIndex()));
            }
            Scene scene = new Scene(sceneData.getName(), sceneData.getDescription(), chapter);
            scene.setRank(sceneData.getRank());
            chapter.addScene(scene);
        }
    }

    private static void initCharacters() throws Exception {
        File[] charactersFolder = NarrativeCraftFile.characterDirectory.listFiles();
        if(charactersFolder == null) return;
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        Gson gson = new Gson();
        for(File characterFolder : charactersFolder) {
            File dataFile = NarrativeCraftFile.getDataFile(characterFolder);
            String dataContent = Files.readString(dataFile.toPath());
            CharacterStory characterStory = gson.fromJson(dataContent, CharacterStory.class);
            //TODO: init skins of character
            if(characterStory == null) {
                throw new Exception(String.format("Character %s couldn't be initialized", characterFolder.getName()));
            }
            characterManager.addCharacter(characterStory);
        }
    }

}
