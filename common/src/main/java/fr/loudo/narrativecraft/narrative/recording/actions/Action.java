package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;

public abstract class Action {

    protected int tick;
    protected ActionType actionType;

    public Action(int tick, ActionType actionType) {
        this.tick = tick;
        this.actionType = actionType;
    }

    public int getTick() {
        return tick;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public abstract void execute(PlaybackData playbackData);
    public abstract void rewind(PlaybackData playbackData);
}
