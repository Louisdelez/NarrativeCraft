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

package fr.loudo.narrativecraft.narrative.story.inkAction.server;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.controllers.interaction.InteractionController;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;

public class InteractionInkAction extends InkAction {

    private String action;
    private Interaction interaction;

    public InteractionInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() == 1) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Action type [summon/remove]"));
        }
        action = arguments.get(1);
        if (!action.equals("summon") && !action.equals("remove")) {
            return InkActionResult.error(Translation.message(WRONG_ARGUMENT_TEXT, "Only action type [summon/remove]"));
        }
        if (arguments.size() == 2) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Interaction name"));
        }
        interaction = scene.getInteractionByName(arguments.get(2));
        if (interaction == null) {
            return InkActionResult.error(Translation.message(
                    WRONG_ARGUMENT_TEXT,
                    Translation.message("interaction.no_exists", arguments.get(2), scene.getName())));
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        if (action.equals("summon")) {
            InteractionController controller =
                    new InteractionController(Environment.PRODUCTION, playerSession.getPlayer(), interaction);
            controller.startSession();
            playerSession.setController(null);
            playerSession.getInteractionControllers().add(controller);
        } else if (action.equals("remove")) {
            for (InteractionController interactionController : playerSession.getInteractionControllers()) {
                if (interactionController.getInteraction().getName().equalsIgnoreCase(interaction.getName())) {
                    interactionController.stopSession(false);
                }
            }
            playerSession
                    .getInteractionControllers()
                    .removeIf(interactionController ->
                            interactionController.getInteraction().getName().equalsIgnoreCase(interaction.getName()));
            for (InkAction inkAction : playerSession.getInkActions()) {
                if (inkAction instanceof InteractionInkAction) {
                    inkAction.setRunning(false);
                }
            }
            isRunning = false;
        }
        return InkActionResult.ok();
    }

    @Override
    public boolean needScene() {
        return true;
    }
}
