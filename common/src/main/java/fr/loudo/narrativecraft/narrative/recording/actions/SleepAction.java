package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

public class SleepAction extends Action {

    private int xB, yB, zB;

    public SleepAction(int tick, BlockPos bedPos) {
        super(tick, ActionType.SLEEP);
        this.xB = bedPos.getX();
        this.yB = bedPos.getY();
        this.zB = bedPos.getZ();
    }

    @Override
    public void execute(PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof LivingEntity livingEntity){
            livingEntity.setSleepingPos(new BlockPos(xB, yB, zB));
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof LivingEntity livingEntity){
            livingEntity.clearSleepingPos();
        }
    }

    public BlockPos getBlockPos() {
        return new BlockPos(xB, yB, zB);
    }
}
