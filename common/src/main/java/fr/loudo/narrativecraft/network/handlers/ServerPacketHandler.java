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
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.network.data.BiAnimationDataPacket;
import fr.loudo.narrativecraft.network.data.BiChapterDataPacket;
import fr.loudo.narrativecraft.network.data.BiSceneDataPacket;
import fr.loudo.narrativecraft.network.data.TypeStoryData;
import fr.loudo.narrativecraft.network.screen.S2CAnimationsScreenPacket;
import fr.loudo.narrativecraft.network.screen.S2CSceneScreenPacket;
import fr.loudo.narrativecraft.network.screen.S2CScreenPacket;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.server.level.ServerPlayer;

public class ServerPacketHandler {

    private static final ChapterManager CHAPTER_MANAGER =
            NarrativeCraftMod.getInstance().getChapterManager();

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
                        new BiChapterDataPacket(packet.name(), packet.description(), TypeStoryData.ADD),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(player, S2CScreenPacket.storyManager());
            } catch (Exception e) {
                chapterManager.removeChapter(chapter);
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
                                packet.name(), packet.description(), packet.chapterIndex(), TypeStoryData.ADD),
                        NarrativeCraftMod.server.getPlayerList().getPlayers());
                Services.PACKET_SENDER.sendToPlayer(player, new S2CSceneScreenPacket(chapter.getIndex()));
            } catch (Exception e) {
                chapter.removeScene(scene);
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
                    for (Scene scene1 : chapter.getSortedSceneList()) {
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
}
