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
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.character.CharacterModel;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.serialization.*;
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
            NarrativeCraftFile.createGlobalDialogValues();
            DialogData.globalDialogData = NarrativeCraftFile.getGlobalDialogValues();
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
        if (chaptersSubFolder == null) return;
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        Gson gson = new Gson();
        for (File chapterFolder : chaptersSubFolder) {
            String dataContent = Files.readString(
                    NarrativeCraftFile.getDataFile(chapterFolder).toPath());
            Chapter chapterData = gson.fromJson(dataContent, Chapter.class);
            int index = Integer.parseInt(chapterFolder.getName());
            Chapter chapter = new Chapter(chapterData.getName(), chapterData.getDescription(), index);
            initScenesOfChapter(chapter);
            chapterManager.addChapter(chapter);
        }
    }

    private static void initScenesOfChapter(Chapter chapter) throws Exception {
        File scenesDirectory = NarrativeCraftFile.getScenesFolder(chapter);
        File[] scenesFolder = scenesDirectory.listFiles();
        if (scenesFolder == null) return;
        Gson gson = new Gson();
        for (File sceneFolder : scenesFolder) {
            File dataFile = NarrativeCraftFile.getDataFileFromSceneFolder(sceneFolder);
            String dataContent = Files.readString(dataFile.toPath());
            Scene sceneData = gson.fromJson(dataContent, Scene.class);
            if (sceneData.getRank() <= 0) {
                throw new Exception(String.format(
                        "Scene %s from chapter %s rank is equal to or less than 0.",
                        sceneData.name, chapter.getIndex()));
            }
            Scene scene = new Scene(sceneData.getName(), sceneData.getDescription(), chapter);
            scene.setRank(sceneData.getRank());
            initNpcs(scene);
            initAnimations(scene);
            initSubscenes(scene);
            initCutscenes(scene);
            initCameraAngleGroups(scene);
            initInteraction(scene);
            chapter.addScene(scene);
        }
    }

    private static void initAnimations(Scene scene) throws IOException {
        File animationFolder = NarrativeCraftFile.getAnimationsFolder(scene);
        File[] animationsFile = animationFolder.listFiles();
        if (animationsFile == null) return;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Animation.class, new AnimationSerializer(scene))
                .create();
        for (File animationFile : animationsFile) {
            String content = Files.readString(animationFile.toPath());
            Animation animation = gson.fromJson(content, Animation.class);
            if (animation == null) continue;
            scene.getAnimations().add(animation);
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
        if (subscenes == null) return;
        scene.getSubscenes().addAll(subscenes);
    }

    private static void initCutscenes(Scene scene) throws IOException {
        File cutsceneFile = NarrativeCraftFile.getCutsceneFile(scene);
        String content = Files.readString(cutsceneFile.toPath());
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Cutscene.class, new CutsceneSerializer(scene))
                .create();
        Type type = new TypeToken<List<Cutscene>>() {}.getType();
        List<Cutscene> cutscenes = gson.fromJson(content, type);
        if (cutscenes == null) return;
        scene.getCutscenes().addAll(cutscenes);
    }

    private static void initCameraAngleGroups(Scene scene) throws IOException {
        File cameraAngleGroupsFile = NarrativeCraftFile.getCameraAngelGroupFile(scene);
        String content = Files.readString(cameraAngleGroupsFile.toPath());
        Type type = new TypeToken<List<CameraAngle>>() {}.getType();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CharacterStoryData.class, new CharacterStoryDataSerializer(scene))
                .create();
        List<CameraAngle> cameraAngleGroups = gson.fromJson(content, type);
        if (cameraAngleGroups == null) return;
        cameraAngleGroups.forEach(group -> group.setScene(scene));
        scene.getCameraAngles().addAll(cameraAngleGroups);
    }

    private static void initInteraction(Scene scene) throws IOException {
        File interactionFile = NarrativeCraftFile.getInteractionFile(scene);
        String content = Files.readString(interactionFile.toPath());
        Type type = new TypeToken<List<Interaction>>() {}.getType();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CharacterStoryData.class, new CharacterStoryDataSerializer(scene))
                .create();
        List<Interaction> interactions = gson.fromJson(content, type);
        if (interactions == null) return;
        interactions.forEach(interaction -> interaction.setScene(scene));
        scene.setInteractions(interactions);
    }

    private static void initNpcs(Scene scene) throws Exception {
        File[] npcsFolder = NarrativeCraftFile.getNpcFolder(scene).listFiles();
        if (npcsFolder == null) return;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CharacterStory.class, new CharacterSerializer())
                .create();
        for (File characterFolder : npcsFolder) {
            File dataFile = NarrativeCraftFile.getDataFile(characterFolder);
            String dataContent = Files.readString(dataFile.toPath());
            CharacterStory characterStory = gson.fromJson(dataContent, CharacterStory.class);
            if (characterStory == null) {
                throw new Exception(String.format(
                        "NPC %s of scene %s couldn't be initialized", characterFolder.getName(), scene.getName()));
            }
            characterStory.setCharacterType(CharacterType.NPC);
            scene.addNpc(characterStory);
        }
    }

    private static void initCharacters() throws Exception {
        File[] charactersFolder = NarrativeCraftFile.characterDirectory.listFiles();
        if (charactersFolder == null) return;
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CharacterStory.class, new CharacterSerializer())
                .create();
        for (File characterFolder : charactersFolder) {
            File dataFile = NarrativeCraftFile.getDataFile(characterFolder);
            String dataContent = Files.readString(dataFile.toPath());
            CharacterStory characterStory = gson.fromJson(dataContent, CharacterStory.class);
            if (characterStory == null) {
                throw new Exception(String.format("Character %s couldn't be initialized", characterFolder.getName()));
            }
            characterStory.setCharacterType(CharacterType.MAIN);
            characterManager.addCharacter(characterStory);
        }
        if (characterManager.getMainCharacter() == null
                && !characterManager.getCharacterStories().isEmpty()) {
            CharacterStory characterStory =
                    characterManager.getCharacterStories().get(0);
            characterManager
                    .getCharacterStories()
                    .get(0)
                    .getMainCharacterAttribute()
                    .setMainCharacter(true);
            NarrativeCraftFile.updateCharacterData(characterStory, characterStory);
        }
        if (NarrativeCraftMod.firstTime) {
            CharacterStory steve = new CharacterStory(
                    "Steve", "Steve from Minecraft.", "17", "05", "2009", CharacterModel.WIDE, CharacterType.MAIN);
            CharacterStory alex = new CharacterStory(
                    "Alex", "Alex from Minecraft.", "22", "08", "2014", CharacterModel.SLIM, CharacterType.MAIN);
            steve.getMainCharacterAttribute().setMainCharacter(true);
            characterManager.addCharacter(steve);
            characterManager.addCharacter(alex);
            NarrativeCraftFile.createCharacterFolder(steve);
            NarrativeCraftFile.createCharacterFolder(alex);
            if (new Random().nextInt(0, 500) >= 445) {
                steve.getMainCharacterAttribute().setMainCharacter(false);
                CharacterStory herobrine = new CharacterStory(
                        "Herobrine",
                        "You can't escape me. §kYour story is now mine...",
                        "00",
                        "00",
                        "9999",
                        CharacterModel.WIDE,
                        CharacterType.MAIN);
                herobrine.getMainCharacterAttribute().setMainCharacter(true);
                characterManager.addCharacter(herobrine);
                NarrativeCraftFile.createCharacterFolder(herobrine);
            }
        }
    }
}
