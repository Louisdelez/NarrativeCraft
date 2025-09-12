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
import fr.loudo.narrativecraft.controllers.AbstractController;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Util;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;

public class StoryHandler {

    private final Minecraft minecraft = Minecraft.getInstance();

    private final PlayerSession playerSession;
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

    public void start() {
        playerSession.setStoryHandler(this);
        try {
            story = new Story(NarrativeCraftFile.storyContent());
            next();
        } catch (Exception e) {
            stop();
            NarrativeCraftMod.LOGGER.error("Can't start the story: ", e);
            Util.sendCrashMessage(minecraft.player, e);
        }
    }

    public void stop() {
        playerSession.getInkTagHandler().stopAll();
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

    public void next() {
        try {
            if (story == null) throw new Exception("Story is not initialized!");
            if (!story.canContinue() && story.getCurrentChoices().isEmpty()) {
                stop();
                return;
            }
            dialogText = story.Continue();
            playerSession.getInkTagHandler().getTagsToExecute().addAll(story.getCurrentTags());
            DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
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

    private void showDialog(String dialog) throws Exception {
        if (dialog.isEmpty()) return;
        Pattern pattern = Pattern.compile("^(\\w+)\\s*:\\s*(.+)$\n");
        Matcher matcher = pattern.matcher(dialog);
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
        DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
        if (dialogRenderer == null) {
            dialogRenderer = getDialogRenderer(dialog, characterStory);
            playerSession.setDialogRenderer(dialogRenderer);
            dialogRenderer.setRunDialogStopped(() -> {
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
            dialogRenderer.start();
        } else {
            dialogRenderer.setText(dialog);
            dialogRenderer.update();
        }
    }

    private boolean sameCharacterTalking(String dialog) {
        DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
        if (dialog.isEmpty() || !(dialogRenderer instanceof DialogRenderer3D)) return true;
        Pattern pattern = Pattern.compile("^(\\w+)\\s*:\\s*(.+)$\n");
        Matcher matcher = pattern.matcher(dialog);
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
        CharacterRuntime characterRuntime = playerSession.getCharacterRuntimeByCharacter(characterStory);
        DialogRenderer dialogRenderer;
        if (characterRuntime != null) {
            dialogRenderer = new DialogRenderer3D(
                    dialog, characterRuntime, new Vec2(0, 0.8F), 90, 5, 5, 0.4F, 0, 0, 0, -1, false);
        } else {
            dialogRenderer = new DialogRenderer(dialog, 90, 5, 5, 0.8F, 0, 0, 0, -1, false); // TODO: 2d dialog
        }
        return dialogRenderer;
    }
}
