package fr.loudo.narrativecraft.managers;

import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayerSessionManager {

    private final List<PlayerSession> playerSessions = new ArrayList<>();

    public void addSession(PlayerSession playerSession) {
        if(playerSessions.contains(playerSession)) return;
        playerSessions.add(playerSession);
    }

    public void removeSession(PlayerSession playerSession) {
        playerSessions.remove(playerSession);
    }

    public PlayerSession getSessionByPlayer(ServerPlayer player) {
        for(PlayerSession playerSession : playerSessions) {
            if(playerSession.isSamePlayer(player)) {
                return playerSession;
            }
        }
        return null;
    }

    public List<PlayerSession> getPlayerSessions() {
        return playerSessions;
    }
}
