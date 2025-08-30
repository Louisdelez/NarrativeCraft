/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.managers;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;

public class PlayerSessionManager {

    private final List<PlayerSession> playerSessions = new ArrayList<>();

    public void addSession(PlayerSession playerSession) {
        if (playerSessions.contains(playerSession)) return;
        playerSessions.add(playerSession);
        NarrativeCraftMod.LOGGER.info(playerSessions.toString());
    }

    public void removeSession(PlayerSession playerSession) {
        playerSessions.remove(playerSession);
    }

    public PlayerSession getSessionByPlayer(Player player) {
        for (PlayerSession playerSession : playerSessions) {
            if (playerSession.isSamePlayer(player)) {
                return playerSession;
            }
        }
        return null;
    }

    public List<PlayerSession> getPlayerSessions() {
        return playerSessions;
    }
}
