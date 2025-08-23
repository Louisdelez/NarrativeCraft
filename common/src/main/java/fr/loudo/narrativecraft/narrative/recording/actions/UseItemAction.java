package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import fr.loudo.narrativecraft.util.FakePlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public class UseItemAction extends Action {

    private final String handName;

    public UseItemAction(int tick, InteractionHand interactionHand) {
        super(tick, ActionType.USE_ITEM);
        handName = interactionHand.name();
    }

    @Override
    public void execute(PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof FakePlayer fakePlayer) {
            ItemStack itemStack = fakePlayer.getItemInHand(InteractionHand.valueOf(handName));
            if(itemStack.getItem() instanceof SpawnEggItem || itemStack.getItem() instanceof BoatItem) return;
            itemStack.setCount(2);
            itemStack.getItem().use(playbackData.getEntity().level(), fakePlayer, InteractionHand.valueOf(handName));
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {}
}
