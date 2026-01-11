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

package fr.loudo.narrativecraft.platform.services;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * MC 1.19.x version of IPacketSender.
 * Uses the old custom payload packet system instead of CustomPacketPayload.
 * 1.19.x does not have CustomPacketPayload - it uses FriendlyByteBuf directly.
 */
public interface IPacketSender {

    /**
     * Send a custom packet to a player.
     * In 1.19.x, this uses the old custom payload channel system.
     *
     * @param player The player to send to
     * @param channelId The channel identifier
     * @param data The packet data
     */
    void sendToPlayer(ServerPlayer player, ResourceLocation channelId, FriendlyByteBuf data);

    /**
     * Compatibility method for sending empty/simple packets.
     * @param player The player to send to
     * @param channelId The channel identifier
     */
    default void sendToPlayer(ServerPlayer player, ResourceLocation channelId) {
        // Default implementation sends empty buffer
        sendToPlayer(player, channelId, null);
    }
}
