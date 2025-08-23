package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recording.actions.ItemPickUpAction;
import fr.loudo.narrativecraft.narrative.recording.actions.RidingAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerCommonMixin {

    @Inject(method = "startRiding", at = @At(value = "HEAD"))
    private void narrativecraft$rideEntity(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        Recording recording = NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
        if(recording == null || !recording.isRecording()) return;
        ActionsData vehicleActionsData = recording.getActionDataFromEntity(entity);
        RidingAction ridingAction = new RidingAction(recording.getTick(), vehicleActionsData.getEntityIdRecording());
        recording.getActionDataFromEntity(player).getActions().add(ridingAction);
    }

    @Inject(method = "onItemPickup", at = @At(value = "HEAD"))
    private void narrativecraft$itemPickUp(ItemEntity itemEntity, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        Recording recording = NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
        if(recording == null || !recording.isRecording()) return;
        ItemPickUpAction action = new ItemPickUpAction(recording.getTick(), recording.getActionDataFromEntity(itemEntity).getEntityIdRecording());
        recording.getActionDataFromEntity(player).addAction(action);
    }
}