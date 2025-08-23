package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.RecordingData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionDifferenceListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoat.class)
public abstract class AbstractBoatMixin {

    @Shadow protected abstract int getBubbleTime();

    @Shadow public abstract boolean getPaddleState(int p_363453_);

    @Inject(method = "tick", at = @At("RETURN"))
    private void narrativecraft$boatTick(CallbackInfo ci) {
        Recording recording = NarrativeCraftMod.getInstance().getRecordingManager().getRecording(Minecraft.getInstance().player);
        if(recording == null || !recording.isRecording()) return;
        AbstractBoat boat = (AbstractBoat) (Object) this;
        RecordingData recordingData = recording.getRecordingDataFromEntity(boat);
        if(recordingData == null || !boat.level().isClientSide) return;
        ActionDifferenceListener actionDifferenceListener = recordingData.getActionDifferenceListener();
        actionDifferenceListener.abstractBoatEntityBubbleListener(getBubbleTime());
        actionDifferenceListener.abstractBoatEntityPaddleListener(getPaddleState(0), getPaddleState(1));
    }

}