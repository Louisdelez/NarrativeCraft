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
import fr.loudo.narrativecraft.narrative.recording.actions.SleepAction;
import fr.loudo.narrativecraft.narrative.recording.actions.SwingAction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract void remove(Entity.RemovalReason p_276115_);

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At(value = "HEAD"))
    private void narrativecraft$onSwing(InteractionHand hand, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            Recording recording =
                    NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
            if (recording == null || !recording.isRecording()) return;
            SwingAction action = new SwingAction(recording.getTick(), hand);
            recording.getActionDataFromEntity(player).addAction(action);
        }
    }

    @Inject(method = "startSleeping", at = @At("HEAD"))
    private void narrativecraft$startSleep(BlockPos pos, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            Recording recording =
                    NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
            if (recording == null || !recording.isRecording()) return;
            SleepAction sleepAction = new SleepAction(recording.getTick(), pos);
            recording.getActionDataFromEntity(player).addAction(sleepAction);
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void narrativecraft$hurtServer(
            ServerLevel serverLevel, DamageSource damageSource, float damageHit, CallbackInfoReturnable<Boolean> cir) {
        if (damageSource.getEntity() instanceof ServerPlayer player) {
            Recording recording =
                    NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
            if (recording == null || !recording.isRecording()) return;
            HurtAction hurtAction = new HurtAction(recording.getTick());
            LivingEntity livingEntity = (LivingEntity) (Object) this;
            ActionsData actionsData = recording.getActionDataFromEntity(livingEntity);
            if (actionsData == null) return;
            actionsData.addAction(hurtAction);
            recording.trackEntity(livingEntity, recording.getTick());
        }
    }
}
