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

package fr.loudo.narrativecraft.api.inkAction;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

public abstract class InkAction {

    protected static final String MISS_ARGUMENT_TEXT = "ink_action.validation.miss_argument";
    protected static final String WRONG_ARGUMENT_TEXT = "ink_action.validation.wrong_argument";
    protected static final String WRONG_TIME_VALUE = "ink_action.validation.wrong_time_value";
    protected static final String WRONG_EASING_VALUE = "ink_action.validation.wrong_easing_value";
    protected static final String NOT_VALID_BOOLEAN = "ink_action.validation.boolean";
    protected static final String NOT_VALID_NUMBER = "ink_action.validation.number";
    protected static final String NOT_VALID_COLOR = "ink_action.validation.color";
    protected static final String NOT_VALID_INK = "ink_action.validation.not_validated";

    protected final String id;
    protected final String syntax;
    protected final Side side;
    protected final CommandMatcher matcher;
    protected boolean isRunning;
    protected boolean canBeExecuted = false;
    protected String command;
    protected Runnable blockEndTask;

    public InkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        this.id = id;
        this.side = side;
        this.syntax = syntax;
        this.matcher = matcher;
    }

    public void tick() {}

    public void partialTick(float partialTick) {}

    public void render(GuiGraphics guiGraphics, float partialTick) {}

    public void render(PoseStack poseStack, float partialTick) {}

    public void stop() {}

    public final InkActionResult validate(String command, Scene scene) {
        this.command = command;
        InkActionResult result = doValidate(InkActionUtil.getArguments(command), scene);
        if (!result.isError()) canBeExecuted = true;
        return result;
    }

    public final InkActionResult execute(PlayerSession playerSession) {
        if (!canBeExecuted) {
            return InkActionResult.error(Translation.message(NOT_VALID_INK));
        }
        isRunning = true;
        return doExecute(playerSession);
    }

    public InkActionResult validateAndExecute(String command, PlayerSession playerSession) {
        InkActionResult result = validate(command, playerSession.getScene());
        if (result.isError()) return result;
        return execute(playerSession);
    }

    protected abstract InkActionResult doValidate(List<String> arguments, Scene scene);

    protected abstract InkActionResult doExecute(PlayerSession playerSession);

    public String getId() {
        return id;
    }

    public Side getSide() {
        return side;
    }

    public CommandMatcher getMatcher() {
        return matcher;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void setBlockEndTask(Runnable blockEndTask) {
        this.blockEndTask = blockEndTask;
    }

    public enum Side {
        CLIENT,
        SERVER
    }

    public interface CommandMatcher {
        boolean matches(String command);
    }
}
