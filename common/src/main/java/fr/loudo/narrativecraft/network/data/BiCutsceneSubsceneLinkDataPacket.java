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
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BiCutsceneSubsceneLinkDataPacket(
        int chapterIndex, String sceneName, String cutsceneName, List<String> subsceneNames)
        implements CustomPacketPayload {

    public static final Type<BiCutsceneSubsceneLinkDataPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "nc_cutscene_subscene_link_data"));

    public static final StreamCodec<ByteBuf, BiCutsceneSubsceneLinkDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            BiCutsceneSubsceneLinkDataPacket::chapterIndex,
            ByteBufCodecs.STRING_UTF8,
            BiCutsceneSubsceneLinkDataPacket::sceneName,
            ByteBufCodecs.STRING_UTF8,
            BiCutsceneSubsceneLinkDataPacket::cutsceneName,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            BiCutsceneSubsceneLinkDataPacket::subsceneNames,
            BiCutsceneSubsceneLinkDataPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
