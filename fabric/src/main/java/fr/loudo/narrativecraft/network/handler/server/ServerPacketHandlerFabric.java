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

package fr.loudo.narrativecraft.network.handler.server;

import fr.loudo.narrativecraft.network.data.*;
import fr.loudo.narrativecraft.network.handlers.ServerPacketHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ServerPacketHandlerFabric {

    public static void handle() {
        ServerPlayNetworking.registerGlobalReceiver(BiChapterDataPacket.TYPE, (payload, context) -> {
            ServerPacketHandler.chapterData(payload, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(BiSceneDataPacket.TYPE, (payload, context) -> {
            ServerPacketHandler.sceneData(payload, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(BiAnimationDataPacket.TYPE, (payload, context) -> {
            ServerPacketHandler.animationData(payload, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(BiCameraAngleDataPacket.TYPE, (payload, context) -> {
            ServerPacketHandler.cameraAngleData(payload, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(BiCutsceneDataPacket.TYPE, (payload, context) -> {
            ServerPacketHandler.cutsceneData(payload, context.player());
        });
    }
}
