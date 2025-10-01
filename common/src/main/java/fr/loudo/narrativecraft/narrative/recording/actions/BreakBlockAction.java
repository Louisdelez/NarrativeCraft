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

package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class BreakBlockAction extends Action {

    private int x, y, z;
    private String data;

    public BreakBlockAction(int tick, BlockPos blockPos, BlockState blockState) {
        super(tick, ActionType.BLOCK_BREAK);
        this.x = blockPos.getX();
        this.y = blockPos.getY();
        this.z = blockPos.getZ();
        this.data = NbtUtils.writeBlockState(blockState).toString();
    }

    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }

    public String getData() {
        return data;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        BlockPos blockPos = new BlockPos(x, y, z);
        BlockState blockState = Util.getBlockStateFromData(
                data, playbackData.getEntity().level().registryAccess());
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
        playbackData.getLevel().destroyBlock(blockPos, false);
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        if (!(playbackData.getEntity() instanceof LivingEntity)) return;
        BlockState blockState = Util.getBlockStateFromData(
                data, playbackData.getEntity().level().registryAccess());
        if (blockState == null) return;
        BlockPos blockPos = new BlockPos(x, y, z);
        Block block = blockState.getBlock();
        if (block instanceof DoorBlock) {
            if (blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                blockState = blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
                blockPos = blockPos.below();
            }
        } else if (blockState.getBlock() instanceof BedBlock) {
            if (blockState.getValue(BedBlock.PART) == BedPart.HEAD) {
                Direction direction = blockState.getValue(BedBlock.FACING);
                blockState = blockState.setValue(BedBlock.PART, BedPart.FOOT);
                blockPos = blockPos.relative(direction.getOpposite());
            }
        }
        playbackData.getLevel().setBlock(blockPos, blockState, 3);
        if (block instanceof BedBlock || block instanceof DoorBlock) {
            block.setPlacedBy(
                    playbackData.getEntity().level(),
                    blockPos,
                    blockState,
                    (LivingEntity) playbackData.getEntity(),
                    blockState.getBlock().asItem().getDefaultInstance());
        }
    }
}
