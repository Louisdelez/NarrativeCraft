package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import net.minecraft.world.entity.LivingEntity;

public class LivingEntityByteAction extends Action {

    private final byte livingEntityByte;
    private final byte oldLivingEntityByte;

    public LivingEntityByteAction(int waitTick, byte livingEntityByte, byte oldLivingEntityByte) {
        super(waitTick, ActionType.LIVING_ENTITY_BYTE);
        this.livingEntityByte = livingEntityByte;
        this.oldLivingEntityByte = oldLivingEntityByte;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof LivingEntity) {
            playbackData.getEntity().getEntityData().set(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS(), livingEntityByte);
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof LivingEntity) {
            playbackData.getEntity().getEntityData().set(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS(), oldLivingEntityByte);
        }
    }
}
