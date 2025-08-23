package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.actions.BreakBlockAction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class OnBreakBlock {
    public static void breakBlock(BlockState blockState, BlockPos blockPos, Player serverPlayer) {
        Recording recording = NarrativeCraftMod.getInstance().getRecordingManager().getRecording(Minecraft.getInstance().player);
        if(recording == null || !recording.isRecording()) return;
        BreakBlockAction breakBlockAction = new BreakBlockAction(recording.getTick(), blockPos, blockState);
        recording.getActionDataFromEntity(serverPlayer).addAction(breakBlockAction);
    }
}