package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import net.minecraft.network.syncher.SynchedEntityData;

public class EntityByteAction extends Action {

    private final byte entityByte;
    private final byte previousEntityByte;

    public EntityByteAction(int waitTick, byte entityByte, byte previousEntityByte) {
        super(waitTick, ActionType.ENTITY_BYTE);
        this.entityByte = entityByte;
        this.previousEntityByte = previousEntityByte;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        SynchedEntityData entityData = playbackData.getEntity().getEntityData();
        entityData.set(EntityAccessor.getDATA_SHARED_FLAGS_ID(), entityByte);
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        SynchedEntityData entityData = playbackData.getEntity().getEntityData();
        entityData.set(EntityAccessor.getDATA_SHARED_FLAGS_ID(), previousEntityByte);
    }
}
