package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class PlaceBlockAction extends Action {

    private int x, y, z;
    private String data;

    public PlaceBlockAction(int tick, BlockPos blockPos, BlockState blockState) {
        super(tick, ActionType.BLOCK_PLACE);
        this.x = blockPos.getX();
        this.y = blockPos.getY();
        this.z = blockPos.getZ();
        this.data = NbtUtils.writeBlockState(blockState).toString();
    }

    @Override
    public void execute(PlaybackData playbackData) {
        BlockState blockState = Util.getBlockStateFromData(data, playbackData.getEntity().registryAccess());
        if(blockState == null) return;
        BlockPos blockPos = new BlockPos(x, y, z);
        playbackData.getLevel().setBlock(blockPos, blockState, 3);
        SoundType soundType = blockState.getSoundType();
        playbackData.getLevel().playSound(playbackData.getEntity() , blockPos, blockState.getSoundType().getPlaceSound(),
                SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);

    }

    public void rewind(PlaybackData playbackData) {
        BlockPos blockPos = getBlockPos();
        BlockState blockState = Util.getBlockStateFromData(data, playbackData.getEntity().registryAccess());
        if (blockState != null) {
            if (blockState.getBlock() instanceof BedBlock) {
                if (blockState.getValue(BedBlock.PART) == BedPart.FOOT) {
                    Direction direction = blockState.getValue(BedBlock.FACING);
                    blockPos = blockPos.relative(direction);
                }
            } else if (blockState.getBlock() instanceof DoorBlock) {
                if (blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                    blockPos = blockPos.below();
                }
            }
        }
        playbackData.getLevel().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }
}
