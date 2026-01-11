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

package fr.loudo.narrativecraft.util;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * MC 1.19.x version of FakePlayer.
 * Key differences from 1.20.x+:
 * - No ClientInformation class - uses direct ServerPlayer constructor
 * - No CommonListenerCookie - uses simpler ServerGamePacketListenerImpl constructor
 * - Different packet handler signatures
 * - Simplified packet listener that ignores all incoming packets
 */
public class FakePlayer extends ServerPlayer {

    public FakePlayer(ServerLevel level, GameProfile profile) {
        // 1.19.x: ServerPlayer constructor doesn't take ClientInformation
        super(level.getServer(), level, profile);
        this.connection = new FakePlayerNetHandler(level.getServer(), this);
        setInvulnerable(true);
    }

    public static FakePlayer createRandom(ServerLevel level) {
        UUID uuid = UUID.randomUUID();
        return new FakePlayer(level, new GameProfile(uuid, uuid.toString()));
    }

    @Override
    public void displayClientMessage(@NotNull Component chatComponent, boolean actionBar) {}

    @Override
    public void awardStat(@NotNull Stat<?> stat, int amount) {}

    @Override
    public void die(@NotNull DamageSource source) {}

    @Override
    public void tick() {}

    /**
     * MC 1.19.x: Simplified fake packet listener.
     * Instead of overriding every single packet handler method, we extend the base class
     * and use a dummy connection that ignores all packets.
     */
    private static class FakePlayerNetHandler extends ServerGamePacketListenerImpl {
        private static final Connection DUMMY_CONNECTION = new DummyConnection(PacketFlow.CLIENTBOUND);

        public FakePlayerNetHandler(MinecraftServer server, ServerPlayer player) {
            // 1.19.x: Simpler constructor - no CommonListenerCookie
            super(server, DUMMY_CONNECTION, player);
        }

        @Override
        public void tick() {}

        @Override
        public void resetPosition() {}

        @Override
        public void disconnect(Component message) {}

        @Override
        public void send(Packet<?> packet) {}

        @Override
        public void send(Packet<?> packet, @Nullable PacketSendListener listener) {}
    }

    /**
     * MC 1.19.x: DummyConnection that ignores all packet sending.
     * Only override methods that exist in 1.19.4's Connection class.
     */
    private static class DummyConnection extends Connection {
        public DummyConnection(PacketFlow packetFlow) {
            super(packetFlow);
        }

        @Override
        public void send(Packet<?> packet) {}

        @Override
        public void send(Packet<?> packet, @Nullable PacketSendListener listener) {}
    }
}
