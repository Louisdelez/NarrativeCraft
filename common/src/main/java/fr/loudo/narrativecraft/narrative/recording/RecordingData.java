package fr.loudo.narrativecraft.narrative.recording;

import fr.loudo.narrativecraft.narrative.recording.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionDifferenceListener;
import net.minecraft.world.entity.Entity;

public class RecordingData {

    private final Entity entity;
    private final ActionDifferenceListener actionDifferenceListener;
    private final ActionsData actionsData;
    private boolean savingTrack;

    public RecordingData(Entity entity, Recording recording) {
        this.entity = entity;
        actionsData = new ActionsData(entity, recording.getTick());
        actionDifferenceListener = new ActionDifferenceListener(actionsData, recording);
        savingTrack = false;
    }

    public boolean isSameEntity(Entity entity) {
        return this.entity.getUUID().equals(entity.getUUID());
    }

    public Entity getEntity() {
        return entity;
    }

    public ActionDifferenceListener getActionDifferenceListener() {
        return actionDifferenceListener;
    }

    public ActionsData getActionsData() {
        return actionsData;
    }

    public boolean isSavingTrack() {
        return savingTrack;
    }

    public void setSavingTrack(boolean savingTrack) {
        this.savingTrack = savingTrack;
    }

}