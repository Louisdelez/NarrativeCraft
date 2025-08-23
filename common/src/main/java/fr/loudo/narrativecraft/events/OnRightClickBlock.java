package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.actions.RightClickBlockAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class OnRightClickBlock {

    public static void onRightClick(Direction direction, BlockPos blockPos, InteractionHand hand, boolean inside, Player serverPlayer) {
        Recording recording = NarrativeCraftMod.getInstance().getRecordingManager().getRecording(serverPlayer);
        if(recording == null || !recording.isRecording()) return;
        RightClickBlockAction rightClickBlockAction = new RightClickBlockAction(
                recording.getTick(),
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ(),
                direction.name(),
                hand.name(),
                inside
        );
        recording.getActionDataFromEntity(serverPlayer).addAction(rightClickBlockAction);
    }

}