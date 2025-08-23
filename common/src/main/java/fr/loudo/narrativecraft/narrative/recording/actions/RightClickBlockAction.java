package fr.loudo.narrativecraft.narrative.recording.actions;

import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import fr.loudo.narrativecraft.util.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RightClickBlockAction extends Action {

    private int x, y, z;
    private String directionName;
    private String handName;
    private boolean inside;

    public RightClickBlockAction(int tick, int x, int y, int z, String directionName, String handName, boolean inside) {
        super(tick, ActionType.RIGHT_CLICK_BLOCK);
        this.x = x;
        this.y = y;
        this.z = z;
        this.directionName = directionName;
        this.handName = handName;
        this.inside = inside;
    }

    public void execute(PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof FakePlayer fakePlayer) {
            BlockPos blockPos = new BlockPos(x, y, z);
            ItemStack itemStack = fakePlayer.getItemInHand(InteractionHand.valueOf(handName));
            if(itemStack.getItem() instanceof SpawnEggItem
                    || itemStack.getItem() instanceof BoatItem
                    || !itemStack.isDamageableItem()
            ) return;
            BlockState blockState = fakePlayer.level().getBlockState(blockPos);
            itemStack.setCount(2);
            BlockHitResult blockHitResult = new BlockHitResult(
                    new Vec3(x, y, z),
                    Direction.valueOf(directionName),
                    blockPos,
                    inside
            );
            UseOnContext useOnContext = new UseOnContext(
                    fakePlayer,
                    InteractionHand.valueOf(handName),
                    blockHitResult
            );

            InteractionResult result = blockState.useWithoutItem(fakePlayer.level(), fakePlayer, blockHitResult);
            if(!result.consumesAction()) {
                itemStack.getItem().useOn(useOnContext);
            }
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {}
}
