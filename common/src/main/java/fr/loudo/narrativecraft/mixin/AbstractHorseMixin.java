package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.RecordingData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionDifferenceListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorse.class)
public class AbstractHorseMixin {

    @Shadow @Final private static EntityDataAccessor<Byte> DATA_ID_FLAGS;

    @Inject(method = "tick", at = @At("RETURN"))
    private void narrativecraft$horseTick(CallbackInfo ci) {
        Recording recording = NarrativeCraftMod.getInstance().getRecordingManager().getRecording(Minecraft.getInstance().player);
        if(recording == null || !recording.isRecording()) return;
        AbstractHorse horse = (AbstractHorse) (Object) this;
        RecordingData recordingData = recording.getRecordingDataFromEntity(horse);
        if(recordingData == null || !horse.level().isClientSide) return;
        ActionDifferenceListener actionDifferenceListener = recordingData.getActionDifferenceListener();
        actionDifferenceListener.abstractHorseEntityByteListener(horse.getEntityData().get(DATA_ID_FLAGS));
    }

}