package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.Playback;
import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import fr.loudo.narrativecraft.util.FakePlayer;
import net.minecraft.world.entity.LivingEntity;

public class DeathAction extends Action {

    private final int entityRecordingId;

    public DeathAction(int tick, int entityRecordingId) {
        super(tick, ActionType.DEATH);
        this.entityRecordingId = entityRecordingId;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        if(playbackData.getActionsData().getEntityIdRecording() == entityRecordingId) {
            if(playbackData.getEntity() != null && playbackData.getEntity() instanceof LivingEntity livingEntity) {
                if(livingEntity instanceof FakePlayer) {
                    livingEntity.setHealth(0.0F);
                    livingEntity.level().broadcastEntityEvent(livingEntity, (byte)60);
                } else {
                    livingEntity.handleEntityEvent((byte)3);
                }
            }
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        Playback playback = playbackData.getPlayback();
        ActionsData actionsData = playbackData.getPlayback().getMasterEntityData();
        Location posToSpawn = actionsData.getLocations().get(playback.getTick() - 1);
        if(posToSpawn == null) return;
        playback.respawnMasterEntity(posToSpawn);
    }

}
