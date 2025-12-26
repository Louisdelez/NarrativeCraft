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

package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.network.data.BiAnimationDataPacket;
import fr.loudo.narrativecraft.network.data.BiChapterDataPacket;
import fr.loudo.narrativecraft.network.data.BiSceneDataPacket;
import fr.loudo.narrativecraft.network.screen.S2CAnimationsScreenPacket;
import fr.loudo.narrativecraft.network.screen.S2CSceneScreenPacket;
import fr.loudo.narrativecraft.network.screen.S2CScreenPacket;
import fr.loudo.narrativecraft.network.storyDataSyncs.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PacketFabricRegister {

    public static void register() {
        registerS2C();
        registerBi();
    }

    private static void registerS2C() {
        PayloadTypeRegistry.playS2C().register(S2CScreenPacket.TYPE, S2CScreenPacket.STREAM_CODEC);

        PayloadTypeRegistry.playS2C().register(S2CSyncChaptersPacket.TYPE, S2CSyncChaptersPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSyncScenesPacket.TYPE, S2CSyncScenesPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSyncAnimationsPacket.TYPE, S2CSyncAnimationsPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSyncCameraAnglesPacket.TYPE, S2CSyncCameraAnglesPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSyncSubscenesPacket.TYPE, S2CSyncSubscenesPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSyncCutscenesPacket.TYPE, S2CSyncCutscenesPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSyncInteractionsPacket.TYPE, S2CSyncInteractionsPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSyncNpcsPacket.TYPE, S2CSyncNpcsPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSceneScreenPacket.TYPE, S2CSceneScreenPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CAnimationsScreenPacket.TYPE, S2CAnimationsScreenPacket.STREAM_CODEC);
    }

    private static void registerBi() {
        PayloadTypeRegistry.playS2C().register(BiChapterDataPacket.TYPE, BiChapterDataPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BiChapterDataPacket.TYPE, BiChapterDataPacket.STREAM_CODEC);

        PayloadTypeRegistry.playS2C().register(BiSceneDataPacket.TYPE, BiSceneDataPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BiSceneDataPacket.TYPE, BiSceneDataPacket.STREAM_CODEC);

        PayloadTypeRegistry.playS2C().register(BiAnimationDataPacket.TYPE, BiAnimationDataPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BiAnimationDataPacket.TYPE, BiAnimationDataPacket.STREAM_CODEC);
    }
}
