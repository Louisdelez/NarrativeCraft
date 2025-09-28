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

package fr.loudo.narrativecraft.narrative.recording.actions.manager;

import fr.loudo.narrativecraft.narrative.recording.actions.*;

public enum ActionType {
    SWING(SwingAction.class),
    POSE(PoseAction.class),
    ENTITY_BYTE(EntityByteAction.class),
    LIVING_ENTITY_BYTE(LivingEntityByteAction.class),
    ITEM_CHANGE(ItemChangeAction.class),
    HURT(HurtAction.class),
    BLOCK_PLACE(PlaceBlockAction.class),
    BLOCK_BREAK(BreakBlockAction.class),
    DESTROY_BLOCK_STAGE(DestroyBlockStageAction.class),
    RIGHT_CLICK_BLOCK(RightClickBlockAction.class),
    EMOTE(EmoteAction.class),
    SLEEP(SleepAction.class),
    USE_ITEM(UseItemAction.class),
    GAMEMODE(GameModeAction.class),
    RIDE(RidingAction.class),
    STOP_RIDE(StopRidingAction.class),
    ABSTRACT_HORSE_BYTE(AbstractHorseByteAction.class),
    ABSTRACT_BOAT_BUBBLE(BoatBubbleAction.class),
    ABSTRACT_BOAT_PADDLE(BoatPaddleAction.class),
    DEATH(DeathAction.class),
    RESPAWN(RespawnAction.class),
    ITEM_PICKUP(ItemPickUpAction.class);

    private final Class<? extends Action> actionClass;

    ActionType(Class<? extends Action> actionClass) {
        this.actionClass = actionClass;
    }

    public Class<? extends Action> getActionClass() {
        return actionClass;
    }
}
