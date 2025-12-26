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
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.network.*;
import fr.loudo.narrativecraft.network.data.BiChapterDataPacket;
import fr.loudo.narrativecraft.network.data.BiSceneDataPacket;
import fr.loudo.narrativecraft.network.data.TypeStoryData;
import fr.loudo.narrativecraft.network.storyDataSyncs.*;
import fr.loudo.narrativecraft.screens.storyManager.chapter.ChaptersScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesScreen;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {

    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final ChapterManager CHAPTER_MANAGER =
            NarrativeCraftMod.getInstance().getChapterManager();

    public static void screenHandler(final S2CScreenPacket packet) {
        switch (packet.screenType()) {
            case STORY_MANAGER -> minecraft.setScreen(new ChaptersScreen());
            case NONE -> minecraft.setScreen(null);
        }
    }

    public static void openSceneScreen(final S2CSceneScreenPacket packet) {
        Chapter chapter = CHAPTER_MANAGER.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        minecraft.setScreen(new ScenesScreen(chapter));
    }

    public static void syncChaptersHandler(final S2CSyncChaptersPacket packet) {
        CHAPTER_MANAGER.getChapters().clear();
        for (Chapter chapter : packet.chapters()) {
            CHAPTER_MANAGER.addChapter(chapter);
        }
    }

    public static void syncScenesHandler(final S2CSyncScenesPacket packet) {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        Chapter chapter = chapterManager.getChapterByIndex(packet.chapterIndex());
        if (chapter != null) {
            chapter.getScenes().clear();
            for (Scene scene : packet.scenes()) {
                scene.setChapter(chapter);
                chapter.addScene(scene);
            }
        }
    }

    public static void syncAnimationsHandler(final S2CSyncAnimationsPacket packet) {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        Chapter chapter = chapterManager.getChapterByIndex(packet.chapterIndex());
        if (chapter != null) {
            Scene scene = chapter.getSceneByName(packet.sceneName());
            if (scene != null) {
                scene.getAnimations().clear();
                for (Animation animation : packet.animations()) {
                    animation.setScene(scene);
                    scene.addAnimation(animation);
                }
            }
        }
    }

    public static void syncSubscenesHandler(final S2CSyncSubscenesPacket packet) {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        Chapter chapter = chapterManager.getChapterByIndex(packet.chapterIndex());
        if (chapter != null) {
            Scene scene = chapter.getSceneByName(packet.sceneName());
            if (scene != null) {
                scene.getSubscenes().clear();
                for (Subscene subscene : packet.subscenes()) {
                    subscene.setScene(scene);
                    scene.addSubscene(subscene);
                }
            }
        }
    }

    public static void syncCutscenesHandler(final S2CSyncCutscenesPacket packet) {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        Chapter chapter = chapterManager.getChapterByIndex(packet.chapterIndex());
        if (chapter != null) {
            Scene scene = chapter.getSceneByName(packet.sceneName());
            if (scene != null) {
                scene.getCutscenes().clear();
                for (Cutscene cutscene : packet.cutscenes()) {
                    cutscene.setScene(scene);
                    scene.addCutscene(cutscene);
                }
            }
        }
    }

    public static void syncInteractionsHandler(final S2CSyncInteractionsPacket packet) {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        Chapter chapter = chapterManager.getChapterByIndex(packet.chapterIndex());
        if (chapter != null) {
            Scene scene = chapter.getSceneByName(packet.sceneName());
            if (scene != null) {
                scene.getInteractions().clear();
                for (Interaction interaction : packet.interactions()) {
                    interaction.setScene(scene);
                    scene.addInteraction(interaction);
                }
            }
        }
    }

    public static void syncNpcsHandler(final S2CSyncNpcsPacket packet) {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        Chapter chapter = chapterManager.getChapterByIndex(packet.chapterIndex());
        if (chapter != null) {
            Scene scene = chapter.getSceneByName(packet.sceneName());
            if (scene != null) {
                scene.getNpcs().clear();
                for (CharacterStory npc : packet.npcs()) {
                    scene.addNpc(npc);
                }
            }
        }
    }

    public static void syncCameraAnglesHandler(S2CSyncCameraAnglesPacket packet) {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        Chapter chapter = chapterManager.getChapterByIndex(packet.chapterIndex());
        if (chapter != null) {
            Scene scene = chapter.getSceneByName(packet.sceneName());
            if (scene != null) {
                scene.getCameraAngles().clear();
                for (CameraAngle cameraAngle : packet.cameraAngles()) {
                    cameraAngle.setScene(scene);
                    scene.addCameraAngleGroup(cameraAngle);
                }
            }
        }
    }

    public static void chapterData(BiChapterDataPacket packet) {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Chapter chapter = new Chapter(
                    packet.name(),
                    packet.description(),
                    chapterManager.getChapters().size() + 1);
            if (chapterManager.chapterExists(chapter.getIndex())) return;
            chapterManager.addChapter(chapter);
        }
    }

    public static void sceneData(BiSceneDataPacket packet) {
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Scene scene = new Scene(packet.name(), packet.description(), chapter);
            chapter.addScene(scene);
        }
    }
}
