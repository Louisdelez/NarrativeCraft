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

package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.world.damagesource.DamageSource;

/**
 * MC 1.19.x version of HurtAction.
 * Key differences from 1.20.x+:
 * - Uses entity.getLevel() instead of entity.level()
 */
public class HurtAction extends Action {
    public HurtAction(int waitTick) {
        super(waitTick, ActionType.HURT);
    }

    @Override
    public void execute(PlaybackData playbackData) {
        // 1.19.x: Use entity.getLevel() instead of entity.level()
        playbackData
                .getEntity()
                .getLevel()
                .getServer()
                .getPlayerList()
                .broadcastAll(new ClientboundHurtAnimationPacket(
                        playbackData.getEntity().getId(), 0F));
        DamageSource damageSource = new DamageSource(
                playbackData.getEntity().damageSources().generic().typeHolder());
        playbackData.getEntity().handleDamageEvent(damageSource);
    }

    @Override
    public void rewind(PlaybackData playbackData) {}
}
