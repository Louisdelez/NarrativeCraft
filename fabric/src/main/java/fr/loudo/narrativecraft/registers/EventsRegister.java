package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.events.LifecycleEvent;
import fr.loudo.narrativecraft.events.PlayerServerConnection;
import fr.loudo.narrativecraft.keys.PressKeyListener;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class EventsRegister {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(LifecycleEvent::onServerStart);
        ServerPlayConnectionEvents.JOIN.register(PlayerServerConnection::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(PlayerServerConnection::onPlayerLeave);
        ClientTickEvents.END_CLIENT_TICK.register(PressKeyListener::onPressKey);
    }

}
