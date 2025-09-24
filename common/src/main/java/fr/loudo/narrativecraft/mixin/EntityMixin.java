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
import fr.loudo.narrativecraft.narrative.recording.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recording.actions.HurtAction;
import fr.loudo.narrativecraft.narrative.recording.actions.StopRidingAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    @Nullable
    public abstract Entity getVehicle();

    @Inject(method = "markHurt", at = @At("HEAD"))
    private void narrativecraft$mark(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            Recording recording =
                    NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
            if (recording == null || !recording.isRecording()) return;
            HurtAction hurtAction = new HurtAction(recording.getTick());
            recording.getActionDataFromEntity(player).addAction(hurtAction);
        }
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void narrativecraft$stopRiding(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            Entity entity = this.getVehicle();
            Recording recording =
                    NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
            if (recording == null || !recording.isRecording()) return;
            ActionsData vehicleActionsData = recording.getActionDataFromEntity(entity);
            if (vehicleActionsData == null) return;
            StopRidingAction stopRidingAction =
                    new StopRidingAction(recording.getTick(), vehicleActionsData.getEntityIdRecording());
            recording.getActionDataFromEntity(player).addAction(stopRidingAction);
        }
    }
}
