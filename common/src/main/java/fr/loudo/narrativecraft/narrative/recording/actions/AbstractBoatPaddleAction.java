package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.mixin.accessor.AbstractBoatAccessor;
import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractBoat;

public class AbstractBoatPaddleAction extends Action {

    private final boolean leftPaddle;
    private final boolean rightPaddle;

    private final boolean oldLeftPaddle;
    private final boolean oldRightPaddle;

    public AbstractBoatPaddleAction(int tick, boolean leftPaddle, boolean rightPaddle, boolean oldLeftPaddle, boolean oldRightPaddle) {
        super(tick, ActionType.ABSTRACT_BOAT_PADDLE);
        this.leftPaddle = leftPaddle;
        this.rightPaddle = rightPaddle;
        this.oldLeftPaddle = oldLeftPaddle;
        this.oldRightPaddle = oldRightPaddle;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        Entity entity1 = playbackData.getEntity().level().getEntity(playbackData.getEntity().getId());
        if(entity1 instanceof AbstractBoat) {
            playbackData.getEntity().getEntityData().set(AbstractBoatAccessor.getDATA_ID_PADDLE_LEFT(), leftPaddle);
            playbackData.getEntity().getEntityData().set(AbstractBoatAccessor.getDATA_ID_PADDLE_RIGHT(), rightPaddle);
        }

    }

    @Override
    public void rewind(PlaybackData playbackData) {
        Entity entity1 = playbackData.getEntity().level().getEntity(playbackData.getEntity().getId());
        if(entity1 instanceof AbstractBoat) {
            playbackData.getEntity().getEntityData().set(AbstractBoatAccessor.getDATA_ID_PADDLE_LEFT(), oldLeftPaddle);
            playbackData.getEntity().getEntityData().set(AbstractBoatAccessor.getDATA_ID_PADDLE_RIGHT(), oldRightPaddle);
        }
    }
}
