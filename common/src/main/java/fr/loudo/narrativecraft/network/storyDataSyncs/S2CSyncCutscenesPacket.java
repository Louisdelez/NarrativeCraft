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

package fr.loudo.narrativecraft.network.storyDataSyncs;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record S2CSyncCutscenesPacket(int chapterIndex, String sceneName, List<Cutscene> cutscenes)
        implements CustomPacketPayload {

    public static final Type<S2CSyncCutscenesPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "nc_sync_cutscenes"));

    public static final StreamCodec<ByteBuf, Cutscene> CUTSCENE_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            Cutscene::getName,
            ByteBufCodecs.STRING_UTF8,
            Cutscene::getDescription,
            Cutscene::new);

    public static final StreamCodec<ByteBuf, S2CSyncCutscenesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            S2CSyncCutscenesPacket::chapterIndex,
            ByteBufCodecs.STRING_UTF8,
            S2CSyncCutscenesPacket::sceneName,
            CUTSCENE_STREAM_CODEC.apply(ByteBufCodecs.list()),
            S2CSyncCutscenesPacket::cutscenes,
            S2CSyncCutscenesPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
