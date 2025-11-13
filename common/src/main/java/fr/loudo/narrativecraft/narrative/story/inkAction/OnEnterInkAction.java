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

package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.util.InkUtil;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.entity.Entity;

public class OnEnterInkAction extends InkAction {
    public OnEnterInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        isRunning = false;
        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler == null) return InkActionResult.ignored();
        String currentKnot = storyHandler.getStory().getState().getCurrentKnot();
        if (currentKnot == null
                || currentKnot.equalsIgnoreCase(playerSession.getScene().knotName())) return InkActionResult.ignored();

        if (!currentKnot.matches(InkUtil.SCENE_KNOT_PATTERN.pattern())) return InkActionResult.ignored();
        String[] splitKnot = currentKnot.split("_");
        if (splitKnot.length < 2) return InkActionResult.ignored();
        int chapterIndex = Integer.parseInt(splitKnot[1]);
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
        if (chapter == null) return InkActionResult.error("Chapter " + chapterIndex + " does not exists!");
        String sceneName;
        if (splitKnot.length > 2) {
            sceneName = String.join(" ", Arrays.copyOfRange(splitKnot, 2, splitKnot.length));
        } else {
            sceneName = chapter.getSortedSceneList().getFirst().getName();
        }
        Scene scene = chapter.getSceneByName(sceneName);
        if (scene == null)
            return InkActionResult.error("Scene " + sceneName + " of chapter " + chapterIndex + " does not exists!");
        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
        for (InkAction inkAction : playerSession.getInkActions()) {
            inkAction.stop();
        }
        playerSession.getInkActions().clear();
        for (Playback playback : playerSession.getPlaybackManager().getPlaybacks()) {
            playback.stop(true);
        }
        for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
            if (characterRuntime.getEntity() == null) continue;
            characterRuntime.getEntity().remove(Entity.RemovalReason.KILLED);
        }
        playerSession.getCharacterRuntimes().clear();
        storyHandler.setDialogData(DialogData.globalDialogData);
        storyHandler.save(true);
        playerSession.setLastAreaTriggerEntered(null);
        return InkActionResult.ok();
    }

    @Override
    public boolean needScene() {
        return false;
    }
}
