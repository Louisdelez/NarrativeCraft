package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.events.*;
import fr.loudo.narrativecraft.keys.PressKeyListener;
import fr.loudo.narrativecraft.narrative.playback.PlaybackTickHandler;
import fr.loudo.narrativecraft.narrative.recording.RecordingTickHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class EventsRegister {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(LifecycleEvent::onServerStart);
        ServerPlayConnectionEvents.JOIN.register(PlayerServerConnection::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(PlayerServerConnection::onPlayerLeave);
        ClientTickEvents.END_CLIENT_TICK.register(PressKeyListener::onPressKey);
        ServerTickEvents.END_SERVER_TICK.register(RecordingTickHandler::tick);
        ServerTickEvents.END_SERVER_TICK.register(PlaybackTickHandler::tick);

        ServerPlayerEvents.AFTER_RESPAWN.register(RespawnEvent::onRespawn);
        PlayerBlockBreakEvents.AFTER.register(BlockBreakEvent::onBlockBreak);
        UseBlockCallback.EVENT.register(RightClickBlock::onRightClickBlock);
    }

}
