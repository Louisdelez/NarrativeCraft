package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;

public class RespawnAction extends Action{

    private final Location respawnPos;

    public RespawnAction(int tick, Location respawnPos) {
        super(tick, ActionType.RESPAWN);
        this.respawnPos = respawnPos;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        playbackData.getPlayback().killMasterEntity();
        playbackData.getPlayback().respawnMasterEntity(respawnPos);
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        playbackData.getPlayback().killMasterEntity();
    }
}
