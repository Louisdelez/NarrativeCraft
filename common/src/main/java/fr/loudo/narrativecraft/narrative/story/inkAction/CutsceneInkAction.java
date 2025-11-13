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
import fr.loudo.narrativecraft.controllers.cutscene.CutsceneController;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframeGroup;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;

public class CutsceneInkAction extends InkAction {

    private Cutscene cutscene;
    private CutsceneController controller;

    public CutsceneInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    public void tick() {
        if (!isRunning) return;
        if (controller.finishedCutscene()) {
            blockEndTask.run();
        }
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() < 3) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Cutscene name"));
        }
        String cutsceneName = arguments.get(2);
        cutscene = scene.getCutsceneByName(cutsceneName);
        if (cutscene == null) {
            return InkActionResult.error(Translation.message("cutscene.no_exists", cutsceneName, scene.getName()));
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        playerSession.clearKilledCharacters();
        controller = new CutsceneController(Environment.PRODUCTION, playerSession.getPlayer(), cutscene);
        controller.startSession();
        if (controller.getKeyframeGroups().isEmpty()) {
            return InkActionResult.error("Cutscene " + cutscene.getName() + " has not keyframes ! Can't be played.");
        }
        CutsceneKeyframeGroup keyframeGroup = controller.getKeyframeGroups().getFirst();
        CutsceneKeyframe keyframeA = keyframeGroup.getKeyframes().getFirst();
        CutsceneKeyframe keyframeB;
        if (keyframeGroup.getKeyframes().size() > 1) {
            keyframeB = keyframeGroup.getKeyframes().get(1);
        } else {
            keyframeB = keyframeA;
        }
        controller.setPlaying(true);
        controller.getCutscenePlayback().setupAndPlay(keyframeA, keyframeB);
        playerSession.getInkActions().removeIf(inkAction -> inkAction instanceof CameraAngleInkAction);
        return InkActionResult.block();
    }

    @Override
    public boolean needScene() {
        return true;
    }
}
