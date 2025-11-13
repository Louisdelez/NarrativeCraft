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
import fr.loudo.narrativecraft.util.Easing;
import fr.loudo.narrativecraft.util.Translation;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.Mth;

public class ChangeDayTimeInkAction extends InkAction {

    private String action;
    private long fromTick, toTick, segmentTick, tick;
    private double forSeconds;
    private Easing easing;

    public ChangeDayTimeInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    public void tick() {
        if (!canBeExecuted || !isRunning || easing == null) return;
        segmentTick++;
    }

    @Override
    public void partialTick(float partialTick) {
        if (!canBeExecuted || !isRunning || easing == null) return;
        double durationTicks = forSeconds * 20.0;
        double t = Mth.clamp((segmentTick + partialTick) / durationTicks, 0.0, 1.0);
        tick = (long) Mth.lerp(t, fromTick, toTick);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() < 2) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Action set or add"));
        }
        action = arguments.get(1);
        if (!action.equals("set") && !action.equals("add")) {
            return InkActionResult.error(Translation.message(WRONG_ARGUMENT_TEXT, "Only set or add as action"));
        }
        if (arguments.size() < 3) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Time day tick"));
        }
        fromTick = getTickFromString(arguments.get(2));
        tick = fromTick;
        if (fromTick == -1) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(2)));
        }
        if (action.equals("add") || arguments.size() == 3) {
            return InkActionResult.ok();
        }
        if (arguments.get(3).equals("to") && arguments.size() < 5) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Time day tick"));
        }
        toTick = getTickFromString(arguments.get(4));
        if (toTick == -1) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(4)));
        }
        if (arguments.get(5).equals("for") && arguments.size() < 7) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "For seconds"));
        }
        try {
            forSeconds = Double.parseDouble(arguments.get(6));
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(6)));
        }
        if (arguments.size() == 7) {
            return InkActionResult.error(Translation.message(WRONG_TIME_VALUE));
        }
        String timeValue = arguments.get(7);
        forSeconds = InkActionUtil.getSecondsFromTimeValue(forSeconds, timeValue);
        if (forSeconds == -1) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, forSeconds));
        }
        easing = Easing.SMOOTH;
        if (arguments.size() > 8) {
            try {
                easing = Easing.valueOf(arguments.get(8).toUpperCase());
            } catch (IllegalArgumentException e) {
                return InkActionResult.error(Translation.message(WRONG_EASING_VALUE, Arrays.toString(Easing.values())));
            }
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        for (InkAction inkAction : playerSession.getInkActions()) {
            if (inkAction instanceof ChangeDayTimeInkAction changeDayTimeInkAction && this.action.equals("add")) {
                tick += changeDayTimeInkAction.getTick();
                changeDayTimeInkAction.setRunning(false);
            }
        }
        return InkActionResult.ok();
    }

    private long getTickFromString(String dayTime) {
        switch (dayTime) {
            case "day" -> {
                return 1000L;
            }
            case "midnight" -> {
                return 18000L;
            }
            case "night" -> {
                return 13000L;
            }
            case "noon" -> {
                return 6000L;
            }
            default -> {
                try {
                    return Long.parseLong(dayTime);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
    }

    public long getTick() {
        return tick;
    }

    @Override
    public boolean needScene() {
        return false;
    }
}
