package fr.loudo.narrativecraft.files;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.serialization.CutsceneSerializer;
import fr.loudo.narrativecraft.serialization.SubsceneSerializer;
import fr.loudo.narrativecraft.util.InkUtil;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;

public class NarrativeCraftFile {
    public static final String EXTENSION_SCRIPT_FILE = ".ink";
    public static final String EXTENSION_DATA_FILE = ".json";

    private static final String DIRECTORY_NAME = NarrativeCraftMod.MOD_ID;

    // GLOBAL
    public static final String DATA_FILE_NAME = "data" + EXTENSION_DATA_FILE;

    // ROOT
    private static final String BUILD_DIRECTORY_NAME = "build";
    private static final String CHAPTERS_DIRECTORY_NAME = "chapters";
    private static final String CHARACTERS_DIRECTORY_NAME = "characters";
    private static final String SAVES_DIRECTORY_NAME = "saves";
    private static final String MAIN_INK_NAME = "main" + EXTENSION_SCRIPT_FILE;
    private static final String VARS_INK_NAME = "vars" + EXTENSION_SCRIPT_FILE;
    private static final String FUNCTIONS_INK_NAME = "funcs" + EXTENSION_SCRIPT_FILE;

    // CHAPTER
    private static final String SCENES_DIRECTORY_NAME = "scenes";

    // SCENE
    public static final String ANIMATIONS_FOLDER_NAME = "animations";
    public static final String NPC_FOLDER_NAME = "npc";
    public static final String DATA_FOLDER_NAME = "data";
    public static final String SKINS_FOLDER_NAME = "skins";

    public static final String CAMERA_ANGLES_FILE_NAME = "camera_angles" + EXTENSION_DATA_FILE;
    public static final String CUTSCENES_FILE_NAME = "cutscenes" + EXTENSION_DATA_FILE;
    public static final String DETAILS_FILE_NAME = "details" + EXTENSION_DATA_FILE;
    public static final String SUBSCENES_FILE_NAME = "subscenes" + EXTENSION_DATA_FILE;

    // DATA
    public static final String DIALOG_FILE_NAME = "dialog" + EXTENSION_DATA_FILE;

    // SAVE
    public static final String SAVE_FILE_NAME = "save" + EXTENSION_DATA_FILE;

    // STORY
    public static final String STORY_FILE_NAME = "story" + EXTENSION_DATA_FILE;

    // OPTIONS
    public static final String USER_OPTIONS_FILE_NAME = "user_options" + EXTENSION_DATA_FILE;
    public static final String WORLD_OPTIONS_FILE_NAME = "world_options" + EXTENSION_DATA_FILE;

    // RESOURCES
    public static final String MAIN_SCREEN_BACKGROUND_FILE_NAME = "main_screen_background" + EXTENSION_DATA_FILE;

    public static File mainDirectory;
    public static File rootDirectory;
    public static File chaptersDirectory;
    public static File characterDirectory;
    public static File savesDirectory;
    public static File buildDirectory;
    public static File dataDirectory;
    public static File mainInkFile;
    public static File varsInkFile;
    public static File functionsInkFile;

    public static void init(MinecraftServer server) {
        NarrativeCraftMod.firstTime = !new File(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME).exists();
        NarrativeCraftFile.mainDirectory = createDirectory(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME);
        rootDirectory = createDirectory(Minecraft.getInstance().gameDirectory, DIRECTORY_NAME);
        chaptersDirectory = createDirectory(NarrativeCraftFile.mainDirectory, CHAPTERS_DIRECTORY_NAME);
        characterDirectory = createDirectory(NarrativeCraftFile.mainDirectory, CHARACTERS_DIRECTORY_NAME);
        savesDirectory = createDirectory(NarrativeCraftFile.mainDirectory, SAVES_DIRECTORY_NAME);
        buildDirectory = createDirectory(NarrativeCraftFile.mainDirectory, BUILD_DIRECTORY_NAME);
        dataDirectory = createDirectory(NarrativeCraftFile.mainDirectory, DATA_FOLDER_NAME);
        mainInkFile = createFile(NarrativeCraftFile.mainDirectory, MAIN_INK_NAME);
        varsInkFile = createFile(NarrativeCraftFile.mainDirectory, VARS_INK_NAME);
        try(Writer writer = new BufferedWriter(new FileWriter(varsInkFile))) {
            writer.write("// " + Translation.message("file.vars_placeholder").append("\n").append("\n").getString());
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.warn("Couldn't write on vars ink file", e);
        }
        functionsInkFile = createFile(NarrativeCraftFile.mainDirectory, FUNCTIONS_INK_NAME);
        try(Writer writer = new BufferedWriter(new FileWriter(functionsInkFile))) {
            writer.write("// " + Translation.message("file.funcs_placeholder").append("\n").append("\n").getString());
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.warn("Couldn't write on functions ink file", e);
        }
    }


    private static File createDirectory(File parent, String name) {
        File directory = new File(parent, name);
        if (!directory.exists()) {
            if (!directory.mkdir()) NarrativeCraftMod.LOGGER.error("Couldn't create directory {}!", name);
        }
        return directory;
    }

    private static File createFile(File parent, String name) {
        File file = new File(parent, name);
        if (!file.exists()) {
            try {
                if (!file.createNewFile())
                    NarrativeCraftMod.LOGGER.error("Couldn't create file {}!", file.getAbsolutePath());
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Couldn't create file {}! Cause: {}", file.getAbsolutePath(), e);
            }
        }
        return file;
    }

    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    public static void createChapterDirectory(Chapter chapter) throws IOException {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
        createDirectory(chapterFolder.getAbsoluteFile(), SCENES_DIRECTORY_NAME);
        File chapterInkFile = createFile(chapterFolder, "chapter_" + chapter.getIndex() + EXTENSION_SCRIPT_FILE);
        File dataFile = getDataFile(chapterFolder);

        String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", chapter.getName(), chapter.getDescription());
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            writer.write(content);
        }
        try (Writer writer = new BufferedWriter(new FileWriter(chapterInkFile))) {
            writer.write("=== " + chapter.knotName() + " ===");
        }
        updateInkIncludes();
    }

    public static void updateChapterData(Chapter chapter) throws IOException {
        File dataFile = getDataFile(chapter);
        String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", chapter.getName(), chapter.getDescription());
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            writer.write(content);
        }
        updateInkIncludes();
    }

    public static void deleteChapterDirectory(Chapter chapter) throws IOException {
        File chapterFolder = getChapterDirectory(chapter);
        deleteDirectory(chapterFolder);
        updateInkIncludes();
    }

    public static void createSceneFolder(Scene scene) throws IOException {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = createDirectory(chapterFolder.getAbsoluteFile(), SCENES_DIRECTORY_NAME);
        File sceneFolder = createDirectory(scenesFolder, Util.snakeCase(scene.getName()));

        File dataFolder = createDirectory(sceneFolder, DATA_FOLDER_NAME);

        File dataFile = getDataFile(dataFolder);
        File sceneInkFile = createFile(sceneFolder, Util.snakeCase(scene.getName()) + EXTENSION_SCRIPT_FILE);

        createDirectory(dataFolder, NPC_FOLDER_NAME);
        createDirectory(dataFolder, ANIMATIONS_FOLDER_NAME);
        createFile(dataFolder, CUTSCENES_FILE_NAME);
        createFile(dataFolder, SUBSCENES_FILE_NAME);
        createFile(dataFolder, CAMERA_ANGLES_FILE_NAME);

        String content = String.format("{\"name\":\"%s\",\"description\":\"%s\",\"rank\":%s}", scene.getName(), scene.getDescription(), scene.getRank());
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            writer.write(content);
        }
        try (Writer writer = new BufferedWriter(new FileWriter(sceneInkFile))) {
            writer.write(
                    "=== " + scene.knotName() + " ===" + "\n" +
                    "# on enter"
            );
        }
        updateMasterSceneKnot(scene);
    }

    public static void updateMasterSceneKnot(Scene scene) throws IOException {
        if (scene.getRank() > 1) return;

        File chapterInkFile = getInkFile(scene.getChapter());
        String originalContent = Files.readString(chapterInkFile.toPath());

        Matcher matcher = InkUtil.SCENE_KNOT_PATTERN.matcher(originalContent);

        String updatedContent;
        if (matcher.find()) {
            updatedContent = matcher.replaceFirst(scene.knotName());
        } else {
            updatedContent = originalContent + "\n-> " + scene.knotName();
        }

        try (Writer writer = new BufferedWriter(new FileWriter(chapterInkFile))) {
            writer.write(updatedContent);
        }
    }

    public static void updateSceneData(Scene oldScene, Scene newScene) throws IOException {
        File sceneFolder = getSceneFolder(oldScene);

        File newSceneFolder = new File(sceneFolder.getParent(), Util.snakeCase(newScene.getName()));

        if (!oldScene.getName().equalsIgnoreCase(newScene.getName())) {
            Files.move(sceneFolder.toPath(), newSceneFolder.toPath());
            sceneFolder = newSceneFolder;
        }

        File sceneData = getDataFile(newScene);
        String content = String.format(
                "{\"name\":\"%s\",\"description\":\"%s\",\"rank\":%s}",
                newScene.getName(),
                newScene.getDescription(),
                newScene.getRank()
        );

        try (Writer writer = new BufferedWriter(new FileWriter(sceneData))) {
            writer.write(content);
        }

        File oldScriptFile = new File(sceneFolder, Util.snakeCase(oldScene.getName()) + EXTENSION_SCRIPT_FILE);
        File newScriptFile = new File(sceneFolder, Util.snakeCase(newScene.getName()) + EXTENSION_SCRIPT_FILE);

        Files.move(oldScriptFile.toPath(), newScriptFile.toPath());

        String inkContent = Files.readString(newScriptFile.toPath());
        inkContent = inkContent.replace(oldScene.knotName(), newScene.knotName());
        try (Writer writer = new BufferedWriter(new FileWriter(newScriptFile))) {
            writer.write(inkContent);
        }

        updateMasterSceneKnot(newScene);
        updateInkIncludes();
    }

    public static void updateSubsceneFile(Scene scene) throws IOException {
        File subsceneFile = getSubsceneFile(scene);
        Gson gson = new GsonBuilder().registerTypeAdapter(Subscene.class, new SubsceneSerializer(scene)).create();
        try(Writer writer = new BufferedWriter(new FileWriter(subsceneFile))) {
            gson.toJson(scene.getSubscenes(), writer);
        }
    }

    public static void updateCutsceneFile(Scene scene) throws IOException {
        File cutsceneFile = getCutsceneFile(scene);
        Gson gson = new GsonBuilder().registerTypeAdapter(Cutscene.class, new CutsceneSerializer(scene)).create();
        try(Writer writer = new BufferedWriter(new FileWriter(cutsceneFile))) {
            gson.toJson(scene.getCutscenes(), writer);
        }
    }

    public static void deleteSceneDirectory(Scene scene) throws IOException {
        File sceneFolder = getSceneFolder(scene);
        deleteDirectory(sceneFolder);
        updateInkIncludes();
    }

    public static void createCharacterFolder(CharacterStory characterStory) throws IOException {
        File characterFolder = getCharacterFolder(characterStory);
        createDirectory(characterFolder, SKINS_FOLDER_NAME);
        File dataFile = getDataFile(characterFolder);
        Gson gson = new Gson();
        try(Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            gson.toJson(characterStory, writer);
        }
    }

    public static void updateCharacterData(CharacterStory oldCharacter, CharacterStory newCharacter) throws IOException {
        File dataFile = getDataFile(oldCharacter);
        Gson gson = new Gson();
        try(Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            gson.toJson(newCharacter, writer);
        }
        File oldCharacterFolder = new File(characterDirectory, Util.snakeCase(oldCharacter.getName()));
        File newCharacterFolder = new File(characterDirectory, Util.snakeCase(newCharacter.getName()));
        Files.move(oldCharacterFolder.toPath(), newCharacterFolder.toPath());
    }

    public static void deleteCharacterFolder(CharacterStory characterStory) {
        deleteDirectory(getCharacterFolder(characterStory));
    }

    public static File getChapterDirectory(Chapter chapter) {
        return createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
    }

    public static File getScenesFolder(Chapter chapter) {
        return createDirectory(getChapterDirectory(chapter), "scenes");
    }

    public static void updateInkIncludes() throws IOException {
        List<Chapter> chapters = NarrativeCraftMod.getInstance().getChapterManager().getChapters();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("// ").append(Translation.message("file.main_ink_warning").append("\n\n").getString());
        stringBuilder.append("INCLUDE funcs.ink").append("\n");
        stringBuilder.append("INCLUDE vars.ink").append("\n\n");
        for(Chapter chapter : chapters) {
            stringBuilder.append("// Chapter ").append(chapter.getIndex()).append("\n");
            String chapterInkFilePath = "chapters\\" + chapter.getIndex() + "\\" + chapter.knotName() + EXTENSION_SCRIPT_FILE;
            stringBuilder.append("INCLUDE ").append(chapterInkFilePath).append("\n");
            for(Scene scene : chapter.getSortedSceneList()) {
                String sceneInkFilePath =
                        "chapters\\" + chapter.getIndex() + "\\"  +
                                "scenes\\" + Util.snakeCase(scene.getName()) + "\\" + Util.snakeCase(scene.getName()) + EXTENSION_SCRIPT_FILE;
                stringBuilder.append("INCLUDE ").append(sceneInkFilePath).append("\n");
            }
            stringBuilder.append("\n");
        }
        if(chapters.size() > 1) {
            stringBuilder.append("->").append(chapters.getFirst().knotName());
        }
        try(Writer writer = new BufferedWriter(new FileWriter(mainInkFile))) {
            writer.write(stringBuilder.toString());
        }
    }

    public static File getSceneFolder(Scene scene) {
        File scenesFolder = createFile(getChapterDirectory(scene.getChapter()), SCENES_DIRECTORY_NAME);
        return createDirectory(scenesFolder, Util.snakeCase(scene.getName()));
    }

    public static File getSubsceneFile(Scene scene) {
        return createFile(getDataFolder(scene), SUBSCENES_FILE_NAME);
    }

    public static File getCutsceneFile(Scene scene) {
        return createFile(getDataFolder(scene), CUTSCENES_FILE_NAME);
    }

    public static File getDataFolder(Scene scene) {
        File sceneFolder = getSceneFolder(scene);
        return createDirectory(sceneFolder, DATA_FOLDER_NAME);
    }

    public static File getCharacterFolder(CharacterStory characterStory) {
        return createDirectory(characterDirectory, Util.snakeCase(characterStory.getName()));
    }

    public static File getDataFile(Chapter chapter) {
        return getDataFile(getChapterDirectory(chapter));
    }

    public static File getDataFile(Scene scene) {
        File dataFolder = createDirectory(getSceneFolder(scene), DATA_FOLDER_NAME);
        return createFile(dataFolder, DATA_FILE_NAME);
    }

    public static File getDataFile(CharacterStory characterStory) {
        return createFile(getCharacterFolder(characterStory), DATA_FILE_NAME);
    }

    public static File getDataFile(File file) {
        return createFile(file, DATA_FILE_NAME);
    }

    public static File getDataFileFromSceneFolder(File sceneFile) {
        File dataFolder = createDirectory(sceneFile, DATA_FOLDER_NAME);
        return createFile(dataFolder, DATA_FILE_NAME);
    }

    public static File getInkFile(Chapter chapter) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
        return createFile(chapterFolder, "chapter_" + chapter.getIndex() + EXTENSION_SCRIPT_FILE);
    }

    public static File getInkFile(Scene scene) {
        File sceneFile = getSceneFolder(scene);
        return createFile(sceneFile, Util.snakeCase(scene.getName()) + EXTENSION_SCRIPT_FILE);
    }
}
