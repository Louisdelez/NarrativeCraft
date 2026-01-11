/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkActionUtil;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.character.*;
import fr.loudo.narrativecraft.narrative.data.MainScreenData;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.story.StorySave;
import fr.loudo.narrativecraft.options.NarrativeClientOption;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.serialization.*;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import fr.loudo.narrativecraft.compat.api.NcId;
import fr.loudo.narrativecraft.compat.api.VersionAdapterLoader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

/**
 * MC 1.20.x version of NarrativeCraftFile.
 * Key differences from 1.21.x:
 * - DefaultPlayerSkin.get() returns PlayerSkin record
 * - PlayerSkin uses texture() instead of body().texturePath()
 * - PlayerSkin uses model() directly with Model enum
 */
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

    public static final String CAMERA_ANGLES_FILE_NAME = "camera_angles" + EXTENSION_DATA_FILE;
    public static final String CUTSCENES_FILE_NAME = "cutscenes" + EXTENSION_DATA_FILE;
    public static final String SUBSCENES_FILE_NAME = "subscenes" + EXTENSION_DATA_FILE;
    public static final String INTERACTION_FILE_NAME = "interactions" + EXTENSION_DATA_FILE;

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
    public static final String SKINS_FOLDER_NAME = "skins";

    public static File rootDirectory;
    public static File mainDirectory;
    public static File chaptersDirectory;
    public static File characterDirectory;
    public static File savesDirectory;
    public static File buildDirectory;
    public static File dataDirectory;
    public static File mainInkFile;
    public static File varsInkFile;
    public static File functionsInkFile;

    public static void init(MinecraftServer server) {
        NarrativeCraftMod.firstTime =
                !new File(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME).exists();
        NarrativeCraftFile.mainDirectory =
                createDirectory(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME);
        rootDirectory = createDirectory(Minecraft.getInstance().gameDirectory, DIRECTORY_NAME);
        chaptersDirectory = createDirectory(NarrativeCraftFile.mainDirectory, CHAPTERS_DIRECTORY_NAME);
        characterDirectory = createDirectory(NarrativeCraftFile.mainDirectory, CHARACTERS_DIRECTORY_NAME);
        savesDirectory = createDirectory(NarrativeCraftFile.mainDirectory, SAVES_DIRECTORY_NAME);
        buildDirectory = createDirectory(NarrativeCraftFile.mainDirectory, BUILD_DIRECTORY_NAME);
        dataDirectory = createDirectory(NarrativeCraftFile.mainDirectory, DATA_FOLDER_NAME);
        mainInkFile = createFile(NarrativeCraftFile.mainDirectory, MAIN_INK_NAME);
        if (!new File(NarrativeCraftFile.mainDirectory, VARS_INK_NAME).exists()) {
            varsInkFile = createFile(NarrativeCraftFile.mainDirectory, VARS_INK_NAME);
            try (Writer writer = new BufferedWriter(new FileWriter(varsInkFile))) {
                writer.write("// "
                        + Translation.message("file.vars_placeholder")
                                .append("\n")
                                .append("\n")
                                .getString());
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.warn("Couldn't write on vars ink file", e);
            }
        }
        if (!new File(NarrativeCraftFile.mainDirectory, FUNCTIONS_INK_NAME).exists()) {
            functionsInkFile = createFile(NarrativeCraftFile.mainDirectory, FUNCTIONS_INK_NAME);
            try (Writer writer = new BufferedWriter(new FileWriter(functionsInkFile))) {
                writer.write("// "
                        + Translation.message("file.funcs_placeholder")
                                .append("\n")
                                .append("\n")
                                .getString());
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.warn("Couldn't write on functions ink file", e);
            }
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

    public static boolean saveExists() {
        return new File(savesDirectory, SAVE_FILE_NAME).exists();
    }

    public static String storyContent() throws IOException {
        return Files.readString(getStoryFile().toPath());
    }

    public static StorySave saveContent() throws IOException {
        File saveFile = new File(savesDirectory, SAVE_FILE_NAME);
        String saveContent = Files.readString(saveFile.toPath());
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(StorySave.class, new StorySaveSerializer())
                .create();
        return gson.fromJson(saveContent, StorySave.class);
    }

    public static void writeSave(StorySave save) throws IOException {
        File saveFile = createFile(savesDirectory, SAVE_FILE_NAME);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(StorySave.class, new StorySaveSerializer())
                .create();
        try (Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
            gson.toJson(save, writer);
        }
    }

    public static void removeSave() {
        File saveFile = new File(savesDirectory, SAVE_FILE_NAME);
        saveFile.delete();
    }

    public static File getStoryFile() {
        File buildFolder = createDirectory(mainDirectory, BUILD_DIRECTORY_NAME);
        return new File(buildFolder, STORY_FILE_NAME);
    }

    public static MainScreenData getMainScreenBackground() throws IOException {
        File mainBackgroundScreenFile = createFile(dataDirectory, MAIN_SCREEN_BACKGROUND_FILE_NAME);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CharacterStoryData.class, new CharacterStoryDataSerializer(null))
                .create();
        String data = Files.readString(mainBackgroundScreenFile.toPath());
        MainScreenData mainScreenData = gson.fromJson(data, MainScreenData.class);

        return mainScreenData != null ? mainScreenData : new MainScreenData();
    }

    public static void updateMainScreenBackground(MainScreenData mainScreenData, Scene scene) throws IOException {
        File mainBackgroundScreenFile = createFile(dataDirectory, MAIN_SCREEN_BACKGROUND_FILE_NAME);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CharacterStoryData.class, new CharacterStoryDataSerializer(scene))
                .create();
        try (Writer writer = new BufferedWriter(new FileWriter(mainBackgroundScreenFile))) {
            gson.toJson(mainScreenData, writer);
        }
    }

    public static void createChapterDirectory(Chapter chapter) throws IOException {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
        createDirectory(chapterFolder.getAbsoluteFile(), SCENES_DIRECTORY_NAME);
        File chapterInkFile = createFile(chapterFolder, "chapter_" + chapter.getIndex() + EXTENSION_SCRIPT_FILE);
        File dataFile = getDataFile(chapterFolder);

        String content =
                String.format("{\"name\":\"%s\",\"description\":\"%s\"}", chapter.getName(), chapter.getDescription());
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
        String content =
                String.format("{\"name\":\"%s\",\"description\":\"%s\"}", chapter.getName(), chapter.getDescription());
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
        File chapterFolder = createDirectory(
                chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = createDirectory(chapterFolder.getAbsoluteFile(), SCENES_DIRECTORY_NAME);
        File sceneFolder = createDirectory(scenesFolder, scene.folderName());

        File dataFolder = createDirectory(sceneFolder, DATA_FOLDER_NAME);

        File dataFile = getDataFile(dataFolder);
        File sceneInkFile = createFile(sceneFolder, Util.snakeCase(scene.getName()) + EXTENSION_SCRIPT_FILE);

        createDirectory(dataFolder, NPC_FOLDER_NAME);
        createDirectory(dataFolder, ANIMATIONS_FOLDER_NAME);
        createFile(dataFolder, CUTSCENES_FILE_NAME);
        createFile(dataFolder, SUBSCENES_FILE_NAME);
        createFile(dataFolder, CAMERA_ANGLES_FILE_NAME);
        createFile(dataFolder, INTERACTION_FILE_NAME);

        String content = String.format(
                "{\"name\":\"%s\",\"description\":\"%s\",\"rank\":%s}",
                scene.getName(), scene.getDescription(), scene.getRank());
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            writer.write(content);
        }
        try (Writer writer = new BufferedWriter(new FileWriter(sceneInkFile))) {
            writer.write("=== " + scene.knotName() + " ===" + "\n" + "# on enter");
        }
        updateMasterSceneKnot(scene);
    }

    public static void updateMasterSceneKnot(Scene scene) throws IOException {
        if (scene.getRank() > 1) return;

        File chapterInkFile = getInkFile(scene.getChapter());
        String originalContent = Files.readString(chapterInkFile.toPath());

        Matcher matcher = InkActionUtil.SCENE_KNOT_PATTERN.matcher(originalContent);

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

    public static void updateSceneNameScript(Scene oldScene, Scene newScene) throws IOException {
        for (Chapter chapter :
                NarrativeCraftMod.getInstance().getChapterManager().getChapters()) {
            File scriptChapterFile = getScriptFile(chapter);
            String chapterContent = Files.readString(scriptChapterFile.toPath());
            chapterContent = chapterContent.replaceAll(oldScene.knotName(), newScene.knotName());
            try (Writer writer = new BufferedWriter(new FileWriter(scriptChapterFile))) {
                writer.write(chapterContent);
            }
            for (Scene scene : chapter.getSortedSceneList()) {
                File scriptFile = getScriptFile(scene);
                String sceneContent = Files.readString(scriptFile.toPath());
                sceneContent = sceneContent.replaceAll(oldScene.knotName(), newScene.knotName());
                try (Writer writer = new BufferedWriter(new FileWriter(scriptFile))) {
                    writer.write(sceneContent);
                }
            }
        }
    }

    public static void updateSceneData(Scene oldScene, Scene newScene) throws IOException {
        File sceneFolder = getSceneFolder(oldScene);

        File newSceneFolder = new File(sceneFolder.getParent(), newScene.folderName());

        if (!oldScene.folderName().equalsIgnoreCase(newScene.folderName())) {
            Files.move(sceneFolder.toPath(), newSceneFolder.toPath());
            sceneFolder = newSceneFolder;
        }

        File sceneData = getDataFile(newScene);
        String content = String.format(
                "{\"name\":\"%s\",\"description\":\"%s\",\"rank\":%s}",
                newScene.getName(), newScene.getDescription(), newScene.getRank());

        try (Writer writer = new BufferedWriter(new FileWriter(sceneData))) {
            writer.write(content);
        }

        File oldScriptFile = new File(sceneFolder, Util.snakeCase(oldScene.getName()) + EXTENSION_SCRIPT_FILE);
        File newScriptFile = new File(sceneFolder, Util.snakeCase(newScene.getName()) + EXTENSION_SCRIPT_FILE);

        Files.move(oldScriptFile.toPath(), newScriptFile.toPath());
    }

    public static void updateSubsceneFile(Scene scene) throws IOException {
        File subsceneFile = getSubsceneFile(scene);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Subscene.class, new SubsceneSerializer(scene))
                .create();
        try (Writer writer = new BufferedWriter(new FileWriter(subsceneFile))) {
            gson.toJson(scene.getSubscenes(), writer);
        }
    }

    public static void updateCutsceneFile(Scene scene) throws IOException {
        File cutsceneFile = getCutsceneFile(scene);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Cutscene.class, new CutsceneSerializer(scene))
                .create();
        try (Writer writer = new BufferedWriter(new FileWriter(cutsceneFile))) {
            gson.toJson(scene.getCutscenes(), writer);
        }
    }

    public static void updateCameraAngles(Scene scene) throws IOException {
        File cameraAngelGroupFile = getCameraAngelGroupFile(scene);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CharacterStoryData.class, new CharacterStoryDataSerializer(scene))
                .create();
        try (Writer writer = new BufferedWriter(new FileWriter(cameraAngelGroupFile))) {
            gson.toJson(scene.getCameraAngles(), writer);
        }
    }

    public static void updateInteractionsFile(Scene scene) throws IOException {
        File interactionsFile = getInteractionFile(scene);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CharacterStoryData.class, new CharacterStoryDataSerializer(scene))
                .create();
        try (Writer writer = new BufferedWriter(new FileWriter(interactionsFile))) {
            gson.toJson(scene.getInteractions(), writer);
        }
    }

    public static void updateAnimationFile(Animation newAnimation) throws IOException {
        File animationsFolder = getAnimationsFolder(newAnimation.getScene());
        File animationFile = createFile(animationsFolder, Util.snakeCase(newAnimation.getName()) + EXTENSION_DATA_FILE);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Animation.class, new AnimationSerializer(newAnimation.getScene()))
                .create();
        try (Writer writer = new BufferedWriter(new FileWriter(animationFile))) {
            gson.toJson(newAnimation, writer);
        }
    }

    public static void updateAnimationFile(Animation oldAnimation, Animation newAnimation) throws IOException {
        File animationsFolder = getAnimationsFolder(newAnimation.getScene());
        NarrativeCraftFile.updateAnimationFile(newAnimation);
        new File(animationsFolder, Util.snakeCase(oldAnimation.getName()) + EXTENSION_DATA_FILE).delete();
    }

    public static void deleteAnimationFile(Animation animation) {
        File animationsFolder = getAnimationsFolder(animation.getScene());
        new File(animationsFolder, Util.snakeCase(animation.getName()) + EXTENSION_DATA_FILE).delete();
    }

    public static void deleteSceneDirectory(Scene scene) throws IOException {
        File sceneFolder = getSceneFolder(scene);
        deleteDirectory(sceneFolder);
        updateInkIncludes();
    }

    /**
     * MC 1.20.x: DefaultPlayerSkin.get() returns PlayerSkin record.
     * PlayerSkin uses texture() instead of body().texturePath().
     */
    public static void createCharacterFolder(CharacterStory characterStory) throws IOException {
        File characterFolder = getCharacterFolder(characterStory);
        File skinsFolder = createDirectory(characterFolder, SKINS_FOLDER_NAME);
        File dataFile = getDataFile(characterFolder);
        File mainSkinFile = createFile(skinsFolder, "main.png");
        PlayerSkin defaultPlayerSkin = DefaultPlayerSkin.get(UUID.randomUUID());
        try (InputStream inputStream = Minecraft.getInstance()
                .getResourceManager()
                .open(defaultPlayerSkin.texture())) {
            Files.copy(inputStream, mainSkinFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
        Gson gson = new Gson();
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            gson.toJson(characterStory, writer);
        }
        try {
            characterStory.setModel(
                    CharacterModel.valueOf(defaultPlayerSkin.model().name()));
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * MC 1.20.x: DefaultPlayerSkin.get() returns PlayerSkin record.
     * PlayerSkin uses texture() instead of body().texturePath().
     */
    public static void createCharacterFolder(CharacterStory characterStory, Scene scene) throws IOException {
        File characterFolder = getCharacterFolder(characterStory, scene);
        File mainSkinFile = createFile(characterFolder, "main.png");
        File dataFile = createFile(characterFolder, DATA_FILE_NAME);
        PlayerSkin defaultPlayerSkin = DefaultPlayerSkin.get(UUID.randomUUID());
        try (InputStream inputStream = Minecraft.getInstance()
                .getResourceManager()
                .open(defaultPlayerSkin.texture())) {
            Files.copy(inputStream, mainSkinFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
        Gson gson = new Gson();
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            gson.toJson(characterStory, writer);
        }
        try {
            characterStory.setModel(
                    CharacterModel.valueOf(defaultPlayerSkin.model().name()));
        } catch (IllegalArgumentException ignored) {
        }
    }

    public static List<File> getSkinFiles(CharacterStory characterStory) {
        File[] skinFiles = createDirectory(getCharacterFolder(characterStory), SKINS_FOLDER_NAME)
                .listFiles();
        if (skinFiles == null) return new ArrayList<>();
        return Arrays.stream(skinFiles).toList();
    }

    public static File getSkinFile(CharacterStory characterStory, Scene scene) {
        return new File(getCharacterFolder(characterStory, scene), "main.png");
    }

    public static void updateCharacterData(CharacterStory oldCharacter, CharacterStory newCharacter)
            throws IOException {
        File dataFile = getDataFile(oldCharacter);
        Gson gson = new Gson();
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            gson.toJson(newCharacter, writer);
        }
        File oldCharacterFolder = new File(characterDirectory, Util.snakeCase(oldCharacter.getName()));
        File newCharacterFolder = new File(characterDirectory, Util.snakeCase(newCharacter.getName()));
        Files.move(oldCharacterFolder.toPath(), newCharacterFolder.toPath());
    }

    public static void updateCharacterData(CharacterStory oldCharacter, CharacterStory newCharacter, Scene scene)
            throws IOException {
        File npcFolder = getNpcFolder(scene);
        File dataFile = getDataFile(oldCharacter, scene);
        Gson gson = new Gson();
        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            gson.toJson(newCharacter, writer);
        }
        File oldCharacterFolder = new File(npcFolder, Util.snakeCase(oldCharacter.getName()));
        File newCharacterFolder = new File(npcFolder, Util.snakeCase(newCharacter.getName()));
        Files.move(oldCharacterFolder.toPath(), newCharacterFolder.toPath());
    }

    public static void deleteCharacterFolder(CharacterStory characterStory) {
        deleteDirectory(getCharacterFolder(characterStory));
    }

    public static void deleteCharacterFolder(CharacterStory characterStory, Scene scene) {
        deleteDirectory(getCharacterFolder(characterStory, scene));
    }

    public static File getChapterDirectory(Chapter chapter) {
        return createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
    }

    public static File getChapterFolder(Chapter chapter) {
        return createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
    }

    public static File getScenesFolder(Chapter chapter) {
        return createDirectory(getChapterDirectory(chapter), "scenes");
    }

    public static File getNpcFolder(Scene scene) {
        File dataFolder = getDataFolder(scene);
        return createDirectory(dataFolder, NPC_FOLDER_NAME);
    }

    public static void updateInkIncludes() throws IOException {
        List<Chapter> chapters =
                NarrativeCraftMod.getInstance().getChapterManager().getChapters();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("// ")
                .append(Translation.message("file.main_ink_warning")
                        .append("\n\n")
                        .getString());
        stringBuilder.append("INCLUDE funcs.ink").append("\n");
        stringBuilder.append("INCLUDE vars.ink").append("\n\n");
        for (Chapter chapter : chapters) {
            File scenesFolder = getScenesFolder(chapter);
            stringBuilder.append("// Chapter ").append(chapter.getIndex()).append("\n");
            String chapterInkFilePath =
                    "chapters\\" + chapter.getIndex() + "\\" + chapter.knotName() + EXTENSION_SCRIPT_FILE;
            stringBuilder.append("INCLUDE ").append(chapterInkFilePath).append("\n");
            for (Scene scene : chapter.getSortedSceneList()) {
                File sceneFolder = new File(scenesFolder, Util.snakeCase(scene.getName()));
                if (sceneFolder.exists()) {
                    File newSceneFolder = new File(scenesFolder, scene.folderName());
                    deleteDirectory(newSceneFolder);
                    Files.move(sceneFolder.toPath(), newSceneFolder.toPath());
                }
                String sceneInkFilePath = "chapters\\" + chapter.getIndex() + "\\" + "scenes\\" + scene.folderName()
                        + "\\" + Util.snakeCase(scene.getName()) + EXTENSION_SCRIPT_FILE;
                stringBuilder.append("INCLUDE ").append(sceneInkFilePath).append("\n");
            }
            stringBuilder.append("\n");
        }
        if (!chapters.isEmpty()) {
            stringBuilder.append("->").append(chapters.get(0).knotName());
        }
        try (Writer writer = new BufferedWriter(new FileWriter(mainInkFile))) {
            writer.write(stringBuilder.toString());
        }
    }

    public static void updateSceneRankData(Chapter chapter) throws IOException {
        File scenesFolder = getScenesFolder(chapter);
        File[] sceneFolders = scenesFolder.listFiles(File::isDirectory);
        if (sceneFolders == null) return;

        Map<String, File> currentFolders = new HashMap<>();
        for (File folder : sceneFolders) {
            currentFolders.put(folder.getName(), folder);
        }

        Map<File, File> tempRenames = new HashMap<>();
        for (Scene scene : chapter.getSortedSceneList()) {
            String expectedName = scene.folderName();
            File existingFolder = currentFolders.values().stream()
                    .filter(f -> f.getName().startsWith(scene.getChapter().getIndex() + "_")
                            && f.getName().endsWith("_" + Util.snakeCase(scene.getName())))
                    .findFirst()
                    .orElse(null);

            if (existingFolder != null && !existingFolder.getName().equals(expectedName)) {
                File tempFile = new File(scenesFolder, expectedName + "_tmp");
                Files.move(existingFolder.toPath(), tempFile.toPath());
                tempRenames.put(tempFile, new File(scenesFolder, expectedName));
            }
        }

        for (Map.Entry<File, File> entry : tempRenames.entrySet()) {
            File tempFile = entry.getKey();
            File finalFile = entry.getValue();
            Files.move(tempFile.toPath(), finalFile.toPath());
        }

        for (Scene scene : chapter.getScenes()) {
            File sceneData = getDataFile(scene);

            if (!sceneData.exists()) continue;

            String content = String.format(
                    "{\"name\":\"%s\",\"description\":\"%s\",\"rank\":%d}",
                    scene.getName(), scene.getDescription(), scene.getRank());

            try (Writer writer = new BufferedWriter(new FileWriter(sceneData))) {
                writer.write(content);
            }
        }
    }

    public static File getAnimationsFolder(Scene scene) {
        return createDirectory(getDataFolder(scene), ANIMATIONS_FOLDER_NAME);
    }

    public static File getSceneFolder(Scene scene) {
        File scenesFolder = createFile(getChapterDirectory(scene.getChapter()), SCENES_DIRECTORY_NAME);
        return createDirectory(scenesFolder, scene.folderName());
    }

    public static File getScriptFile(Scene scene) {
        return createFile(getSceneFolder(scene), Util.snakeCase(scene.getName()) + EXTENSION_SCRIPT_FILE);
    }

    public static File getScriptFile(Chapter chapter) {
        return createFile(getChapterFolder(chapter), "chapter_" + chapter.getIndex() + EXTENSION_SCRIPT_FILE);
    }

    public static File getSubsceneFile(Scene scene) {
        return createFile(getDataFolder(scene), SUBSCENES_FILE_NAME);
    }

    public static File getCutsceneFile(Scene scene) {
        return createFile(getDataFolder(scene), CUTSCENES_FILE_NAME);
    }

    public static File getCameraAngelGroupFile(Scene scene) {
        return createFile(getDataFolder(scene), CAMERA_ANGLES_FILE_NAME);
    }

    public static File getInteractionFile(Scene scene) {
        return createFile(getDataFolder(scene), INTERACTION_FILE_NAME);
    }

    public static File getDataFolder(Scene scene) {
        File sceneFolder = getSceneFolder(scene);
        return createDirectory(sceneFolder, DATA_FOLDER_NAME);
    }

    public static File getCharacterFolder(CharacterStory characterStory) {
        return createDirectory(characterDirectory, Util.snakeCase(characterStory.getName()));
    }

    public static File getCharacterFolder(CharacterStory characterStory, Scene scene) {
        return createDirectory(getNpcFolder(scene), Util.snakeCase(characterStory.getName()));
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

    public static File getDataFile(CharacterStory characterStory, Scene scene) {
        return createFile(getCharacterFolder(characterStory, scene), DATA_FILE_NAME);
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

    public static void updateUserOptions(NarrativeClientOption narrativeClientOption) {
        File userOptionsFile = createFile(rootDirectory, USER_OPTIONS_FILE_NAME);
        try {
            try (Writer writer = new BufferedWriter(new FileWriter(userOptionsFile))) {
                new Gson().toJson(narrativeClientOption, writer);
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Couldn't update user options! ", e);
            }
        } catch (JsonIOException e) {
            NarrativeCraftMod.LOGGER.error("Couldn't update user options! ", e);
        }
    }

    public static NarrativeClientOption loadUserOptions() {
        File userOptionsFile = createFile(rootDirectory, USER_OPTIONS_FILE_NAME);
        try {
            String content = Files.readString(userOptionsFile.toPath());
            if (content.isEmpty()) {
                updateUserOptions(new NarrativeClientOption());
            }
            NarrativeClientOption option = new Gson().fromJson(content, NarrativeClientOption.class);
            return Objects.requireNonNullElseGet(option, NarrativeClientOption::new);
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Couldn't read user options! ", e);
        }
        return null;
    }

    public static void updateWorldOptions(NarrativeWorldOption narrativeWorldOption) {
        File worldOptionsFile = createFile(dataDirectory, WORLD_OPTIONS_FILE_NAME);
        try {
            try (Writer writer = new BufferedWriter(new FileWriter(worldOptionsFile))) {
                new Gson().toJson(narrativeWorldOption, writer);
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Couldn't update world options! ", e);
            }
        } catch (JsonIOException e) {
            NarrativeCraftMod.LOGGER.error("Couldn't update world options! ", e);
        }
    }

    public static void updateWorldOptions(File worldOptionsFile, NarrativeWorldOption narrativeWorldOption) {
        try {
            try (Writer writer = new BufferedWriter(new FileWriter(worldOptionsFile))) {
                new Gson().toJson(narrativeWorldOption, writer);
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Couldn't update world options! ", e);
            }
        } catch (JsonIOException e) {
            NarrativeCraftMod.LOGGER.error("Couldn't update world options! ", e);
        }
    }

    public static NarrativeWorldOption getNarrativeCraftWorldVersion(String levelName, String levelVersion) {
        File worldOptionFile = getWorldOptionFile(levelName);
        if (!worldOptionFile.exists()) return null;
        try {
            String worldOptionContent = Files.readString(worldOptionFile.toPath());
            NarrativeWorldOption option = new Gson().fromJson(worldOptionContent, NarrativeWorldOption.class);
            if (option.stringMcVersion.isEmpty()) {
                option.stringMcVersion = levelVersion;
                updateWorldOptions(worldOptionFile, option);
            }
            return option;
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.info("Can't get narrative craft world version of {}", levelName, e);
        }
        return null;
    }

    public static File getWorldOptionFile(String levelName) {
        Path path = Minecraft.getInstance().getLevelSource().getLevelPath(levelName);
        File narrativecraftFolder = new File(path.toFile(), "narrativecraft");
        File dataFolder = new File(narrativecraftFolder, "data");
        return new File(dataFolder, "world_options" + NarrativeCraftFile.EXTENSION_DATA_FILE);
    }

    public static NarrativeWorldOption loadWorldOptions() {
        File worldOptionFile = createFile(dataDirectory, WORLD_OPTIONS_FILE_NAME);
        try {
            String content = Files.readString(worldOptionFile.toPath());
            if (content.isEmpty()) {
                updateWorldOptions(new NarrativeWorldOption());
            }
            NarrativeWorldOption option = new Gson().fromJson(content, NarrativeWorldOption.class);
            return Objects.requireNonNullElseGet(option, NarrativeWorldOption::new);
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Couldn't read world options! ", e);
        }
        return null;
    }

    public static void createGlobalDialogValues() throws IOException {
        File dialogFile = new File(dataDirectory, DIALOG_FILE_NAME);
        if (dialogFile.exists()) return;
        try (Writer writer = new BufferedWriter(new FileWriter(dialogFile))) {
            new Gson().toJson(DialogData.globalDialogData, writer);
        }
    }

    public static DialogData getGlobalDialogValues() throws IOException {
        File dialogFile = createFile(dataDirectory, DIALOG_FILE_NAME);
        String dialogContent = Files.readString(dialogFile.toPath());
        return new Gson().fromJson(dialogContent, DialogData.class);
    }

    public static void updateGlobalDialogValues(DialogData dialogData) throws IOException {
        File dialogFile = createFile(dataDirectory, DIALOG_FILE_NAME);
        try (Writer writer = new BufferedWriter(new FileWriter(dialogFile))) {
            new Gson().toJson(dialogData, writer);
        }
    }

    public static NcId getMainCharacterSkin() {
        CharacterStory characterStory =
                NarrativeCraftMod.getInstance().getCharacterManager().getMainCharacter();
        if (characterStory == null) return null;
        CharacterRuntime characterRuntime = new CharacterRuntime(characterStory, null, null, null);
        CharacterSkinController characterSkinController = new CharacterSkinController(characterRuntime, null, null);
        File mainSkinFile = characterSkinController.getMainSkinFile();
        if (mainSkinFile == null) return null;
        return NcId.of(
                NarrativeCraftMod.MOD_ID,
                "character/" + Util.snakeCase(characterStory.getName()) + "/" + Util.snakeCase(mainSkinFile.getName()));
    }
}
