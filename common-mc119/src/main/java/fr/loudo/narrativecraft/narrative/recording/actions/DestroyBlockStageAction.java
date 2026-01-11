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
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;

/**
 * MC 1.19.x version of DestroyBlockStageAction.
 * Key differences from 1.20.x+:
 * - Uses entity.getLevel() instead of entity.level()
 */
public class DestroyBlockStageAction extends Action {

    private int id;
    private int x, y, z;
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
        // 1.19.x: Use entity.getLevel() instead of entity.level()
        playbackData
                .getEntity()
                .getLevel()
                .getServer()
                .getPlayerList()
                .broadcastAll(new ClientboundBlockDestructionPacket(id, new BlockPos(x, y, z), progress));
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        // 1.19.x: Use entity.getLevel() instead of entity.level()
        playbackData
                .getEntity()
                .getLevel()
                .getServer()
                .getPlayerList()
                .broadcastAll(new ClientboundBlockDestructionPacket(
                        id, new BlockPos(x, y, z), progress == 1 ? -1 : progress));
    }

    public int getProgress() {
        return progress;
    }
}
