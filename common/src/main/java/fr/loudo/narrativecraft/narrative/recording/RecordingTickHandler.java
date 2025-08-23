package fr.loudo.narrativecraft.narrative.recording;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class RecordingTickHandler {
    public static void tick(MinecraftServer minecraftServer) {
        List<Recording> recordings = NarrativeCraftMod.getInstance().getRecordingManager().getCurrentRecording();
        for(Recording recording : recordings) {
            recording.tick();
        }
    }
}
