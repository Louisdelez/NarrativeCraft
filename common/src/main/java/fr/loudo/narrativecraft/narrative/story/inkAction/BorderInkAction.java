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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;

public class BorderInkAction extends InkAction {

    private int up, right, down, left, color;
    private double opacity;

    public BorderInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        if (!isRunning) return;
        Minecraft minecraft = Minecraft.getInstance();
        int widthScreen = minecraft.getWindow().getGuiScaledWidth();
        int heightScreen = minecraft.getWindow().getGuiScaledHeight();
        int guiScale = minecraft.options.guiScale().get();
        if (minecraft.options.guiScale().get() == 0) {
            guiScale = 3;
        }
        // UP
        guiGraphics.fill(0, 0, widthScreen, up / guiScale, color);

        // RIGHT
        guiGraphics.fill(widthScreen - right / guiScale, 0, widthScreen, heightScreen, color);

        // DOWN
        guiGraphics.fill(0, heightScreen - down / guiScale, widthScreen, heightScreen, color);

        // LEFT
        guiGraphics.fill(0, 0, left / guiScale, heightScreen, color);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() < 2) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Up value missing"));
        }
        if (arguments.size() < 3) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Right value missing"));
        }
        if (arguments.size() < 4) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Down value missing"));
        }
        if (arguments.size() < 5) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Left value missing"));
        }
        if (arguments.size() < 6) {
            try {
                up = Integer.parseInt(arguments.get(1)) * 2;
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(1)));
            }
            try {
                right = Integer.parseInt(arguments.get(2)) * 2;
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(2)));
            }
            try {
                down = Integer.parseInt(arguments.get(3)) * 2;
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
            }
            try {
                left = Integer.parseInt(arguments.get(4)) * 2;
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(4)));
            }
        } else {
            try {
                color = Integer.parseInt(arguments.get(6), 16);
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_COLOR, arguments.get(6)));
            }
        }
        opacity = 1.0;
        if (arguments.size() > 6) {
            try {
                opacity = Integer.parseInt(arguments.get(6));
                if (opacity > 1) {
                    return InkActionResult.error(
                            Translation.message(WRONG_ARGUMENT_TEXT, "The opacity value is greater than 1"));
                } else if (opacity < 0) {
                    return InkActionResult.error(
                            Translation.message(WRONG_ARGUMENT_TEXT, "The opacity value is less than 0"));
                }
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(6)));
            }
        }
        color = FastColor.ABGR32.color((int) (opacity * 255), color);
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        return InkActionResult.ok();
    }

    @Override
    public boolean needScene() {
        return false;
    }
}
