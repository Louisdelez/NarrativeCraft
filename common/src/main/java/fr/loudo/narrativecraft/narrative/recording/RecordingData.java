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

package fr.loudo.narrativecraft.narrative.recording;

import fr.loudo.narrativecraft.narrative.recording.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionDifferenceListener;
import net.minecraft.world.entity.Entity;

public class RecordingData {

    private final Entity entity;
    private final ActionDifferenceListener actionDifferenceListener;
    private final ActionsData actionsData;
    private boolean savingTrack;

    public RecordingData(Entity entity, Recording recording) {
        this.entity = entity;
        actionsData = new ActionsData(entity, recording.getTick());
        actionDifferenceListener = new ActionDifferenceListener(actionsData, recording);
        savingTrack = false;
    }

    public boolean isSameEntity(Entity entity) {
        return this.entity.getUUID().equals(entity.getUUID());
    }

    public Entity getEntity() {
        return entity;
    }

    public ActionDifferenceListener getActionDifferenceListener() {
        return actionDifferenceListener;
    }

    public ActionsData getActionsData() {
        return actionsData;
    }

    public boolean isSavingTrack() {
        return savingTrack;
    }

    public void setSavingTrack(boolean savingTrack) {
        this.savingTrack = savingTrack;
    }
}
