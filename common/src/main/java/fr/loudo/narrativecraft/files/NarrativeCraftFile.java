package fr.loudo.narrativecraft.files;

import com.google.gson.Gson;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.util.InkUtil;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.nio.file.Files;
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
        File chapterInkFile = createFile(chapterFolder, "chapter_" + chapter.getIndex() + EXTENSION_SCRIPT_FILE);
        File dataFile = getDataFile(chapterFolder);

        String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", chapter.getName(), chapter.getDescription());
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            writer.write(content);
        }
        try (Writer writer = new BufferedWriter(new FileWriter(chapterInkFile))) {
            writer.write("=== chapter_" + chapter.getIndex() + " ===");
        }
    }

    public static void updateChapterData(Chapter chapter) throws IOException {
        File dataFile = getDataFile(chapter);
        Gson gson = new Gson();
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            gson.toJson(chapter, writer);
        }
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

        String content = String.format("{\"name\":\"%s\",\"description\":\"%s\",\"placement\":%s}", scene.getName(), scene.getDescription(), scene.getRank());
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            writer.write(content);
        }
        try (Writer writer = new BufferedWriter(new FileWriter(sceneInkFile))) {
            writer.write(scene.knotName() + "\n# on enter");
        }
        updateMasterSceneKnot(scene);
    }

    public static void updateMasterSceneKnot(Scene scene) throws IOException {
        if (scene.getRank() > 1) return;

        File chapterInkFile = getChapterInkFile(scene.getChapter());
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
        File sceneFolder = getSceneFile(oldScene);

        File newSceneFolder = new File(sceneFolder.getParent(), Util.snakeCase(newScene.getName()));

        if (!oldScene.getName().equalsIgnoreCase(newScene.getName())) {
            Files.move(sceneFolder.toPath(), newSceneFolder.toPath());
            sceneFolder = newSceneFolder;
        }

        File sceneData = getDataFile(newScene);
        String content = String.format(
                "{\"name\":\"%s\",\"description\":\"%s\",\"placement\":%s}",
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

        updateMasterSceneKnot(newScene);
    }


    public static File getChapterFolder(Chapter chapter) {
        return createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
    }

    public static File getSceneFile(Scene scene) {
        File scenesFolder = createFile(getChapterFolder(scene.getChapter()), SCENES_DIRECTORY_NAME);
        return createFile(scenesFolder, Util.snakeCase(scene.getName()));
    }

    private static File getDataFile(Chapter chapter) {
        File dataFile = getDataFile(getChapterFolder(chapter));
        return createFile(dataFile, DATA_FILE_NAME);
    }

    private static File getDataFile(Scene scene) {
        File dataFile = getDataFile(getSceneFile(scene));
        return createFile(dataFile, DATA_FILE_NAME);
    }

    private static File getDataFile(File file) {
        return createFile(file, DATA_FILE_NAME);
    }

    private static File getChapterInkFile(Chapter chapter) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
        return createFile(chapterFolder, "chapter_" + chapter.getIndex() + EXTENSION_SCRIPT_FILE);
    }

}
