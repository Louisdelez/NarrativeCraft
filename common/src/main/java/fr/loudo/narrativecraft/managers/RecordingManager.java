package fr.loudo.narrativecraft.managers;

import fr.loudo.narrativecraft.narrative.recording.Recording;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class RecordingManager {

    private final List<Recording> recordings = new ArrayList<>();

    public void addRecording(Recording recording) {
        if(recordings.contains(recording)) return;
        recordings.add(recording);
    }

    public void removeRecording(Recording recording) {
        recordings.remove(recording);
    }

    public Recording getRecording(Entity entity) {
        for(Recording recording : recordings) {
            if(recording.isSameEntity(entity)) {
                return recording;
            }
        }
        return null;
    }

    public boolean isRecording(ServerPlayer player) {
        for(Recording recording : recordings) {
            if(recording.isSameEntity(player) && recording.isRecording()) {
                return true;
            }
        }
        return false;
    }

    public List<Recording> getCurrentRecording() {
        return recordings.stream().filter(Recording::isRecording).toList();
    }

    public List<Recording> getRecordings() {
        return recordings;
    }

}
