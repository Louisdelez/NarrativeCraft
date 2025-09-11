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
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;

public class CooldownInkAction extends InkAction {

    private double totalTick;
    private double currentTick;

    public CooldownInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    public void tick() {
        if (!isRunning) return;
        currentTick++;
        if (currentTick >= totalTick) {
            isRunning = false;
            blockEndTask.run();
        }
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() == 1) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Cooldown value"));
        }
        double waitSeconds;
        try {
            waitSeconds = Double.parseDouble(arguments.get(1));
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(1)));
        }
        if (arguments.size() == 2) {
            return InkActionResult.error(Translation.message(WRONG_TIME_VALUE));
        }
        String timeValue = arguments.get(2);
        waitSeconds = InkActionUtil.getSecondsFromTimeValue(waitSeconds, timeValue);
        if (waitSeconds == -1.0) {
            return InkActionResult.error(Translation.message(WRONG_TIME_VALUE));
        }
        if (waitSeconds > 2) {
            waitSeconds -= 1;
        }
        totalTick = waitSeconds * 20;
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        return InkActionResult.block();
    }
}
