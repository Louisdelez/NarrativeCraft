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
import fr.loudo.narrativecraft.narrative.character.CharacterModel;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.narrative.character.MainCharacterAttribute;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record S2CSyncCharactersPacket(List<CharacterStory> characters) implements CustomPacketPayload {

    public static final Type<S2CSyncCharactersPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "nc_sync_characters"));

    public static final StreamCodec<ByteBuf, MainCharacterAttribute> MAIN_CHARACTER_ATTRIBUTE_STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    MainCharacterAttribute::isMainCharacter,
                    ByteBufCodecs.BOOL,
                    MainCharacterAttribute::isSameSkinAsPlayer,
                    ByteBufCodecs.BOOL,
                    MainCharacterAttribute::isSameSkinAsTheir,
                    MainCharacterAttribute::new);

    public static final StreamCodec<ByteBuf, CharacterStory> CHARACTER_STORY_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CharacterStory::getName,
            ByteBufCodecs.STRING_UTF8,
            CharacterStory::getDescription,
            ByteBufCodecs.STRING_UTF8,
            CharacterStory::getBirthDate,
            ByteBufCodecs.idMapper(i -> CharacterModel.values()[i], CharacterModel::ordinal),
            CharacterStory::getModel,
            ByteBufCodecs.idMapper(i -> CharacterType.values()[i], CharacterType::ordinal),
            CharacterStory::getCharacterType,
            ByteBufCodecs.BOOL,
            CharacterStory::showNametag,
            MAIN_CHARACTER_ATTRIBUTE_STREAM_CODEC,
            CharacterStory::getMainCharacterAttribute,
            CharacterStory::new);

    public static final StreamCodec<ByteBuf, S2CSyncCharactersPacket> STREAM_CODEC = StreamCodec.composite(
            CHARACTER_STORY_STREAM_CODEC.apply(ByteBufCodecs.list()),
            S2CSyncCharactersPacket::characters,
            S2CSyncCharactersPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
