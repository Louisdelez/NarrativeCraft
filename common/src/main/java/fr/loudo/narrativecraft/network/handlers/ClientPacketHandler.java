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

import fr.loudo.narrativecraft.client.NarrativeCraftModClient;
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
import fr.loudo.narrativecraft.network.storyDataSyncs.*;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.animations.AnimationsScreen;
import fr.loudo.narrativecraft.screens.storyManager.cameraAngle.CameraAngleScreen;
import fr.loudo.narrativecraft.screens.storyManager.chapter.ChaptersScreen;
import fr.loudo.narrativecraft.screens.storyManager.character.CharactersScreen;
import fr.loudo.narrativecraft.screens.storyManager.cutscene.CutscenesScreen;
import fr.loudo.narrativecraft.screens.storyManager.interaction.InteractionsScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesScreen;
import fr.loudo.narrativecraft.screens.storyManager.subscene.SubscenesScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {

    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final ChapterManager CHAPTER_MANAGER_CLIENT =
            NarrativeCraftModClient.getInstance().getChapterManager();
    private static final CharacterManager CHARACTER_MANAGER_CLIENT =
            NarrativeCraftModClient.getInstance().getCharacterManager();

    public static void screenHandler(final S2CScreenPacket packet) {
        switch (packet.screenType()) {
            case STORY_MANAGER -> minecraft.setScreen(new ChaptersScreen());
            case CHARACTER_MANAGER -> minecraft.setScreen(new CharactersScreen(null));
            case NONE -> minecraft.setScreen(null);
        }
    }

    public static void openSceneScreen(final S2CSceneScreenPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        minecraft.setScreen(new ScenesScreen(chapter));
    }

    public static void openAnimationsScreen(S2CAnimationsScreenPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;

        minecraft.setScreen(new AnimationsScreen(scene));
    }

    public static void openCameraAnglesScreen(S2CCameraAnglesScreenPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;

        minecraft.setScreen(new CameraAngleScreen(scene));
    }

    public static void openCutscenesScreen(S2CCutscenesScreenPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;

        minecraft.setScreen(new CutscenesScreen(scene));
    }

    public static void openInteractionScreen(S2CInteractionsScreenPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;

        minecraft.setScreen(new InteractionsScreen(scene));
    }

    public static void openNpcsScreen(S2CNpcsScreenPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;

        minecraft.setScreen(new CharactersScreen(scene));
    }

    public static void openSubscenesScreen(S2CSubscenesScreenPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;

        minecraft.setScreen(new SubscenesScreen(scene));
    }

    public static void syncChaptersHandler(final S2CSyncChaptersPacket packet) {
        CHAPTER_MANAGER_CLIENT.getChapters().clear();
        for (Chapter chapter : packet.chapters()) {
            CHAPTER_MANAGER_CLIENT.addChapter(chapter);
        }
    }

    public static void syncScenesHandler(final S2CSyncScenesPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter != null) {
            chapter.getScenes().clear();
            for (Scene scene : packet.scenes()) {
                scene.setChapter(chapter);
                chapter.addScene(scene);
            }
        }
    }

    public static void syncAnimationsHandler(final S2CSyncAnimationsPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
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
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
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
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
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
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
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
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
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

    public static void syncCharactersHandler(final S2CSyncCharactersPacket packet) {
        CHARACTER_MANAGER_CLIENT.getCharacterStories().clear();
        for (CharacterStory character : packet.characters()) {
            CHARACTER_MANAGER_CLIENT.addCharacter(character);
        }
    }

    public static void syncCameraAnglesHandler(S2CSyncCameraAnglesPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
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
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Chapter chapter = new Chapter(
                    packet.name(),
                    packet.description(),
                    CHAPTER_MANAGER_CLIENT.getChapters().size() + 1);
            if (CHAPTER_MANAGER_CLIENT.chapterExists(chapter.getIndex())) return;
            CHAPTER_MANAGER_CLIENT.addChapter(chapter);
            updateStoryElementScreen();
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByName(packet.chapterName());
            if (chapter == null) return;
            chapter.setName(packet.name());
            chapter.setDescription(packet.description());
        } else if (packet.typeStoryData() == TypeStoryData.REMOVE) {
            Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByName(packet.chapterName());
            if (chapter == null) return;
            CHAPTER_MANAGER_CLIENT.removeChapter(chapter);
            updateStoryElementScreen();
        }
    }

    public static void sceneData(BiSceneDataPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Scene scene = new Scene(packet.name(), packet.description(), chapter);
            chapter.addScene(scene);
            updateStoryElementScreen();
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Scene scene = chapter.getSceneByName(packet.sceneName());
            if (scene == null) return;
            scene.setName(packet.name());
            scene.setDescription(packet.description());
            chapter.setSceneRank(scene, packet.rank());
        } else if (packet.typeStoryData() == TypeStoryData.REMOVE) {
            Scene scene = chapter.getSceneByName(packet.sceneName());
            if (scene == null) return;
            chapter.removeScene(scene);
            updateStoryElementScreen();
        }
    }

    public static void animationData(BiAnimationDataPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;
        if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Animation animation = scene.getAnimationByName(packet.animationName());
            if (animation == null) return;
            animation.setName(packet.name());
            animation.setDescription(packet.description());
        } else if (packet.typeStoryData() == TypeStoryData.REMOVE) {
            Animation animation = scene.getAnimationByName(packet.animationName());
            if (animation == null) return;
            scene.removeAnimation(animation);
            updateStoryElementScreen();
        }
    }

    public static void cameraAngleData(BiCameraAngleDataPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            CameraAngle cameraAngle = new CameraAngle(packet.name(), packet.description(), scene);
            scene.addCameraAngleGroup(cameraAngle);
            updateStoryElementScreen();
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            CameraAngle cameraAngle = scene.getCameraAngleByName(packet.cameraAngleName());
            if (cameraAngle == null) return;
            cameraAngle.setName(packet.name());
            cameraAngle.setDescription(packet.description());
        } else if (packet.typeStoryData() == TypeStoryData.REMOVE) {
            CameraAngle cameraAngle = scene.getCameraAngleByName(packet.cameraAngleName());
            if (cameraAngle == null) return;
            scene.removeCameraAngleGroup(cameraAngle);
            updateStoryElementScreen();
        }
    }

    public static void cutsceneData(BiCutsceneDataPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Cutscene cutscene = new Cutscene(packet.name(), packet.description(), scene);
            scene.addCutscene(cutscene);
            updateStoryElementScreen();
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Cutscene cutscene = scene.getCutsceneByName(packet.cutsceneName());
            if (cutscene == null) return;
            cutscene.setName(packet.name());
            cutscene.setDescription(packet.description());
        } else if (packet.typeStoryData() == TypeStoryData.REMOVE) {
            Cutscene cutscene = scene.getCutsceneByName(packet.cutsceneName());
            if (cutscene == null) return;
            scene.removeCutscene(cutscene);
            updateStoryElementScreen();
        }
    }

    public static void interactionData(BiInteractionDataPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Interaction interaction = new Interaction(packet.name(), packet.description(), scene);
            scene.addInteraction(interaction);
            updateStoryElementScreen();
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Interaction interaction = scene.getInteractionByName(packet.interactionName());
            if (interaction == null) return;
            interaction.setName(packet.name());
            interaction.setDescription(packet.description());
        } else if (packet.typeStoryData() == TypeStoryData.REMOVE) {
            Interaction interaction = scene.getInteractionByName(packet.interactionName());
            if (interaction == null) return;
            scene.removeInteraction(interaction);
            updateStoryElementScreen();
        }
    }

    public static void npcData(BiNpcDataPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            CharacterStory characterStory =
                    new CharacterStory(packet.name(), packet.description(), CharacterType.NPC, packet.characterModel());
            scene.addNpc(characterStory);
            updateStoryElementScreen();
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            CharacterStory characterStory = scene.getNpcByName(packet.npcName());
            if (characterStory == null) return;
            characterStory.setName(packet.name());
            characterStory.setDescription(packet.description());
            characterStory.setModel(packet.characterModel());
            characterStory.setShowNametag(packet.showNametag());
        } else if (packet.typeStoryData() == TypeStoryData.REMOVE) {
            CharacterStory characterStory = scene.getNpcByName(packet.npcName());
            if (characterStory == null) return;
            scene.removeNpc(characterStory);
            updateStoryElementScreen();
        }
    }

    public static void subsceneData(BiSubsceneDataPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            Subscene subscene = new Subscene(packet.name(), packet.description(), scene);
            scene.addSubscene(subscene);
            updateStoryElementScreen();
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            Subscene subscene = scene.getSubsceneByName(packet.subsceneName());
            if (subscene == null) return;
            subscene.setName(packet.name());
            subscene.setDescription(packet.description());
        } else if (packet.typeStoryData() == TypeStoryData.REMOVE) {
            Subscene subscene = scene.getSubsceneByName(packet.subsceneName());
            if (subscene == null) return;
            scene.removeSubscene(subscene);
            updateStoryElementScreen();
        }
    }

    public static void characterData(BiCharacterDataPacket packet) {
        if (packet.typeStoryData() == TypeStoryData.ADD) {
            CharacterStory characterStory = new CharacterStory(
                    packet.name(),
                    packet.description(),
                    packet.day(),
                    packet.month(),
                    packet.year(),
                    packet.characterModel(),
                    CharacterType.MAIN);
            characterStory.getMainCharacterAttribute().setMainCharacter(packet.mainCharacter());
            characterStory.getMainCharacterAttribute().setSameSkinAsPlayer(packet.sameSkinAsPlayer());
            characterStory.getMainCharacterAttribute().setSameSkinAsTheir(packet.sameSkinAsTheir());
            CHARACTER_MANAGER_CLIENT.addCharacter(characterStory);
            updateStoryElementScreen();
        } else if (packet.typeStoryData() == TypeStoryData.EDIT) {
            CharacterStory characterStory = CHARACTER_MANAGER_CLIENT.getCharacterByName(packet.characterName());
            if (characterStory == null) return;
            CharacterStory currentMainCharacter = CHARACTER_MANAGER_CLIENT.getMainCharacter();
            characterStory.setName(packet.name());
            characterStory.setDescription(packet.description());
            characterStory.setModel(packet.characterModel());
            characterStory.setShowNametag(packet.showNametag());
            characterStory.getMainCharacterAttribute().setMainCharacter(packet.mainCharacter());
            characterStory.getMainCharacterAttribute().setSameSkinAsPlayer(packet.sameSkinAsPlayer());
            characterStory.getMainCharacterAttribute().setSameSkinAsTheir(packet.sameSkinAsTheir());
            if (currentMainCharacter != null
                    && characterStory.getMainCharacterAttribute().isMainCharacter()
                    && !currentMainCharacter.getName().equalsIgnoreCase(packet.name())) {
                currentMainCharacter.getMainCharacterAttribute().setMainCharacter(false);
            }
        } else if (packet.typeStoryData() == TypeStoryData.REMOVE) {
            CharacterStory characterStory = CHARACTER_MANAGER_CLIENT.getCharacterByName(packet.characterName());
            if (characterStory == null) return;
            CHARACTER_MANAGER_CLIENT.removeCharacter(characterStory);
            updateStoryElementScreen();
        }
    }

    private static void updateStoryElementScreen() {
        if (minecraft.screen instanceof StoryElementScreen screen) {
            screen.reload();
        }
    }

    public static void subsceneAnimationLinkData(BiSubsceneAnimationLinkDataPacket packet) {
        Chapter chapter = CHAPTER_MANAGER_CLIENT.getChapterByIndex(packet.chapterIndex());
        if (chapter == null) return;
        Scene scene = chapter.getSceneByName(packet.sceneName());
        if (scene == null) return;
        Subscene subscene = scene.getSubsceneByName(packet.subsceneName());
        if (subscene == null) return;

        List<Animation> newAnimations = new ArrayList<>();
        for (String animName : packet.animationNames()) {
            Animation anim = scene.getAnimationByName(animName);
            if (anim != null) {
                newAnimations.add(anim);
            }
        }
        subscene.getAnimations().clear();
        subscene.getAnimations().addAll(newAnimations);

        updateStoryElementScreen();
    }
}
