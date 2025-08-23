package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public class SwingAction extends Action {

    private InteractionHand interactionHand;

    public SwingAction(int waitTick, InteractionHand interactionHand) {
        super(waitTick, ActionType.SWING);
        this.interactionHand = interactionHand;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.swing(interactionHand);
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {}
}
