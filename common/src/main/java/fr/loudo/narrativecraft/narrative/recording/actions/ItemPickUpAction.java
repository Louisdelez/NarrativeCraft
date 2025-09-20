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
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import fr.loudo.narrativecraft.util.FakePlayer;
import net.minecraft.world.entity.item.ItemEntity;

public class ItemPickUpAction extends Action {

    private final int entityRecordingId;

    public ItemPickUpAction(int tick, int entityRecordingId) {
        super(tick, ActionType.ITEM_PICKUP);
        this.entityRecordingId = entityRecordingId;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        if (playbackData.getEntity() instanceof FakePlayer fakePlayer) {
            ItemEntity item = (ItemEntity) playbackData.getPlayback().getEntityByRecordId(entityRecordingId);
            if (item == null) return;
            fakePlayer.take(item, item.getItem().getCount());
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        PlaybackData itemPlayback = playbackData.getPlayback().getPlaybackDataByRecordId(entityRecordingId);
        itemPlayback.killEntity();
        Location takeLoc = playbackData.getActionsData().getLocations().get(tick);
        if (takeLoc == null) return;
        itemPlayback.spawnEntity(takeLoc);
    }
}
