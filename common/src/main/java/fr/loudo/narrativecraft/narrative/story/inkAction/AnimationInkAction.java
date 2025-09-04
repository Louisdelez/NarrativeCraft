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
import fr.loudo.narrativecraft.managers.PlaybackManager;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;

public class AnimationInkAction extends InkAction {

    private Animation animation;
    private Playback playback;
    private String action;
    private boolean isLooping, isBlock, isUnique;

    public AnimationInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    public void tick() {
        if (playback == null) {
            isRunning = false;
            return;
        }
        isRunning = !playback.hasEnded();
        if (!isRunning && blockEndTask != null) {
            blockEndTask.run();
        }
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
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Animation name"));
        }
        String animationName = arguments.get(2);
        animation = scene.getAnimationByName(animationName);
        if (animation == null) {
            return InkActionResult.error(Translation.message(
                    WRONG_ARGUMENT_TEXT, Translation.message("animation.no_exists", animationName, scene.getName())));
        }
        if (arguments.size() < 4 || action.equals("stop")) {
            canBeExecuted = true;
            return InkActionResult.ok();
        }
        try {
            isLooping = Boolean.parseBoolean(arguments.get(3));
        } catch (Exception e) {
            return InkActionResult.error(Translation.message(NOT_VALID_BOOLEAN, arguments.get(3)));
        }
        if (arguments.size() > 4) {
            try {
                isUnique = Boolean.parseBoolean(arguments.get(4));
            } catch (Exception e) {
                return InkActionResult.error(Translation.message(NOT_VALID_BOOLEAN, arguments.get(4)));
            }
        }
        if (arguments.size() > 5) {
            try {
                isBlock = Boolean.parseBoolean(arguments.get(5));
            } catch (Exception e) {
                return InkActionResult.error(Translation.message(NOT_VALID_BOOLEAN, arguments.get(5)));
            }
        }
        canBeExecuted = true;
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        if (action.equals("start")) {
            playback = new Playback(
                    PlaybackManager.ID_INCREMENTER.incrementAndGet(),
                    animation,
                    playerSession.getPlayer().level(),
                    Environment.PRODUCTION,
                    isLooping);
            playback.setUnique(isUnique);
            playerSession.getPlaybackManager().addPlayback(playback);
        } else if (action.equals("stop")) {
            Playback playback = playerSession.getPlaybackManager().getPlayback(animation.getName());
            if (playback == null) return InkActionResult.ignored();
            playback.stop(true);
        }
        return isBlock ? InkActionResult.block() : InkActionResult.ok();
    }
}
