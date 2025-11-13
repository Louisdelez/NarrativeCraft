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

package fr.loudo.narrativecraft.narrative.story;

import com.bladecoder.ink.runtime.Story;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.controllers.AbstractController;
import fr.loudo.narrativecraft.controllers.interaction.InteractionController;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.hud.StoryDebugHud;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.AreaTrigger;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.CharacterInteraction;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.EntityInteraction;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.StitchInteraction;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.dialog.*;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagHandlerException;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.inkAction.GameplayInkAction;
import fr.loudo.narrativecraft.options.NarrativeClientOption;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.screens.components.CrashScreen;
import fr.loudo.narrativecraft.screens.credits.CreditScreen;
import fr.loudo.narrativecraft.screens.story.StoryChoicesScreen;
import fr.loudo.narrativecraft.util.InkUtil;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class StoryHandler {

    private final NarrativeWorldOption worldOption =
            NarrativeCraftMod.getInstance().getNarrativeWorldOption();
    private final NarrativeClientOption clientOption =
            NarrativeCraftMod.getInstance().getNarrativeClientOptions();
    private final Minecraft minecraft = Minecraft.getInstance();
    public static final String DIALOG_REGEX = "^(\\w+)\\s*:\\s*(.+?)\\s*$";

    private final PlayerSession playerSession;
    private final StoryDebugHud storyDebugHud;
    private DialogData dialogData = new DialogData(DialogData.globalDialogData);
    private Story story;
    private String dialogText;
    private boolean loadScene, debugMode, firstLoad, hasError;

    public StoryHandler(PlayerSession playerSession) {
        this.playerSession = playerSession;
        Chapter firstChapter =
                NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(1);
        playerSession.setChapter(firstChapter);
        playerSession.setScene(firstChapter.getSortedSceneList().get(0));
        storyDebugHud = new StoryDebugHud(playerSession);
    }

    public StoryHandler(Chapter chapter, PlayerSession playerSession) {
        this.playerSession = playerSession;
        playerSession.setChapter(chapter);
        playerSession.setScene(chapter.getSortedSceneList().get(0));
        storyDebugHud = new StoryDebugHud(playerSession);
    }

    public StoryHandler(Chapter chapter, Scene scene, PlayerSession playerSession) {
        this.playerSession = playerSession;
        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
        loadScene = true;
        storyDebugHud = new StoryDebugHud(playerSession);
    }

    public boolean isRunning() {
        return story != null;
    }

    public boolean isFinished() {
        return !story.canContinue()
                && story.getCurrentChoices().isEmpty()
                && !story.hasError()
                && !hasError
                && !playerSession.isOnGameplay();
    }

    public void start() {
        if (playerSession.getController() != null) {
            playerSession.getController().stopSession(false);
        }
        playerSession.getAreaTriggersEntered().clear();
        playerSession.setLastAreaTriggerEntered(null);
        playerSession.setStoryHandler(this);
        minecraft.options.hideGui = false;
        firstLoad = true;
        try {
            story = new Story(NarrativeCraftFile.storyContent());
            story.onError = (s, errorType) -> {
                NarrativeCraftMod.server.execute(this::stop);
                showCrash(new Exception(errorType + " " + s));
                hasError = true;
            };
            if (NarrativeCraftFile.saveExists() && !debugMode) {
                loadSave();
                if (!loadScene) {
                    firstLoad = false;
                    return;
                }
            }
            if (loadScene) {
                loadScene(playerSession.getScene());
            }
            next();
        } catch (Exception e) {
            stop();
            showCrash(e);
        }
    }

    public void stop() {
        playerSession.getInkTagHandler().stopAll();
        playerSession.setLastAreaTriggerEntered(null);
        for (InkAction inkAction : playerSession.getInkActions()) {
            inkAction.stop();
        }
        for (InteractionController interactionController : playerSession.getInteractionControllers()) {
            interactionController.stopSession(false);
        }
        playerSession.getInkActions().clear();
        for (Playback playback : playerSession.getPlaybackManager().getPlaybacks()) {
            playback.stop(true);
        }
        for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
            if (characterRuntime.getEntity() == null
                    || characterRuntime.getEntity().isRemoved()) continue;
            characterRuntime.getEntity().remove(Entity.RemovalReason.KILLED);
        }
        AbstractController controller = playerSession.getController();
        if (controller != null) {
            controller.stopSession(false);
        }
        playerSession.setCurrentCamera(null);
        playerSession.setDialogRenderer(null);
        playerSession.setStoryHandler(null);
        playerSession.getCharacterRuntimes().clear();
        playerSession.getAreaTriggersEntered().clear();
        playerSession.setLastAreaTriggerEntered(null);
        playerSession.getInteractionControllers().clear();
        playerSession.getInkTagHandler().getTagsToExecute().clear();
    }

    public void stopAndFinishScreen() {
        stop();
        if (worldOption.showCreditsScreen && !debugMode) {
            CreditScreen creditScreen = new CreditScreen(playerSession, false, !worldOption.finishedStory);
            minecraft.execute(() -> minecraft.setScreen(creditScreen));
            worldOption.finishedStory = true;
            NarrativeCraftFile.updateWorldOptions(worldOption);
        }
    }

    public void playStitch(String stitch) {
        try {
            story.choosePathString(playerSession.getScene().knotName() + "." + stitch);
            next();
            if (playerSession.getCurrentCamera() != null) {
                playerSession.getInkActions().removeIf(inkAction -> inkAction instanceof GameplayInkAction);
            }
        } catch (Exception e) {
            stop();
            showCrash(e);
        }
    }

    public boolean characterInStory(CharacterStory characterStory) {
        for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
            if (characterRuntime.getCharacterStory().getName().equals(characterStory.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<CharacterRuntime> getCharacterRuntimeFromCharacter(CharacterStory characterStory) {
        List<CharacterRuntime> characterRuntimes = new ArrayList<>();
        for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
            if (characterRuntime.getCharacterStory().getName().equals(characterStory.getName())) {
                characterRuntimes.add(characterRuntime);
            }
        }
        return characterRuntimes;
    }

    public void killCharacter(CharacterStory characterStory) {
        CharacterRuntime toRemove = null;
        for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
            if (characterRuntime.getCharacterStory().getName().equals(characterStory.getName())) {
                if (characterRuntime.getEntity() == null) continue;
                characterRuntime.getEntity().remove(Entity.RemovalReason.KILLED);
                toRemove = characterRuntime;
            }
        }
        playerSession.getCharacterRuntimes().remove(toRemove);
    }

    public void chooseChoiceAndNext(int choiceIndex) {
        try {
            story.chooseChoiceIndex(choiceIndex);
            playerSession.setDialogRenderer(null);
            next();
            if (dialogText.isEmpty() && !isFinished() && story.getCurrentTags().isEmpty()) {
                throw new Exception("Empty dialog after a choice cannot be rendered!");
            }
        } catch (Exception e) {
            stop();
            NarrativeCraftMod.LOGGER.error("Can't choose the choice: ", e);
            Util.sendCrashMessage(minecraft.player, e);
        }
    }

    public void next() {
        try {
            if (story == null) throw new Exception("Story is not initialized!");
            DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
            if (isFinished()) {
                if (dialogRenderer != null) {
                    dialogRenderer.setRunDialogStopped(this::stopAndFinishScreen);
                    dialogRenderer.stop();
                } else {
                    stopAndFinishScreen();
                }
                return;
            }
            if (!story.getCurrentChoices().isEmpty()) {
                handleChoices();
                return;
            }
            dialogText = story.Continue().replace("\n", "");
            dialogText = dialogText.replace(
                    "__username__", playerSession.getPlayer().getName().getString());
            dialogText = InkUtil.parseVariables(story, dialogText);
            if (story.hasError() || hasError) return;
            if (firstLoad) {
                save(false);
                firstLoad = false;
            }
            playerSession.getInkTagHandler().getTagsToExecute().addAll(story.getCurrentTags());
            if (isTransitioning() && dialogRenderer != null) {
                dialogRenderer.stop();
                return;
            }
            if (dialogText.isEmpty() && !story.getCurrentChoices().isEmpty() && dialogRenderer != null) {
                playerSession.getInkTagHandler().setRun(() -> {
                    playerSession.getInkTagHandler().setRun(null);
                    showChoices();
                });
                dialogRenderer.stop();
                return;
            }
            if (dialogRenderer != null && isFinished() && dialogText.isEmpty()) {
                dialogRenderer.stop();
                return;
            }
            String currentStitch = story.getState().getCurrentStitch();
            if (currentStitch != null
                    && !currentStitch.equalsIgnoreCase(playerSession.getStitch())
                    && dialogRenderer != null) {
                playerSession.setStitch(story.getState().getCurrentStitch());
                dialogRenderer.stop();
                return;
            }
            // Handles dialog stopping animation, to executes tags AFTER the animation disappeared.
            // And for that, it checks if the new dialog character is the same as the old dialog (dialog renderer)
            if (dialogRenderer == null
                    || (sameCharacterTalking(dialogText)
                            && story.getCurrentTags().isEmpty())) {
                playerSession.getInkTagHandler().execute();
            } else {
                dialogRenderer.stop();
            }
            playerSession.setStitch(story.getState().getCurrentStitch());
            playerSession.clearKilledCharacters();
            playerSession.clearPlaybacksNotPlaying();
        } catch (Exception e) {
            stop();
            showCrash(e);
        }
    }

    public void showCurrentDialog() {
        minecraft.execute(() -> {
            try {
                showDialog(dialogText);
            } catch (Exception e) {
                stop();
                showCrash(e);
            }
        });
    }

    public Matcher getDialogMatcher(String dialog) {
        Pattern pattern = Pattern.compile(DIALOG_REGEX);
        return pattern.matcher(dialog);
    }

    private void loadSave() throws Exception {
        StorySave save = NarrativeCraftFile.saveContent();
        if (save == null) throw new Exception("Chapter or scene cannot be found in save file");
        story.getState().loadJson(save.getSaveData());
        if (loadScene) {
            return;
        }
        playerSession.setChapter(save.getChapter());
        playerSession.setScene(save.getScene());
        List<String> tags = new ArrayList<>(story.getCurrentTags());
        int subListId = 0;
        for (int i = 0; i < tags.size(); i++) {
            if ("on enter".equals(tags.get(i))) {
                subListId = i + 1;
                break;
            }
        }
        List<String> newTags = new ArrayList<>();
        if (subListId <= tags.size()) {
            newTags.addAll(tags.subList(subListId, tags.size()));
        }
        if (!newTags.isEmpty() && newTags.get(0).equalsIgnoreCase("save")) {
            newTags = newTags.subList(1, newTags.size());
        }
        newTags.addAll(save.getTagsRunning());

        story.getCurrentTags().clear();
        story.getCurrentTags().addAll(newTags);

        playerSession.getInkTagHandler().getTagsToExecute().addAll(story.getCurrentTags());
        for (CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
            characterStoryData.spawn(playerSession.getPlayer().level(), Environment.PRODUCTION);
            playerSession.getCharacterRuntimes().add(characterStoryData.getCharacterRuntime());
        }
        dialogData = save.getDialogData();
        dialogText = story.getCurrentText().replace("\n", "");
        for (String areaTriggerName : save.getAreaTriggersEnteredName()) {
            AreaTrigger areaTrigger = save.getScene().getAreaTriggerByName(areaTriggerName);
            if (areaTrigger == null) continue;
            playerSession.getAreaTriggersEntered().add(areaTrigger);
        }
        playerSession.getInkTagHandler().execute();
        if (!story.getCurrentChoices().isEmpty()) {
            handleChoices();
        }
    }

    private void loadScene(Scene scene) throws Exception {
        try {
            story.choosePathString(scene.knotName());
        } catch (Exception e) {
            // If the scene was added in-game after a save was created, then load the latest scene
            List<Scene> scenes = NarrativeCraftMod.getInstance()
                    .getChapterManager()
                    .getChapters()
                    .get(0)
                    .getSortedSceneList();
            story.choosePathString(scenes.get(scenes.size() - 1).knotName());
        }
    }

    private void handleChoices() {
        DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
        if (dialogRenderer != null) {
            dialogRenderer.stop();
            dialogRenderer.setRunDialogStopped(this::showChoices);
        } else {
            showChoices();
        }
    }

    public void showChoices() {
        playerSession.setDialogRenderer(null);
        StoryChoicesScreen choicesScreen = new StoryChoicesScreen(playerSession, true);
        minecraft.execute(() -> minecraft.setScreen(choicesScreen));
    }

    public void showCrash(Exception exception) {
        NarrativeCraftMod.LOGGER.error("Error occurred on the story: ", exception);
        if (debugMode) {
            playerSession
                    .getPlayer()
                    .sendSystemMessage(
                            Translation.message("crash.story-runtime").withStyle(ChatFormatting.RED));
            playerSession
                    .getPlayer()
                    .sendSystemMessage(Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED));
        } else {
            CrashScreen screen = new CrashScreen(playerSession, exception.getMessage());
            minecraft.execute(() -> minecraft.setScreen(screen));
        }
    }

    public boolean interactWith(Entity entity) {
        for (InteractionController interactionController : playerSession.getInteractionControllers()) {
            StitchInteraction stitchInteraction = null;
            CharacterRuntime characterRuntime = interactionController.getCharacterFromEntity(entity);
            if (characterRuntime != null) {
                CharacterInteraction characterInteraction = interactionController.getCharacterInteractionFromCharacter(
                        interactionController.getCharacterStoryDataFromEntity(characterRuntime.getEntity()));
                if (characterInteraction != null) {
                    stitchInteraction = characterInteraction;
                }
            }
            EntityInteraction entityInteraction = interactionController.getEntityInteraction(entity);
            if (entityInteraction != null) {
                stitchInteraction = entityInteraction;
            }
            if (stitchInteraction != null) {
                if (stitchInteraction.getStitch().isEmpty()) {
                    return false;
                }
                boolean sameInteraction = (playerSession.getLastInteraction() != null
                        && playerSession.getLastInteraction().getStitch().equals(stitchInteraction.getStitch()));
                if (playerSession.getDialogRenderer() != null) {
                    if (sameInteraction) return true;
                    playerSession.setDialogRenderer(null);
                }
                playerSession.setLastInteraction(stitchInteraction);
                playStitch(stitchInteraction.getStitch());
                return true;
            }
        }
        return false;
    }

    public void save(boolean showLogo) {
        try {
            if (!debugMode) {
                StorySave save = new StorySave(playerSession);
                NarrativeCraftFile.writeSave(save);
            }
            if (showLogo) {
                playerSession.getStorySaveIconGui().showSave(debugMode);
            }
        } catch (Exception e) {
            stop();
            showCrash(e);
        }
    }

    public boolean isChangingScene() {
        String currentKnot = story.getState().getCurrentKnot();
        return currentKnot != null
                && !currentKnot.equalsIgnoreCase(playerSession.getScene().knotName());
    }

    private void showDialog(String dialog) throws Exception {
        if (dialog == null || dialog.isEmpty()) return;
        Matcher matcher = getDialogMatcher(dialog);
        CharacterStory characterStory = null;
        if (matcher.matches()) {
            String characterName = matcher.group(1).trim();
            characterStory =
                    NarrativeCraftMod.getInstance().getCharacterManager().getCharacterByName(characterName);
            if (characterStory == null) {
                characterStory = playerSession.getScene().getNpcByName(characterName);
            }
            if (characterStory == null) {
                throw new Exception("Character " + characterName + " was not found.");
            }
            if (!characterInStory(characterStory)) {
                throw new Exception("Character " + characterName + " is not in the story, can't render the dialog.");
            }
            dialog = matcher.group(2).trim() + "\n";
        }
        DialogRenderer newDialogRenderer = getDialogRenderer(dialog, characterStory);
        DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
        if (dialogRenderer == null || newDialogRenderer.getClass() != dialogRenderer.getClass()) {
            playerSession.setDialogRenderer(newDialogRenderer);
            newDialogRenderer.setRunDialogStopped(() -> {
                // Runnable task to execute the stuff AFTER the stopping animation ended.
                // If the next action is only a new dialog, then show the new dialog
                // Or else, executes the tags, and after it show the dialog (inside ink tag handler)
                playerSession.setDialogRenderer(null);
                try {
                    if (playerSession.getInkTagHandler().getTagsToExecute().isEmpty()) {
                        showCurrentDialog();
                    } else {
                        playerSession.getInkTagHandler().execute();
                    }
                } catch (Exception e) {
                    stop();
                    if (!debugMode && e instanceof InkTagHandlerException) {
                        CrashScreen screen = new CrashScreen(playerSession, e.getMessage());
                        minecraft.execute(() -> minecraft.setScreen(screen));
                    } else {
                        Util.sendCrashMessage(playerSession.getPlayer(), e);
                    }
                }
            });
            newDialogRenderer.setRunDialogAutoSkipped(this::next);
            newDialogRenderer.start();
        } else {
            configureAutoSkip(dialogRenderer, dialog);
            dialogRenderer.autoSkipAt(dialogData.getAutoSkipSeconds());
            dialogRenderer.setText(dialog);
            dialogRenderer.update();
        }
    }

    private boolean sameCharacterTalking(String dialog) {
        DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
        Matcher matcher = getDialogMatcher(dialog);
        if (dialog.isEmpty()) return false;
        if (dialogRenderer instanceof DialogRenderer2D && !matcher.matches()) return true;
        if (!(dialogRenderer instanceof DialogRenderer3D)) return false;
        String characterName = "";
        if (matcher.matches()) {
            characterName = matcher.group(1).trim();
        }
        return ((DialogRenderer3D) dialogRenderer)
                .getCharacterRuntime()
                .getCharacterStory()
                .getName()
                .equalsIgnoreCase(characterName);
    }

    private DialogRenderer getDialogRenderer(String dialog, CharacterStory characterStory) throws Exception {
        DialogRenderer dialogRenderer;
        if (characterStory != null) {
            List<CharacterRuntime> characterRuntimes = playerSession.getCharacterRuntimesByCharacter(characterStory);
            if (characterRuntimes.isEmpty())
                throw new Exception("Dialog cannot be instantiated as " + characterStory.getName()
                        + " is not present in the world!");
            dialogRenderer =
                    new DialogRenderer3D(dialog, characterStory.getName(), dialogData, characterRuntimes.get(0));
            DialogRenderer3D dialogRenderer3D = (DialogRenderer3D) dialogRenderer;
            dialogRenderer3D.setDialogEntityBobbing(new DialogEntityBobbing(
                    dialogRenderer3D, dialogData.getNoiseShakeSpeed(), dialogData.getNoiseShakeStrength()));
        } else {
            dialogRenderer = new DialogRenderer2D(dialog, 350, 400, 90, 30, dialogData);
        }
        configureAutoSkip(dialogRenderer, dialog);
        dialogRenderer.autoSkipAt(dialogData.getAutoSkipSeconds());
        dialogRenderer.setNoSkip(dialogData.isNoSkip());
        return dialogRenderer;
    }

    private void configureAutoSkip(DialogRenderer dialogRenderer, String dialog) {
        dialogRenderer.stopAutoSkip();
        if (clientOption.autoSkip) {
            String[] words = dialog.trim().split("\\s+");
            int wordCount = words.length;
            double readingSpeed = 3.0;
            int autoSkipTime = (int) Math.ceil(wordCount / readingSpeed);
            autoSkipTime = Math.max(autoSkipTime, 2);
            dialogRenderer.autoSkipAt(autoSkipTime);
        }
    }

    private boolean isTransitioning() {
        if (story.getState().getCurrentKnot() == null) return false;
        return !story.getState()
                        .getCurrentKnot()
                        .equals(playerSession.getScene().knotName())
                && story.getState().getCurrentKnot().matches(InkUtil.SCENE_KNOT_PATTERN.pattern());
    }

    public DialogData getDialogData() {
        return dialogData;
    }

    public void setDialogData(DialogData dialogData) {
        this.dialogData = dialogData;
    }

    public Story getStory() {
        return story;
    }

    public String getDialogText() {
        return dialogText;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public StoryDebugHud getStoryDebugHud() {
        return storyDebugHud;
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }
}
