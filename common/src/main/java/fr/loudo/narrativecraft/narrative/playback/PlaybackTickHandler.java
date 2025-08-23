package fr.loudo.narrativecraft.narrative.playback;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class PlaybackTickHandler {
    public static void tick(MinecraftServer minecraftServer) {
        List<Playback> playbacks = NarrativeCraftMod.getInstance().getPlaybackManager().getPlaybacksPlaying();
        List<Playback> toRemove = new ArrayList<>();
        for(Playback playback : playbacks) {
            playback.tick();
            if(playback.hasEnded()) {
                toRemove.add(playback);
            }
        }
        NarrativeCraftMod.getInstance().getPlaybackManager().getPlaybacks().removeAll(toRemove);
    }
}
