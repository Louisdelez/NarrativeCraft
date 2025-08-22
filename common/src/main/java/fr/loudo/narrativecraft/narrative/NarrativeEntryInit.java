package fr.loudo.narrativecraft.narrative;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.serialization.SubsceneSerializer;
import net.minecraft.client.resources.PlayerSkin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

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
            initSubscenes(scene);
            chapter.addScene(scene);
        }
    }

    private static void initSubscenes(Scene scene) throws IOException {
        File subsceneFile = NarrativeCraftFile.getSubsceneFile(scene);
        String content = Files.readString(subsceneFile.toPath());
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Subscene.class, new SubsceneSerializer(scene))
                .create();

        Type type = new TypeToken<List<Subscene>>() {}.getType();
        List<Subscene> subscenes = gson.fromJson(content, type);
        if(subscenes == null) return;
        scene.getSubscenes().addAll(subscenes);
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
        if(NarrativeCraftMod.firstTime) {
            CharacterStory steve = new CharacterStory("Steve", "Steve from Minecraft.", "17", "05", "2009", PlayerSkin.Model.WIDE, CharacterType.MAIN);
            CharacterStory alex = new CharacterStory("Alex", "Alex from Minecraft.", "22", "08", "2014", PlayerSkin.Model.SLIM, CharacterType.MAIN);
            characterManager.addCharacter(steve);
            characterManager.addCharacter(alex);
            if(new Random().nextInt(0, 500) >= 445) {
                CharacterStory herobrine = new CharacterStory("Herobrine", "You can't escape me. §kYour story is now mine...", "00", "00", "9999", PlayerSkin.Model.WIDE, CharacterType.MAIN);
                characterManager.addCharacter(herobrine);
            }
        }
    }

}
