package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import fr.loudo.narrativecraft.util.FakePlayer;
import net.minecraft.world.entity.item.ItemEntity;

public class ItemPickUpAction extends Action {

    private final int entityRecordingId;

    public ItemPickUpAction(int tick, int entityRecordingId) {
        super(tick, ActionType.ITEM_PICKUP);
        this.entityRecordingId = entityRecordingId;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof FakePlayer fakePlayer) {
            ItemEntity item = (ItemEntity) playbackData.getPlayback().getEntityByRecordId(entityRecordingId);
            if(item == null) return;
            fakePlayer.take(item, item.getItem().getCount());
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        PlaybackData itemPlayback = playbackData.getPlayback().getPlaybackDataByRecordId(entityRecordingId);
        itemPlayback.killEntity();
        Location takeLoc = playbackData.getActionsData().getLocations().get(tick);
        if(takeLoc == null) return;
        itemPlayback.spawnEntity(takeLoc);
    }

}
