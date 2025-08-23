package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.playback.PlaybackTickHandler;
import fr.loudo.narrativecraft.narrative.recording.RecordingTickHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class ServerTickEventNeoForge {

    public ServerTickEventNeoForge(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(ServerTickEventNeoForge::onServerTick);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        RecordingTickHandler.tick(event.getServer());
        PlaybackTickHandler.tick(event.getServer());
    }

}