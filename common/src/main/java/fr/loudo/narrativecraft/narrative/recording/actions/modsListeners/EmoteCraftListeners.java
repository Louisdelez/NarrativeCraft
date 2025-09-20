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

package fr.loudo.narrativecraft.narrative.recording.actions.modsListeners;

import fr.loudo.narrativecraft.narrative.recording.actions.EmoteAction;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionDifferenceListener;
import io.github.kosmx.emotes.api.events.server.ServerEmoteEvents;

public class EmoteCraftListeners extends ModsListenerImpl {

    private final ServerEmoteEvents.EmotePlayEvent emotePlayEvent;
    private final ServerEmoteEvents.EmoteStopEvent emoteStopEvent;

    public EmoteCraftListeners(ActionDifferenceListener actionDifferenceListener) {
        super(actionDifferenceListener);
        emotePlayEvent = (emoteData, tick, userID) -> {
            if (actionDifferenceListener.getActionsData().getEntity().getUUID().equals(userID)) {
                EmoteAction emoteAction =
                        new EmoteAction(actionDifferenceListener.getRecording().getTick(), emoteData.get());
                actionDifferenceListener.getActionsData().addAction(emoteAction);
            }
        };
        emoteStopEvent = (uuid, uuid1) -> {
            if (actionDifferenceListener.getActionsData().getEntity().getUUID().equals(uuid1)) {
                EmoteAction emoteAction =
                        new EmoteAction(actionDifferenceListener.getRecording().getTick(), null);
                actionDifferenceListener.getActionsData().addAction(emoteAction);
            }
        };
    }

    @Override
    public void start() {
        ServerEmoteEvents.EMOTE_PLAY.register(emotePlayEvent);
        ServerEmoteEvents.EMOTE_STOP_BY_USER.register(emoteStopEvent);
    }

    @Override
    public void stop() {
        ServerEmoteEvents.EMOTE_PLAY.unregister(emotePlayEvent);
        ServerEmoteEvents.EMOTE_STOP_BY_USER.unregister(emoteStopEvent);
    }
}
