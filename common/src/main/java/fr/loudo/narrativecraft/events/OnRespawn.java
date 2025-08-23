package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recording.actions.RespawnAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class OnRespawn {

    public static void respawn(Player player) {
        Recording recording = NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
        if(recording == null || !recording.isRecording()) return;
        Location respawnLocation = new Location(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getXRot(),
                player.getYRot(),
                player.onGround()
        );
        RespawnAction action = new RespawnAction(recording.getTick(), respawnLocation);
        ActionsData actionsData = recording.getActionDataFromEntity(player);
        actionsData.addAction(action);
        for(ServerPlayer serverPlayer : NarrativeCraftMod.server.getPlayerList().getPlayers()) {
            if(serverPlayer.getUUID().equals(player.getUUID())) {
                actionsData.setEntity(serverPlayer);
            }
        }
    }

}