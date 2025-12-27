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

package fr.loudo.narrativecraft.network.handlers;

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
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.network.data.*;
import fr.loudo.narrativecraft.network.screen.*;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.util.Util;
import java.io.IOException;
import net.minecraft.server.level.ServerPlayer;

public class ServerPacketHandler {

    private static final ChapterManager CHAPTER_MANAGER =
            NarrativeCraftMod.getInstance().getChapterManager();
    private static final CharacterManager CHARACTER_MANAGER =
            NarrativeCraftMod.getInstance().getCharacterManager();

    public static void chapterData(BiChapterDataPacket packet, ServerPlayer player) {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Chapter chapter = new Chapter(
                    packet.name(),
                    packet.description(),
                    chapterManager.getChapters().size() + 1);
            try {
                chapterManager.addChapter(chapter);
                NarrativeCraftFile.createChapterDirectory(chapter);
                Util.broadcastPacket(
                        new BiChapterDataPacket(packet.name(), packet.description(), "", TypeStoryData.ADD),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.storyManager());
            } catch (Exception e) {
                chapterManager.removeChapter(chapter);
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Chapter existingChapter = CHAPTER_MANAGER.getChapterByName(packet.chapterName());
            if (existingChapter == null) return;
            Chapter newChapter = new Chapter(packet.name(), packet.description(), existingChapter.getIndex());
            Chapter oldChapter = new Chapter(
                    existingChapter.getName(), existingChapter.getDescription(), existingChapter.getIndex());
            try {
                NarrativeCraftFile.updateChapterData(newChapter);

                existingChapter.setName(packet.name());
                existingChapter.setDescription(packet.description());
                NarrativeCraftFile.updateInkIncludes();
                Util.broadcastPacket(
                        new BiChapterDataPacket(
                                packet.name(), packet.description(), packet.chapterName(), TypeStoryData.EDIT),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.storyManager());
            } catch (Exception e) {
                existingChapter.setName(oldChapter.getName());
                existingChapter.setDescription(oldChapter.getDescription());
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        }
    }

    public static void sceneData(BiSceneDataPacket packet, ServerPlayer player) {
        Chapter chapter = CHAPTER_MANAGER.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Scene scene = new Scene(packet.name(), packet.description(), chapter);
            try {
                NarrativeCraftFile.createSceneFolder(scene);
                chapter.addScene(scene);
                NarrativeCraftFile.updateInkIncludes();
                Util.broadcastPacket(
                        new BiSceneDataPacket(
                                packet.name(),
                                packet.description(),
                                packet.chapterIndex(),
                                scene.getRank(),
                                "",
                                TypeStoryData.ADD),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(player, new S2CSceneScreenPacket(chapter.getIndex()));
            } catch (Exception e) {
                chapter.removeScene(scene);
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Scene existingScene = chapter.getSceneByName(packet.sceneName());
            if (existingScene == null) return;

            Scene newScene = new Scene(packet.name(), packet.description(), chapter);
            newScene.setRank(existingScene.getRank());
            Scene oldScene = new Scene(existingScene.getName(), existingScene.getDescription(), chapter);
            oldScene.setRank(existingScene.getRank());
            try {
                NarrativeCraftFile.updateSceneData(oldScene, newScene);
                existingScene.setName(packet.name());
                existingScene.setDescription(packet.description());
                NarrativeCraftFile.updateSceneNameScript(oldScene, newScene);
                if (existingScene.getRank() != packet.rank()) {
                    chapter.setSceneRank(existingScene, packet.rank());
                    NarrativeCraftFile.updateSceneRankData(chapter);
                }
                NarrativeCraftFile.updateMasterSceneKnot(existingScene);
                NarrativeCraftFile.updateInkIncludes();
                Util.broadcastPacket(
                        new BiSceneDataPacket(
                                packet.name(),
                                packet.description(),
                                packet.chapterIndex(),
                                packet.rank(),
                                packet.sceneName(),
                                TypeStoryData.EDIT),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(player, new S2CSceneScreenPacket(chapter.getIndex()));
            } catch (Exception e) {
                existingScene.setName(oldScene.getName());
                existingScene.setDescription(oldScene.getDescription());
                chapter.setSceneRank(existingScene, oldScene.getRank());
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        }
    }

    public static void animationData(BiAnimationDataPacket packet, ServerPlayer player) {
        Chapter chapter = CHAPTER_MANAGER.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;
        Animation exitingAnimation = scene.getAnimationByName(packet.animationName());
        if (exitingAnimation == null) return;

        if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Animation oldAnimation = new Animation(packet.animationName(), scene);
            try {
                exitingAnimation.setName(packet.name());
                exitingAnimation.setDescription(packet.description());
                NarrativeCraftFile.updateAnimationFile(oldAnimation, exitingAnimation);
                for (Chapter chapter1 : CHAPTER_MANAGER.getChapters()) {
                    for (Scene scene1 : chapter1.getSortedSceneList()) {
                        NarrativeCraftFile.updateSubsceneFile(scene1);
                        NarrativeCraftFile.updateCutsceneFile(scene1);
                    }
                }
                Util.broadcastPacket(
                        new BiAnimationDataPacket(
                                exitingAnimation.getName(),
                                exitingAnimation.getDescription(),
                                chapter.getIndex(),
                                scene.getName(),
                                oldAnimation.getName(),
                                TypeStoryData.EDIT),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CAnimationsScreenPacket(chapter.getIndex(), scene.getName()));
            } catch (Exception e) {
                exitingAnimation.setName(oldAnimation.getName());
                exitingAnimation.setDescription(oldAnimation.getDescription());
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        }
    }

    public static void cameraAngleData(BiCameraAngleDataPacket packet, ServerPlayer player) {
        Chapter chapter = CHAPTER_MANAGER.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            CameraAngle cameraAngleGroup = new CameraAngle(packet.name(), packet.description(), scene);
            try {
                scene.addCameraAngleGroup(cameraAngleGroup);
                NarrativeCraftFile.updateCameraAngles(scene);
                Util.broadcastPacket(
                        new BiCameraAngleDataPacket(
                                packet.name(),
                                packet.description(),
                                chapter.getIndex(),
                                scene.getName(),
                                "",
                                TypeStoryData.ADD),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CCameraAnglesScreenPacket(chapter.getIndex(), scene.getName()));
            } catch (Exception e) {
                scene.removeCameraAngleGroup(cameraAngleGroup);
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            CameraAngle existingCameraAngleGroup = scene.getCameraAngleByName(packet.cameraAngleName());
            if (existingCameraAngleGroup == null) return;
            CameraAngle oldCameraAngleGroup = new CameraAngle(packet.name(), packet.description(), scene);
            try {
                existingCameraAngleGroup.setName(packet.name());
                existingCameraAngleGroup.setDescription(packet.description());
                NarrativeCraftFile.updateCameraAngles(scene);
                Util.broadcastPacket(
                        new BiCameraAngleDataPacket(
                                packet.name(),
                                packet.description(),
                                chapter.getIndex(),
                                scene.getName(),
                                existingCameraAngleGroup.getName(),
                                TypeStoryData.EDIT),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CCameraAnglesScreenPacket(chapter.getIndex(), scene.getName()));
            } catch (Exception e) {
                existingCameraAngleGroup.setName(oldCameraAngleGroup.getName());
                existingCameraAngleGroup.setDescription(oldCameraAngleGroup.getDescription());
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        }
    }

    public static void cutsceneData(BiCutsceneDataPacket packet, ServerPlayer player) {
        Chapter chapter = CHAPTER_MANAGER.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Cutscene cutscene = new Cutscene(packet.name(), packet.description(), scene);
            try {
                scene.addCutscene(cutscene);
                NarrativeCraftFile.updateCutsceneFile(scene);
                Util.broadcastPacket(
                        new BiCutsceneDataPacket(
                                packet.name(),
                                packet.description(),
                                chapter.getIndex(),
                                scene.getName(),
                                "",
                                TypeStoryData.ADD),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CCutscenesScreenPacket(chapter.getIndex(), scene.getName()));
            } catch (Exception e) {
                scene.removeCutscene(cutscene);
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Cutscene existingCutscene = scene.getCutsceneByName(packet.cutsceneName());
            if (existingCutscene == null) return;
            Cutscene oldCutscene = new Cutscene(existingCutscene.getName(), existingCutscene.getDescription(), scene);
            try {
                existingCutscene.setName(packet.name());
                existingCutscene.setDescription(packet.description());
                NarrativeCraftFile.updateCutsceneFile(scene);
                Util.broadcastPacket(
                        new BiCutsceneDataPacket(
                                packet.name(),
                                packet.description(),
                                chapter.getIndex(),
                                scene.getName(),
                                existingCutscene.getName(),
                                TypeStoryData.EDIT),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CCutscenesScreenPacket(chapter.getIndex(), scene.getName()));
            } catch (Exception e) {
                existingCutscene.setName(oldCutscene.getName());
                existingCutscene.setDescription(oldCutscene.getDescription());
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        }
    }

    public static void interactionData(BiInteractionDataPacket packet, ServerPlayer player) {
        Chapter chapter = CHAPTER_MANAGER.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Interaction interaction = new Interaction(packet.name(), packet.description(), scene);
            try {
                scene.addInteraction(interaction);
                NarrativeCraftFile.updateInteractionsFile(scene);
                Util.broadcastPacket(
                        new BiInteractionDataPacket(
                                packet.name(),
                                packet.description(),
                                chapter.getIndex(),
                                scene.getName(),
                                "",
                                TypeStoryData.ADD),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CInteractionsScreenPacket(chapter.getIndex(), scene.getName()));
            } catch (IOException e) {
                scene.removeInteraction(interaction);
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Interaction existingInteraction = scene.getInteractionByName(packet.interactionName());
            if (existingInteraction == null) return;
            Interaction oldInteraction =
                    new Interaction(existingInteraction.getName(), existingInteraction.getDescription(), scene);

            try {
                existingInteraction.setName(packet.name());
                existingInteraction.setDescription(packet.description());
                NarrativeCraftFile.updateInteractionsFile(scene);
                Util.broadcastPacket(
                        new BiInteractionDataPacket(
                                packet.name(),
                                packet.description(),
                                chapter.getIndex(),
                                scene.getName(),
                                "",
                                TypeStoryData.EDIT),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CInteractionsScreenPacket(chapter.getIndex(), scene.getName()));
            } catch (Exception e) {
                existingInteraction.setName(oldInteraction.getName());
                existingInteraction.setDescription(oldInteraction.getDescription());
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        }
    }

    public static void npcData(BiNpcDataPacket packet, ServerPlayer player) {
        Chapter chapter = CHAPTER_MANAGER.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;

        CharacterStory characterStory = new CharacterStory(
                packet.name(),
                packet.description(),
                "GG",
                "you found",
                "an easter egg, you like being curious don't you?",
                packet.characterModel(),
                CharacterType.NPC);
        characterStory.setMainCharacterAttribute(null);
        try {
            if (packet.typeStoryData() == TypeStoryData.ADD) {
                NarrativeCraftFile.createCharacterFolder(characterStory, scene);
                Util.broadcastPacket(
                        new BiNpcDataPacket(
                                packet.name(),
                                packet.description(),
                                packet.characterModel(),
                                packet.showNametag(),
                                chapter.getIndex(),
                                scene.getName(),
                                "",
                                TypeStoryData.ADD),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                scene.addNpc(characterStory);
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CNpcsScreenPacket(chapter.getIndex(), scene.getName()));
            } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
                CharacterStory existingCharacter = scene.getNpcByName(packet.npcName());
                if (existingCharacter == null) return;
                characterStory.setShowNametag(packet.showNametag());
                NarrativeCraftFile.updateCharacterData(existingCharacter, characterStory, scene);
                existingCharacter.setName(characterStory.getName());
                existingCharacter.setDescription(characterStory.getDescription());
                existingCharacter.setShowNametag(characterStory.showNametag());
                existingCharacter.setModel(characterStory.getModel());
                existingCharacter.setShowNametag(characterStory.showNametag());
                for (Chapter chapter1 : CHAPTER_MANAGER.getChapters()) {
                    for (Scene scene1 : chapter1.getSortedSceneList()) {
                        for (Animation animation : scene1.getAnimations()) {
                            NarrativeCraftFile.updateAnimationFile(animation);
                        }
                        NarrativeCraftFile.updateCameraAngles(scene);
                    }
                }
                Util.broadcastPacket(
                        new BiNpcDataPacket(
                                packet.name(),
                                packet.description(),
                                packet.characterModel(),
                                packet.showNametag(),
                                chapter.getIndex(),
                                scene.getName(),
                                packet.npcName(),
                                TypeStoryData.EDIT),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CNpcsScreenPacket(chapter.getIndex(), scene.getName()));
            }
        } catch (Exception e) {
            Util.sendCrashMessage(player, e);
            Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
        }
    }

    public static void subsceneData(BiSubsceneDataPacket packet, ServerPlayer player) {
        Chapter chapter = CHAPTER_MANAGER.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;

        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Subscene subscene = new Subscene(packet.name(), packet.description(), scene);
            try {
                scene.addSubscene(subscene);
                NarrativeCraftFile.updateSubsceneFile(scene);
                Util.broadcastPacket(
                        new BiSubsceneDataPacket(
                                packet.name(),
                                packet.description(),
                                chapter.getIndex(),
                                scene.getName(),
                                "",
                                TypeStoryData.ADD),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CSubscenesScreenPacket(chapter.getIndex(), scene.getName()));
            } catch (Exception e) {
                scene.removeSubscene(subscene);
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Subscene existingSubscene = scene.getSubsceneByName(packet.subsceneName());
            if (existingSubscene == null) return;
            Subscene oldSubscene = new Subscene(existingSubscene.getName(), existingSubscene.getDescription(), scene);
            try {
                existingSubscene.setName(packet.name());
                existingSubscene.setDescription(packet.description());
                NarrativeCraftFile.updateSubsceneFile(scene);
                for (Chapter chapter1 : CHAPTER_MANAGER.getChapters()) {
                    for (Scene scene1 : chapter1.getSortedSceneList()) {
                        NarrativeCraftFile.updateCutsceneFile(scene1);
                    }
                }
                Util.broadcastPacket(
                        new BiSubsceneDataPacket(
                                packet.name(),
                                packet.description(),
                                chapter.getIndex(),
                                scene.getName(),
                                packet.subsceneName(),
                                TypeStoryData.EDIT),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(
                        player, new S2CSubscenesScreenPacket(chapter.getIndex(), scene.getName()));
            } catch (Exception e) {
                existingSubscene.setName(oldSubscene.getName());
                existingSubscene.setDescription(oldSubscene.getDescription());
                Util.sendCrashMessage(player, e);
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.none());
            }
        }
    }
}
