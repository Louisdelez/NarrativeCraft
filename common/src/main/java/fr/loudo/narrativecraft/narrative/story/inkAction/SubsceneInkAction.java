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

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkActionUtil;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;

public class SubsceneInkAction extends InkAction {
    private Subscene subscene;
    private String action;
    private boolean isLooping, isBlock, isUnique;

    public SubsceneInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    public void tick() {
        if (subscene == null) {
            isRunning = false;
            return;
        }
        if (!isRunning && blockEndTask != null) {
            blockEndTask.run();
            return;
        }
        isRunning = !subscene.hasEnded();
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() < 2) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Action start or stop"));
        }
        action = arguments.get(1);
        if (!action.equals("start") && !action.equals("stop")) {
            return InkActionResult.error(Translation.message(WRONG_ARGUMENT_TEXT, "Only start or stop as action"));
        }
        if (arguments.size() < 3) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Subscene name"));
        }
        String subsceneName = arguments.get(2);
        subscene = scene.getSubsceneByName(subsceneName);
        if (subscene == null) {
            return InkActionResult.error(Translation.message(
                    WRONG_ARGUMENT_TEXT, Translation.message("subscene.no_exists", subsceneName, scene.getName())));
        }
        isLooping = InkActionUtil.getOptionalArgument(command, "loop");
        isUnique = InkActionUtil.getOptionalArgument(command, "unique");
        isBlock = InkActionUtil.getOptionalArgument(command, "block");
        if (arguments.size() < 4 || action.equals("stop")) {
            canBeExecuted = true;
            return InkActionResult.ok();
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        if (action.equals("start")) {
            StoryHandler storyHandler = playerSession.getStoryHandler();
            if (storyHandler != null) {
                subscene.start(playerSession.getPlayer().level(), Environment.PRODUCTION, isLooping, storyHandler);
            } else {
                subscene.start(playerSession.getPlayer().level(), Environment.PRODUCTION, isLooping);
            }
            for (Playback playback : subscene.getPlaybacks()) {
                playerSession.getCharacterRuntimes().add(playback.getCharacterRuntime());
                playback.setUnique(isUnique);
            }
            playerSession.clearKilledCharacters();
            playerSession.getPlaybackManager().getPlaybacks().addAll(subscene.getPlaybacks());
        } else if (action.equals("stop")) {
            for (Playback playback : subscene.getPlaybacks()) {
                playerSession.getCharacterRuntimes().remove(playback.getCharacterRuntime());
            }
            playerSession.getPlaybackManager().getPlaybacks().removeAll(subscene.getPlaybacks());
            subscene.stop(true);
        }
        return isBlock ? InkActionResult.block() : InkActionResult.ok();
    }

    @Override
    public boolean needScene() {
        return true;
    }
}
