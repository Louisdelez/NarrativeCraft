package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.mixin.accessor.AbstractHorseAccessor;
import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class AbstractHorseByteAction extends Action {

    private final byte currentByte;
    private final byte oldByte;

    public AbstractHorseByteAction(int tick, byte currentByte, byte oldByte) {
        super(tick, ActionType.ABSTRACT_HORSE_BYTE);
        this.currentByte = currentByte;
        this.oldByte = oldByte;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof AbstractHorse abstractHorse) {
            playbackData.getEntity().getEntityData().set(AbstractHorseAccessor.getDATA_ID_FLAGS(), currentByte);
            if(currentByte >= AbstractHorseAccessor.getFLAG_STANDING()) {
                if(abstractHorse.getItemBySlot(EquipmentSlot.SADDLE).isEmpty()) {
                    abstractHorse.ejectPassengers();
                }
            }
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof AbstractHorse) {
            playbackData.getEntity().getEntityData().set(AbstractHorseAccessor.getDATA_ID_FLAGS(), oldByte);
        }
    }
}
