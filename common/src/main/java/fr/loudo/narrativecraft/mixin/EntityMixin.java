package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.actions.HurtAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "markHurt", at = @At("HEAD"))
    private void narrativecraft$mark(CallbackInfo ci) {
        if((Object) this instanceof ServerPlayer player) {
            Recording recording = NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
            if(recording == null || !recording.isRecording()) return;
            HurtAction hurtAction = new HurtAction(recording.getTick());
            recording.getActionDataFromEntity(player).addAction(hurtAction);
        }
    }
}