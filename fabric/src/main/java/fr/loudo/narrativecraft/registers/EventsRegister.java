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

import fr.loudo.narrativecraft.events.*;
import fr.loudo.narrativecraft.keys.PressKeyListener;
import fr.loudo.narrativecraft.narrative.playback.PlaybackTickHandler;
import fr.loudo.narrativecraft.narrative.recording.RecordingTickHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class EventsRegister {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(LifecycleEvent::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(LifecycleEvent::onServerStop);
        ServerPlayConnectionEvents.JOIN.register(PlayerServerConnection::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(PlayerServerConnection::onPlayerLeave);
        ClientTickEvents.END_CLIENT_TICK.register(PressKeyListener::onPressKey);
        ClientTickEvents.END_CLIENT_TICK.register(OnClientTick::clientTick);
        ServerTickEvents.END_SERVER_TICK.register(RecordingTickHandler::tick);
        ServerTickEvents.END_SERVER_TICK.register(PlaybackTickHandler::tick);
        ServerTickEvents.END_SERVER_TICK.register(OnServerTick::tick);
        UseEntityCallback.EVENT.register(EntityRightClick::onEntityRightClick);
        ServerPlayerEvents.AFTER_RESPAWN.register(RespawnEvent::onRespawn);
        PlayerBlockBreakEvents.AFTER.register(BlockBreakEvent::onBlockBreak);
        UseBlockCallback.EVENT.register(RightClickBlock::onRightClickBlock);
        WorldRenderEvents.LAST.register(RenderWorldEvent::renderWorld);
    }
}
