package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recording.actions.DeathAction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

public class OnDeath {

    public static void death(LivingEntity entity) {
        Recording recording = NarrativeCraftMod.getInstance().getRecordingManager().getRecording(Minecraft.getInstance().player);
        if(recording == null || !recording.isRecording()) return;
        ActionsData actionsData = recording.getActionDataFromEntity(entity);
        if(actionsData == null) return;
        DeathAction deathAction = new DeathAction(recording.getTick(), actionsData.getEntityIdRecording());
        actionsData.addAction(deathAction);
    }

}