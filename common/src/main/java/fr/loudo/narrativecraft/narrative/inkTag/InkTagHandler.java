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
import fr.loudo.narrativecraft.util.Util;
import java.util.ArrayList;
import java.util.List;

public class InkTagHandler {

    private final PlayerSession playerSession;
    private List<String> tagsToExecute = new ArrayList<>();
    private Runnable run;

    public InkTagHandler(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    public void execute() throws InkTagHandlerException {
        InkActionResult result = InkActionResult.ok();
        for (int i = 0; i < tagsToExecute.size(); i++) {
            String tag = tagsToExecute.get(i);
            InkAction inkAction = InkActionRegistry.findByCommand(tag);
            if (inkAction == null) continue;
            result = inkAction.validate(tag, playerSession.getScene());
            if (result.isError()) {
                throw new InkTagHandlerException(inkAction.getClass(), result.errorMessage());
            }
            result = inkAction.execute(playerSession);
            if (result.isError()) {
                throw new InkTagHandlerException(inkAction.getClass(), result.errorMessage());
            }
            if (result.isBlock()) {
                inkAction.setBlockEndTask(() -> {
                    inkAction.setRunning(false);
                    try {
                        execute();
                    } catch (InkTagHandlerException e) {
                        if (playerSession.getStoryHandler() == null) {
                            Util.sendCrashMessage(playerSession.getPlayer(), e);
                        } else {
                            playerSession.getStoryHandler().showCrash(e);
                            playerSession.getStoryHandler().stop();
                        }
                    }
                });
            }
            if (result.isIgnore()) {
                inkAction.setRunning(false);
            }
            playerSession.addInkAction(inkAction);
            if (result.isBlock()) {
                tagsToExecute = tagsToExecute.subList(i + 1, tagsToExecute.size());
                return;
            }
        }
        tagsToExecute.clear();
        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler != null && tagsToExecute.isEmpty() && result.isOk()) {
            if (storyHandler.isFinished() && storyHandler.getDialogText().isEmpty()) {
                storyHandler.stopAndFinishScreen();
            } else if (!storyHandler.isFinished()
                    && storyHandler.getDialogText().isEmpty()
                    && storyHandler.getStory().getCurrentChoices().isEmpty()
                    && !playerSession.isOnGameplay()) {
                storyHandler.next();
                run = null;
            } else {
                storyHandler.showCurrentDialog();
            }
        }
        if (run != null) run.run();
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

    public Runnable getRun() {
        return run;
    }

    public void setRun(Runnable run) {
        this.run = run;
    }
}
