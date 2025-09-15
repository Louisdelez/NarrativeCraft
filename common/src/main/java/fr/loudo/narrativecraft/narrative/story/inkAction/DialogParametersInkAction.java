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
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer3D;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec2;

public class DialogParametersInkAction extends InkAction {

    private static final Set<String> VALID_PARAMETERS = Set.of(
            "offset",
            "scale",
            "padding",
            "width",
            "text_color",
            "background_color",
            "gap",
            "letter_spacing",
            "no_skip",
            "manual_skip",
            "auto_skip",
            "bobbing");

    private enum ParameterType {
        OFFSET(2),
        SCALE(1),
        PADDING(2),
        WIDTH(1),
        TEXT_COLOR(1),
        BACKGROUND_COLOR(1),
        GAP(1),
        LETTER_SPACING(1),
        NO_SKIP(0),
        MANUAL_SKIP(0),
        AUTO_SKIP(1),
        BOBBING(2);

        private final int expectedValues;

        ParameterType(int expectedValues) {
            this.expectedValues = expectedValues;
        }

        public int getExpectedValues() {
            return expectedValues;
        }

        public static ParameterType fromString(String parameter) {
            try {
                return valueOf(parameter.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private ParameterType parameterType;
    private float value1, value2;

    public DialogParametersInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() < 2) {
            return InkActionResult.error(Translation.message(
                    MISS_ARGUMENT_TEXT, "Parameter available: " + String.join(", ", VALID_PARAMETERS)));
        }

        String parameter = arguments.get(1).toLowerCase();
        parameterType = ParameterType.fromString(parameter);

        if (parameterType == null) {
            return InkActionResult.error(Translation.message(
                    MISS_ARGUMENT_TEXT, "Valid parameters: " + String.join(", ", VALID_PARAMETERS)));
        }

        int expectedArgs = 2 + parameterType.getExpectedValues();
        if (arguments.size() < expectedArgs) {
            return InkActionResult.error(Translation.message(
                    MISS_ARGUMENT_TEXT,
                    "Expected " + parameterType.getExpectedValues() + " value(s) for " + parameter));
        }

        return parseValues(arguments);
    }

    private InkActionResult parseValues(List<String> arguments) {
        try {
            switch (parameterType) {
                case OFFSET:
                case PADDING:
                case BOBBING:
                    value1 = Float.parseFloat(arguments.get(2));
                    value2 = Float.parseFloat(arguments.get(3));
                    break;
                case SCALE:
                case GAP:
                case LETTER_SPACING:
                case AUTO_SKIP:
                    value1 = Float.parseFloat(arguments.get(2));
                    break;
                case WIDTH:
                    value1 = Integer.parseInt(arguments.get(2));
                case TEXT_COLOR:
                case BACKGROUND_COLOR:
                    value1 = Integer.parseInt(arguments.get(2), 16);
                    break;
                case NO_SKIP:
                    break;
            }
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(2)));
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        DialogRenderer dialogRenderer = playerSession.getDialogRenderer();
        StoryHandler storyHandler = playerSession.getStoryHandler();

        executeParameter(dialogRenderer, storyHandler);

        if (dialogRenderer != null) {
            dialogRenderer.update();
        }

        isRunning = false;
        return InkActionResult.ok();
    }

    private void executeParameter(DialogRenderer dialogRenderer, StoryHandler storyHandler) {
        switch (parameterType) {
            case OFFSET:
                executeIfRenderer3D(dialogRenderer, renderer3D -> renderer3D.setDialogOffset(new Vec2(value1, value2)));
                executeDialogData(storyHandler, dialogData -> dialogData.setOffset(new Vec2(value1, value2)));
                break;
            case SCALE:
                executeIfRenderer(dialogRenderer, renderer -> renderer.setScale(value1));
                executeDialogData(storyHandler, dialogData -> dialogData.setScale(value1));
                break;
            case PADDING:
                executeIfRenderer(dialogRenderer, renderer -> {
                    renderer.setPaddingX(value1);
                    renderer.setPaddingY(value2);
                });
                executeDialogData(storyHandler, dialogData -> {
                    dialogData.setPaddingX(value1);
                    dialogData.setPaddingY(value2);
                });
                break;
            case WIDTH:
                executeIfRenderer(dialogRenderer, renderer -> renderer.setWidth(value1));
                executeDialogData(storyHandler, dialogData -> dialogData.setWidth(value1));
                break;
            case TEXT_COLOR:
                executeIfRenderer(dialogRenderer, renderer -> {
                    int color = ARGB.color(255, (int) value1);
                    renderer.setTextColor(color);
                });
                executeDialogData(storyHandler, dialogData -> {
                    int color = ARGB.color(255, (int) value1);
                    dialogData.setTextColor(color);
                });
                break;
            case BACKGROUND_COLOR:
                executeIfRenderer(dialogRenderer, renderer -> {
                    int color = ARGB.color(255, (int) value1);
                    renderer.setBackgroundColor(color);
                });
                executeDialogData(storyHandler, dialogData -> {
                    int color = ARGB.color(255, (int) value1);
                    dialogData.setBackgroundColor(color);
                });
                break;
            case GAP:
                executeIfRenderer(dialogRenderer, renderer -> renderer.setGap(value1));
                executeDialogData(storyHandler, dialogData -> dialogData.setGap(value1));
                break;
            case LETTER_SPACING:
                executeIfRenderer(dialogRenderer, renderer -> renderer.setLetterSpacing(value1));
                executeDialogData(storyHandler, dialogData -> dialogData.setLetterSpacing(value1));
                break;
            case NO_SKIP:
                executeDialogData(storyHandler, dialogData -> dialogData.setNoSkip(true));
                break;
            case MANUAL_SKIP:
                executeIfRenderer(dialogRenderer, renderer -> renderer.setNoSkip(false));
                executeDialogData(storyHandler, dialogData -> dialogData.setNoSkip(false));
                break;
            case AUTO_SKIP:
                executeIfRenderer(dialogRenderer, renderer -> {
                    if (value1 > 0) {
                        renderer.stopAutoSkip();
                    } else {
                        renderer.autoSkipAt(value1);
                    }
                });
                executeDialogData(storyHandler, dialogData -> {
                    if (value1 > 0) {
                        dialogData.setDialogAutoSkip(true);
                        dialogData.setAutoSkipSeconds(value1);
                    } else {
                        dialogData.setDialogAutoSkip(false);
                        dialogData.setAutoSkipSeconds(0.0);
                    }
                });
                break;
            case BOBBING:
                executeIfRenderer3D(dialogRenderer, renderer -> renderer.updateBobbing(value1, value2));
                executeDialogData(storyHandler, dialogData -> {
                    dialogData.setNoiseShakeSpeed(value1);
                    dialogData.setNoiseShakeStrength(value2);
                });
                break;
        }
    }

    private void executeIfRenderer(DialogRenderer renderer, Consumer<DialogRenderer> action) {
        if (renderer != null) {
            action.accept(renderer);
        }
    }

    private void executeIfRenderer3D(DialogRenderer renderer, Consumer<DialogRenderer3D> action) {
        if (renderer instanceof DialogRenderer3D renderer3D) {
            action.accept(renderer3D);
        }
    }

    private void executeDialogData(StoryHandler storyHandler, Consumer<DialogData> action) {
        if (storyHandler == null) return;
        DialogData dialogData = storyHandler.getDialogData();
        action.accept(dialogData);
    }

    @Override
    public boolean needScene() {
        return false;
    }
}
