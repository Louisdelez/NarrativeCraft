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
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.dialog.*;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.story.StoryChoicesScreen;
import fr.loudo.narrativecraft.util.Util;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;

public class StoryHandler {

    private final Minecraft minecraft = Minecraft.getInstance();
    public static final String DIALOG_REGEX = "^(\\w+)\\s*:\\s*(.+?)\\s*$";

    private final PlayerSession playerSession;
    private DialogData dialogData =
            new DialogData(new Vec2(0, 0.8F), 90, 5, 5, 0.4F, 0, 0, 0, -1, 2.9F, 2.15F, false, false, 0.0);
    private Story story;
    private String dialogText;

    public StoryHandler(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    public StoryHandler(Chapter chapter, PlayerSession playerSession) {
        this.playerSession = playerSession;
        playerSession.setChapter(chapter);
        playerSession.setScene(chapter.getSortedSceneList().getFirst());
    }

    public StoryHandler(Chapter chapter, Scene scene, PlayerSession playerSession) {
        this.playerSession = playerSession;
        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
    }

    public boolean isRunning() {
        return story != null;
    }

    public boolean isFinished() {
        return !story.canContinue() && story.getCurrentChoices().isEmpty();
    }

    public void start() {
        playerSession.setStoryHandler(this);
        try {
            story = new Story(NarrativeCraftFile.storyContent());
            if (playerSession.isSessionSet() && NarrativeCraftFile.saveExists()) {
                loadSave();
                return;
            }
            next();
        } catch (Exception e) {
            stop();
            NarrativeCraftMod.LOGGER.error("Can't start the story: ", e);
            Util.sendCrashMessage(minecraft.player, e);
        }
    }

    public void stop() {
        playerSession.getInkTagHandler().stopAll();
        for (InkAction inkAction : playerSession.getInkActions()) {
            inkAction.stop();
        }
        playerSession.getInkActions().clear();
        for (Playback playback : playerSession.getPlaybackManager().getPlaybacks()) {
            playback.stop(true);
        }
        for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
            if (characterRuntime.getEntity() == null
                    || !characterRuntime.getEntity().isAlive()) continue;
            characterRuntime.getEntity().remove(Entity.RemovalReason.KILLED);
        }
        AbstractController controller = playerSession.getController();
        if (controller != null) {
            controller.stopSession(false);
        }
        playerSession.setCurrentCamera(null);
        playerSession.getCharacterRuntimes().clear();
        playerSession.setDialogRenderer(null);
        playerSession.getInkTagHandler().getTagsToExecute().clear();
        playerSession.setStoryHandler(null);
    }

    public boolean characterInStory(CharacterStory characterStory) {
        for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
            if (characterRuntime.getCharacterStory().getName().equals(characterStory.getName())) {
                return true;
            }
        }
        return false;
    }

    public CharacterRuntime getCharacterRuntimeFromCharacter(CharacterStory characterStory) {
        for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
            if (characterRuntime.getCharacterStory().getName().equals(characterStory.getName())) {
                return characterRuntime;
            }
        }
        return null;
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
            if (dialogText.isEmpty() && !isFinished()) {
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
            if (isFinished()) {
                stop();
                return;
            }
            DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
            dialogText = story.Continue().trim();
            playerSession.getInkTagHandler().getTagsToExecute().addAll(story.getCurrentTags());
            if (!story.getCurrentChoices().isEmpty()) {
                handleChoices();
                return;
            }
            // Handles dialog stopping animation, to executes tags AFTER the animation disappeared.
            // And for that, it checks if the new dialog character is the same as the old dialog (dialog renderer)
            if (dialogRenderer == null || sameCharacterTalking(dialogText)) {
                playerSession.getInkTagHandler().execute();
            } else {
                dialogRenderer.stop();
            }
        } catch (Exception e) {
            stop();
            NarrativeCraftMod.LOGGER.error("Can't continue the story: ", e);
            Util.sendCrashMessage(minecraft.player, e);
        }
    }

    public void showCurrentDialog() {
        try {
            showDialog(dialogText);
        } catch (Exception e) {
            stop();
            NarrativeCraftMod.LOGGER.error("Can't show current dialog: ", e);
            Util.sendCrashMessage(minecraft.player, e);
        }
    }

    public Matcher getDialogMatcher(String dialog) {
        Pattern pattern = Pattern.compile(DIALOG_REGEX);
        return pattern.matcher(dialog);
    }

    private void loadSave() throws Exception {
        StorySave save = NarrativeCraftFile.saveContent();
        if (save == null) throw new Exception("Chapter or scene cannot be found in save file");
        story.getState().loadJson(save.getSaveData());
        playerSession.setChapter(save.getChapter());
        playerSession.setScene(save.getScene());
        removeForbiddenTagLoadSave();
        story.getCurrentTags().addAll(save.getTagsRunning());
        playerSession.getInkTagHandler().getTagsToExecute().addAll(story.getCurrentTags());
        for (CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
            characterStoryData.spawn(playerSession.getPlayer().level(), Environment.PRODUCTION);
            playerSession.getCharacterRuntimes().add(characterStoryData.getCharacterRuntime());
        }
        dialogData = save.getDialogData();
        dialogText = story.getCurrentText();
        playerSession.setCurrentCamera(save.getCameraLocation());
        playerSession.getInkTagHandler().execute();
        if (!story.getCurrentChoices().isEmpty()) {
            handleChoices();
        }
    }

    private void removeForbiddenTagLoadSave() throws Exception {
        story.getCurrentTags().remove("save");
        story.getCurrentTags().remove("on enter");
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

    private void showChoices() {
        playerSession.setDialogRenderer(null);
        playerSession.getInkTagHandler().execute();
        StoryChoicesScreen choicesScreen = new StoryChoicesScreen(playerSession, true);
        minecraft.execute(() -> minecraft.setScreen(choicesScreen));
    }

    public void save() {
        try {
            StorySave save = new StorySave(playerSession);
            NarrativeCraftFile.writeSave(save);
            playerSession.getStorySaveIconGui().showSave();
        } catch (Exception e) {
            stop();
            Util.sendCrashMessage(playerSession.getPlayer(), e);
        }
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
                if (playerSession.getInkTagHandler().getTagsToExecute().isEmpty()) {
                    showCurrentDialog();
                } else {
                    playerSession.getInkTagHandler().execute();
                }
            });
            newDialogRenderer.setRunDialogAutoSkipped(this::next);
            newDialogRenderer.start();
        } else {
            dialogRenderer.setText(dialog);
            dialogRenderer.update();
        }
    }

    private boolean sameCharacterTalking(String dialog) {
        DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
        if (dialog.isEmpty()) return true;
        if (!(dialogRenderer instanceof DialogRenderer3D)) return false;
        Matcher matcher = getDialogMatcher(dialog);
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

    private DialogRenderer getDialogRenderer(String dialog, CharacterStory characterStory) {
        DialogRenderer dialogRenderer;
        if (characterStory != null) {
            CharacterRuntime characterRuntime = playerSession.getCharacterRuntimeByCharacter(characterStory);
            dialogRenderer = new DialogRenderer3D(dialog, characterStory.getName(), dialogData, characterRuntime);
            DialogRenderer3D dialogRenderer3D = (DialogRenderer3D) dialogRenderer;
            dialogRenderer3D.setDialogEntityBobbing(new DialogEntityBobbing(
                    dialogRenderer3D, dialogData.getNoiseShakeSpeed(), dialogData.getNoiseShakeStrength()));
        } else {
            dialogRenderer = new DialogRenderer2D(dialog, 350, 400, 90, 30, dialogData);
        }
        dialogRenderer.autoSkipAt(dialogData.getAutoSkipSeconds());
        dialogRenderer.setNoSkip(dialogData.isNoSkip());
        return dialogRenderer;
    }

    public DialogData getDialogData() {
        return dialogData;
    }

    public Story getStory() {
        return story;
    }

    public String getDialogText() {
        return dialogText;
    }
}
