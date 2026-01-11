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

package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.RecordingData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionDifferenceListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class AbstractBoatMixin {

    @Shadow
    protected abstract int getBubbleTime();

    @Shadow
    public abstract boolean getPaddleState(int p_363453_);

    @Inject(method = "tick", at = @At("RETURN"))
    private void narrativecraft$boatTick(CallbackInfo ci) {
        if (NarrativeCraftMod.server == null) return;
        for (ServerPlayer player : NarrativeCraftMod.server.getPlayerList().getPlayers()) {
            Recording recording =
                    NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
            if (recording == null || !recording.isRecording()) return;
            Boat boat = (Boat) (Object) this;
            RecordingData recordingData = recording.getRecordingDataFromEntity(boat);
            if (recordingData == null || boat.getLevel().isClientSide()) return;
            ActionDifferenceListener actionDifferenceListener = recordingData.getActionDifferenceListener();
            actionDifferenceListener.abstractBoatEntityBubbleListener(getBubbleTime());
            actionDifferenceListener.abstractBoatEntityPaddleListener(getPaddleState(0), getPaddleState(1));
        }
    }
}
