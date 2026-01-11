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

package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recording.actions.RespawnAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * MC 1.19.x version of OnRespawn.
 * Key differences from 1.20.x+:
 * - Uses onGround instead of onGround() (method doesn't exist in 1.19.x)
 */
public class OnRespawn {

    public static void respawn(Player player) {
        if (NarrativeCraftMod.server == null) return;
        Recording recording =
                NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
        if (recording == null || !recording.isRecording()) return;
        // 1.19.x: Use isOnGround() method
        Location respawnLocation = new Location(
                player.getX(), player.getY(), player.getZ(), player.getXRot(), player.getYRot(), player.isOnGround());
        RespawnAction action = new RespawnAction(recording.getTick(), respawnLocation);
        ActionsData actionsData = recording.getActionDataFromEntity(player);
        actionsData.addAction(action);
        for (ServerPlayer serverPlayer :
                NarrativeCraftMod.server.getPlayerList().getPlayers()) {
            if (serverPlayer.getUUID().equals(player.getUUID())) {
                actionsData.setEntity(serverPlayer);
            }
        }
    }
}
