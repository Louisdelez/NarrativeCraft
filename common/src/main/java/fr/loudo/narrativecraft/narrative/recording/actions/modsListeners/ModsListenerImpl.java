package fr.loudo.narrativecraft.narrative.recording.actions.modsListeners;

import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionDifferenceListener;

public abstract class ModsListenerImpl {

    protected ActionDifferenceListener actionDifferenceListener;

    public ModsListenerImpl(ActionDifferenceListener actionDifferenceListener) {
        this.actionDifferenceListener = actionDifferenceListener;
    }

    public abstract void start();
    public abstract void stop();

}
