package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;

public class DestroyBlockStageAction extends Action {

    private int id;
    private int x,y,z;
    private int progress;

    public DestroyBlockStageAction(int tick, int id, int x, int y, int z, int progress) {
        super(tick, ActionType.DESTROY_BLOCK_STAGE);
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.progress = progress;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        playbackData.getEntity().level().getServer().getPlayerList().broadcastAll(new ClientboundBlockDestructionPacket(id, new BlockPos(x, y, z), progress));
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        playbackData.getEntity().level().getServer().getPlayerList().broadcastAll(new ClientboundBlockDestructionPacket(id, new BlockPos(x, y, z), progress == 1 ? -1 : progress));
    }

    public int getProgress() {
        return progress;
    }

}
