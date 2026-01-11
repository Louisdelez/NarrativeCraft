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
        if (playbackData.getEntity() instanceof FakePlayer fakePlayer) {
            ItemStack itemStack = fakePlayer.getItemInHand(InteractionHand.valueOf(handName));
            if (itemStack.getItem() instanceof SpawnEggItem || itemStack.getItem() instanceof BoatItem) return;
            itemStack.setCount(2);
            itemStack.getItem().use(playbackData.getEntity().getLevel(), fakePlayer, InteractionHand.valueOf(handName));
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {}
}
