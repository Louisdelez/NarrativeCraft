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

package fr.loudo.narrativecraft.narrative.inkTag;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionRegistry;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import java.util.ArrayList;
import java.util.List;

public class InkTagHandler {

    private final PlayerSession playerSession;
    private final List<String> tagsToExecute = new ArrayList<>();

    public InkTagHandler(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    public void execute() {
        InkActionResult result = InkActionResult.ok();
        List<String> executed = new ArrayList<>();
        for (String tag : tagsToExecute) {
            executed.add(tag);
            InkAction inkAction = InkActionRegistry.findByCommand(tag);
            if (inkAction == null) continue;
            inkAction.setBlockEndTask(this::execute);
            result = inkAction.validate(tag, playerSession.getScene());
            if (result.isError()) {
                throw new InkTagHandlerException(inkAction.getClass(), result.errorMessage());
            }
            result = inkAction.execute(playerSession);
            playerSession.addInkAction(inkAction);
            if (result.isBlock()) break;
        }
        tagsToExecute.removeAll(executed);
        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler != null && tagsToExecute.isEmpty() && result.isOk()) {
            storyHandler.showCurrentDialog();
        }
    }

    public void stopAll() {
        for (InkAction inkAction : playerSession.getInkActions()) {
            inkAction.stop();
        }
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public List<String> getTagsToExecute() {
        return tagsToExecute;
    }
}
