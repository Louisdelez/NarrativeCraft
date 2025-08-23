package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.actions.GameModeAction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class OnGameModeChange {

    public static void gameModeChange(GameType gameType, GameType oldGameType, Player player) {
        Recording recording = NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
        if(recording == null || !recording.isRecording()) return;
        GameModeAction gameModeAction = new GameModeAction(recording.getTick(), gameType, oldGameType);
        recording.getActionDataFromEntity(player).addAction(gameModeAction);

    }

}