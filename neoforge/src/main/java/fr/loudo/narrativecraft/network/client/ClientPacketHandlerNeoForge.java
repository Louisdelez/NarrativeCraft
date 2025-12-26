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

package fr.loudo.narrativecraft.network.client;

import fr.loudo.narrativecraft.network.handlers.ClientPacketHandler;
import fr.loudo.narrativecraft.network.screen.*;
import fr.loudo.narrativecraft.network.storyDataSyncs.*;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPacketHandlerNeoForge {

    public static void screenHandler(final S2CScreenPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.screenHandler(packet);
        });
    }

    public static void syncChaptersHandler(final S2CSyncChaptersPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.syncChaptersHandler(packet);
        });
    }

    public static void syncScenesHandler(final S2CSyncScenesPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.syncScenesHandler(packet);
        });
    }

    public static void syncAnimationsHandler(final S2CSyncAnimationsPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.syncAnimationsHandler(packet);
        });
    }

    public static void syncCameraAnglesHandler(final S2CSyncCameraAnglesPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.syncCameraAnglesHandler(packet);
        });
    }

    public static void syncSubscenesHandler(final S2CSyncSubscenesPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.syncSubscenesHandler(packet);
        });
    }

    public static void syncCutscenesHandler(final S2CSyncCutscenesPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.syncCutscenesHandler(packet);
        });
    }

    public static void syncInteractionsHandler(final S2CSyncInteractionsPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.syncInteractionsHandler(packet);
        });
    }

    public static void syncNpcsHandler(final S2CSyncNpcsPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.syncNpcsHandler(packet);
        });
    }

    public static void openSceneScreen(S2CSceneScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.openSceneScreen(packet);
        });
    }

    public static void openAnimationsScreen(S2CAnimationsScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.openAnimationsScreen(packet);
        });
    }

    public static void openCameraAngleScreen(S2CCameraAnglesScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.openCameraAnglesScreen(packet);
        });
    }

    public static void openCutsceneScreen(S2CCutscenesScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.openCutscenesScreen(packet);
        });
    }
}
