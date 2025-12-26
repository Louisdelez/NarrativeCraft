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

package fr.loudo.narrativecraft.handler.client;

import fr.loudo.narrativecraft.network.data.BiAnimationDataPacket;
import fr.loudo.narrativecraft.network.data.BiChapterDataPacket;
import fr.loudo.narrativecraft.network.data.BiSceneDataPacket;
import fr.loudo.narrativecraft.network.handlers.ClientPacketHandler;
import fr.loudo.narrativecraft.network.screen.S2CAnimationsScreenPacket;
import fr.loudo.narrativecraft.network.screen.S2CSceneScreenPacket;
import fr.loudo.narrativecraft.network.screen.S2CScreenPacket;
import fr.loudo.narrativecraft.network.storyDataSyncs.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientPacketHandlerFabric {

    public static void handle() {
        ClientPlayNetworking.registerGlobalReceiver(S2CScreenPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.screenHandler(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CSceneScreenPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.openSceneScreen(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CAnimationsScreenPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.openAnimationsScreen(payload);
        });

        ClientPlayNetworking.registerGlobalReceiver(S2CSyncChaptersPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.syncChaptersHandler(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncScenesPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.syncScenesHandler(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncAnimationsPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.syncAnimationsHandler(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncSubscenesPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.syncSubscenesHandler(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncCutscenesPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.syncCutscenesHandler(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncInteractionsPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.syncInteractionsHandler(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncNpcsPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.syncNpcsHandler(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncCameraAnglesPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.syncCameraAnglesHandler(payload);
        });

        ClientPlayNetworking.registerGlobalReceiver(BiChapterDataPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.chapterData(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(BiSceneDataPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.sceneData(payload);
        });
        ClientPlayNetworking.registerGlobalReceiver(BiAnimationDataPacket.TYPE, (payload, context) -> {
            ClientPacketHandler.animationData(payload);
        });
    }
}
