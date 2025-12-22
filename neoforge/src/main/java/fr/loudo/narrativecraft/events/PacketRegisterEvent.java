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

package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.network.*;
import fr.loudo.narrativecraft.network.client.ClientPacketHandlerNeoForge;
import fr.loudo.narrativecraft.network.common.CommonPackerHandlerNeoForge;
import fr.loudo.narrativecraft.network.data.BiChapterDataPacket;
import fr.loudo.narrativecraft.network.storyDataSyncs.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(NarrativeCraftMod.MOD_ID)
public class PacketRegisterEvent {

    public PacketRegisterEvent(IEventBus modBus) {
        modBus.addListener(PacketRegisterEvent::onPackerRegister);
    }

    private static void onPackerRegister(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                S2CScreenPacket.TYPE, S2CScreenPacket.STREAM_CODEC, ClientPacketHandlerNeoForge::screenHandler);

        // Story data syncers
        registrar.playToClient(
                S2CSyncChaptersPacket.TYPE,
                S2CSyncChaptersPacket.STREAM_CODEC,
                ClientPacketHandlerNeoForge::syncChaptersHandler);
        registrar.playToClient(
                S2CSyncScenesPacket.TYPE,
                S2CSyncScenesPacket.STREAM_CODEC,
                ClientPacketHandlerNeoForge::syncScenesHandler);
        registrar.playToClient(
                S2CSyncAnimationsPacket.TYPE,
                S2CSyncAnimationsPacket.STREAM_CODEC,
                ClientPacketHandlerNeoForge::syncAnimationsHandler);
        registrar.playToClient(
                S2CSyncCameraAnglesPacket.TYPE,
                S2CSyncCameraAnglesPacket.STREAM_CODEC,
                ClientPacketHandlerNeoForge::syncCameraAnglesHandler);
        registrar.playToClient(
                S2CSyncSubscenesPacket.TYPE,
                S2CSyncSubscenesPacket.STREAM_CODEC,
                ClientPacketHandlerNeoForge::syncSubscenesHandler);
        registrar.playToClient(
                S2CSyncCutscenesPacket.TYPE,
                S2CSyncCutscenesPacket.STREAM_CODEC,
                ClientPacketHandlerNeoForge::syncCutscenesHandler);
        registrar.playToClient(
                S2CSyncInteractionsPacket.TYPE,
                S2CSyncInteractionsPacket.STREAM_CODEC,
                ClientPacketHandlerNeoForge::syncInteractionsHandler);
        registrar.playToClient(
                S2CSyncNpcsPacket.TYPE, S2CSyncNpcsPacket.STREAM_CODEC, ClientPacketHandlerNeoForge::syncNpcsHandler);

        // Story Data

        registrar.playBidirectional(
                BiChapterDataPacket.TYPE, BiChapterDataPacket.STREAM_CODEC, CommonPackerHandlerNeoForge::chapterData);
    }
}
