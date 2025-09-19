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
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class FadeInkAction extends InkAction {

    private double fadeInSeconds, staySeconds, fadeOutSeconds;
    private int color, totalTick, currentTick;
    private FadeState currentFadeState;

    public FadeInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    public void tick() {
        if (!isRunning) return;
        currentTick++;
        if (currentTick >= totalTick) {
            currentTick = 0;
            if (currentFadeState == FadeState.FADE_IN) {
                currentFadeState = FadeState.STAY;
                totalTick = (int) (staySeconds * 20.0);
            } else if (currentFadeState == FadeState.STAY) {
                currentFadeState = FadeState.FADE_OUT;
                totalTick = (int) (fadeOutSeconds * 20.0);
            } else if (currentFadeState == FadeState.FADE_OUT) {
                isRunning = false;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        if (!isRunning) return;
        double t = Math.clamp((currentTick + partialTick) / totalTick, 0.0, 1.0);
        int opacity = 255;
        if (currentFadeState == FadeState.FADE_IN) {
            opacity = (int) Mth.lerp(t, 0, 255);
        } else if (currentFadeState == FadeState.FADE_OUT) {
            opacity = (int) Mth.lerp(t, 255, 0);
        }
        int newColor = ARGB.color(opacity, color);
        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), newColor);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() == 1) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Fade in seconds"));
        }
        try {
            fadeInSeconds = Double.parseDouble(arguments.get(1));
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(1)));
        }
        if (arguments.size() == 2) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Stay seconds"));
        }
        try {
            staySeconds = Double.parseDouble(arguments.get(2));
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(2)));
        }
        if (arguments.size() == 3) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Fade out seconds"));
        }
        try {
            fadeOutSeconds = Double.parseDouble(arguments.get(3));
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
        }
        color = 0x000000;
        if (arguments.size() > 4) {
            try {
                color = Integer.parseInt(arguments.get(4), 16);
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_COLOR, arguments.get(4)));
            }
        }
        if (fadeInSeconds > 2) {
            fadeInSeconds -= 1;
        }
        if (staySeconds > 2) {
            staySeconds -= 1;
        }
        if (fadeOutSeconds > 2) {
            fadeOutSeconds -= 1;
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        currentFadeState = FadeState.FADE_IN;
        totalTick = (int) (fadeInSeconds * 20.0);
        return InkActionResult.ok();
    }

    public enum FadeState {
        FADE_IN,
        STAY,
        FADE_OUT
    }

    @Override
    public boolean needScene() {
        return false;
    }
}
