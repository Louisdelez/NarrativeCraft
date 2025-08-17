package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.FakePlayer;
import net.minecraft.server.level.ServerPlayer;

public class OnPlayerServerConnection {

    public static void playerJoin(ServerPlayer player) {
        if(player instanceof FakePlayer) return;
        initSession(player);
    }

    public static void playerLeave(ServerPlayer player) {
        if(player instanceof FakePlayer) return;
        clearSession(player);
    }

    private static void initSession(ServerPlayer player) {
        PlayerSession playerSession = new PlayerSession(player);
        NarrativeCraftMod.getInstance().getPlayerSessionManager().addSession(playerSession);
    }

    private static void clearSession(ServerPlayer player) {
        PlayerSessionManager playerSessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession playerSession = playerSessionManager.getSessionByPlayer(player);
        if(playerSession == null) return;
        playerSessionManager.removeSession(playerSession);
    }

}
