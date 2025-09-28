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

import fr.loudo.narrativecraft.mixin.accessor.AbstractHorseAccessor;
import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class AbstractHorseByteAction extends Action {

    private final byte currentByte;
    private final byte oldByte;

    public AbstractHorseByteAction(int tick, byte currentByte, byte oldByte) {
        super(tick, ActionType.ABSTRACT_HORSE_BYTE);
        this.currentByte = currentByte;
        this.oldByte = oldByte;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        if (playbackData.getEntity() instanceof AbstractHorse abstractHorse) {
            playbackData.getEntity().getEntityData().set(AbstractHorseAccessor.getDATA_ID_FLAGS(), currentByte);
            if (currentByte >= AbstractHorseAccessor.getFLAG_STANDING()) {
                if (!abstractHorse.isSaddled()) {
                    abstractHorse.ejectPassengers();
                }
            }
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        if (playbackData.getEntity() instanceof AbstractHorse) {
            playbackData.getEntity().getEntityData().set(AbstractHorseAccessor.getDATA_ID_FLAGS(), oldByte);
        }
    }
}
