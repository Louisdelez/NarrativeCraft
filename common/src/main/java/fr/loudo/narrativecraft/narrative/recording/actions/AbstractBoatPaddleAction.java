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

import fr.loudo.narrativecraft.mixin.accessor.AbstractBoatAccessor;
import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractBoat;

public class AbstractBoatPaddleAction extends Action {

    private final boolean leftPaddle;
    private final boolean rightPaddle;

    private final boolean oldLeftPaddle;
    private final boolean oldRightPaddle;

    public AbstractBoatPaddleAction(
            int tick, boolean leftPaddle, boolean rightPaddle, boolean oldLeftPaddle, boolean oldRightPaddle) {
        super(tick, ActionType.ABSTRACT_BOAT_PADDLE);
        this.leftPaddle = leftPaddle;
        this.rightPaddle = rightPaddle;
        this.oldLeftPaddle = oldLeftPaddle;
        this.oldRightPaddle = oldRightPaddle;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        Entity entity1 = playbackData
                .getEntity()
                .level()
                .getEntity(playbackData.getEntity().getId());
        if (entity1 instanceof AbstractBoat) {
            playbackData.getEntity().getEntityData().set(AbstractBoatAccessor.getDATA_ID_PADDLE_LEFT(), leftPaddle);
            playbackData.getEntity().getEntityData().set(AbstractBoatAccessor.getDATA_ID_PADDLE_RIGHT(), rightPaddle);
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        Entity entity1 = playbackData
                .getEntity()
                .level()
                .getEntity(playbackData.getEntity().getId());
        if (entity1 instanceof AbstractBoat) {
            playbackData.getEntity().getEntityData().set(AbstractBoatAccessor.getDATA_ID_PADDLE_LEFT(), oldLeftPaddle);
            playbackData
                    .getEntity()
                    .getEntityData()
                    .set(AbstractBoatAccessor.getDATA_ID_PADDLE_RIGHT(), oldRightPaddle);
        }
    }
}
