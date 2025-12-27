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

package fr.loudo.narrativecraft.network.data;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.character.CharacterModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BiNpcDataPacket(
        String name,
        String description,
        CharacterModel characterModel,
        boolean showNametag,
        int chapterIndex,
        String sceneName,
        String npcName,
        TypeStoryData typeStoryData)
        implements CustomPacketPayload {

    public static final Type<BiNpcDataPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "nc_npc_data"));

    public static final StreamCodec<ByteBuf, BiNpcDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            BiNpcDataPacket::name,
            ByteBufCodecs.STRING_UTF8,
            BiNpcDataPacket::description,
            ByteBufCodecs.idMapper(i -> CharacterModel.values()[i], CharacterModel::ordinal),
            BiNpcDataPacket::characterModel,
            ByteBufCodecs.BOOL,
            BiNpcDataPacket::showNametag,
            ByteBufCodecs.INT,
            BiNpcDataPacket::chapterIndex,
            ByteBufCodecs.STRING_UTF8,
            BiNpcDataPacket::sceneName,
            ByteBufCodecs.STRING_UTF8,
            BiNpcDataPacket::npcName,
            ByteBufCodecs.idMapper(i -> TypeStoryData.values()[i], TypeStoryData::ordinal),
            BiNpcDataPacket::typeStoryData,
            BiNpcDataPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
