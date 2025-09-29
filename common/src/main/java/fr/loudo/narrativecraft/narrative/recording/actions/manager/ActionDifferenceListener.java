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

import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.narrative.recording.Recording;
import fr.loudo.narrativecraft.narrative.recording.actions.*;
import fr.loudo.narrativecraft.narrative.recording.actions.modsListeners.EmoteCraftListeners;
import fr.loudo.narrativecraft.narrative.recording.actions.modsListeners.ModsListenerImpl;
import fr.loudo.narrativecraft.platform.Services;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ActionDifferenceListener {

    private final List<EquipmentSlot> equipmentSlotList = Arrays.asList(
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND,
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET);

    private final ActionsData actionsData;
    private final Recording recording;
    private Pose poseState;

    private byte entityByteState;
    private byte livingEntityByteState;

    private byte abstractHorseEntityByteState;

    private int BoatEntityBubbleState;
    private boolean BoatEntityLeftPaddleState;
    private boolean BoatEntityRightPaddleState;

    private final HashMap<EquipmentSlot, ItemStack> currentItemInEquipmentSlot;
    private List<ModsListenerImpl> modsListenerList;

    public ActionDifferenceListener(ActionsData actionsData, Recording recording) {
        this.actionsData = actionsData;
        this.currentItemInEquipmentSlot = new HashMap<>();
        this.recording = recording;
        BoatEntityLeftPaddleState = false;
        BoatEntityRightPaddleState = false;
        initItemSlot();
        initModsListeners();
    }

    private void initItemSlot() {
        for (EquipmentSlot equipmentSlot : equipmentSlotList) {
            currentItemInEquipmentSlot.put(equipmentSlot, new ItemStack(Items.AIR));
        }
    }

    private void initModsListeners() {
        modsListenerList = new ArrayList<>();
        if (Services.PLATFORM.isModLoaded("emotecraft")) {
            EmoteCraftListeners emoteCraftListeners = new EmoteCraftListeners(this);
            emoteCraftListeners.start();
            modsListenerList.add(emoteCraftListeners);
        }
    }

    public void listenDifference() {

        if (actionsData.getEntity() instanceof LivingEntity) {
            poseListener();
            entityByteListener();
            livingEntityByteListener();
            itemListener();
        }
    }

    private void poseListener() {
        if (actionsData.getEntity().getPose() != poseState) {
            PoseAction action =
                    new PoseAction(recording.getTick(), actionsData.getEntity().getPose(), poseState);
            poseState = actionsData.getEntity().getPose();
            actionsData.addAction(action);
        }
    }

    private void entityByteListener() {
        byte entityCurrentByte = actionsData.getEntity().getEntityData().get(EntityAccessor.getDATA_SHARED_FLAGS_ID());
        if (entityByteState != entityCurrentByte) {
            EntityByteAction entityByteAction =
                    new EntityByteAction(recording.getTick(), entityCurrentByte, entityByteState);
            entityByteState = entityCurrentByte;
            actionsData.addAction(entityByteAction);
        }
    }

    private void livingEntityByteListener() {
        byte livingEntityCurrentByte =
                actionsData.getEntity().getEntityData().get(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS());
        if (livingEntityByteState != livingEntityCurrentByte) {
            LivingEntityByteAction livingEntityByteAction =
                    new LivingEntityByteAction(recording.getTick(), livingEntityCurrentByte, livingEntityByteState);
            livingEntityByteState = livingEntityCurrentByte;
            actionsData.addAction(livingEntityByteAction);
        }
    }

    public void abstractHorseEntityByteListener(byte abstractHorseCurrentByte) {
        if (actionsData.getEntity() instanceof AbstractHorse) {
            if (abstractHorseEntityByteState != abstractHorseCurrentByte) {
                AbstractHorseByteAction action = new AbstractHorseByteAction(
                        recording.getTick(), abstractHorseCurrentByte, abstractHorseEntityByteState);
                abstractHorseEntityByteState = abstractHorseCurrentByte;
                actionsData.addAction(action);
            }
        }
    }

    public void BoatEntityBubbleListener(int BoatCurrentBubble) {
        if (actionsData.getEntity() instanceof Boat) {
            if (BoatEntityBubbleState != BoatCurrentBubble) {
                BoatBubbleAction action =
                        new BoatBubbleAction(recording.getTick(), BoatCurrentBubble, BoatEntityBubbleState);
                BoatEntityBubbleState = BoatCurrentBubble;
                actionsData.addAction(action);
            }
        }
    }

    public void BoatEntityPaddleListener(boolean left, boolean right) {
        if (actionsData.getEntity() instanceof Boat) {
            if (BoatEntityLeftPaddleState != left || BoatEntityRightPaddleState != right) {
                BoatPaddleAction action = new BoatPaddleAction(
                        recording.getTick(), left, right, BoatEntityLeftPaddleState, BoatEntityRightPaddleState);
                BoatEntityLeftPaddleState = left;
                BoatEntityRightPaddleState = right;
                actionsData.addAction(action);
            }
        }
    }

    private void itemListener() {

        for (EquipmentSlot equipmentSlot : equipmentSlotList) {
            ItemStack itemFromSlot = currentItemInEquipmentSlot.get(equipmentSlot);
            ItemStack currentItemFromSlot = ((LivingEntity) actionsData.getEntity()).getItemBySlot(equipmentSlot);
            if (BuiltInRegistries.ITEM.getId(itemFromSlot.getItem())
                    != BuiltInRegistries.ITEM.getId(currentItemFromSlot.getItem())) {
                currentItemInEquipmentSlot.replace(equipmentSlot, currentItemFromSlot.copy());
                onItemChange(currentItemFromSlot, itemFromSlot, equipmentSlot, recording.getTick());
            }
        }
    }

    private void onItemChange(ItemStack itemStack, ItemStack oldItemStack, EquipmentSlot equipmentSlot, int tick) {
        ItemChangeAction itemChangeAction = new ItemChangeAction(
                tick,
                equipmentSlot.name(),
                itemStack,
                oldItemStack);
        actionsData.addAction(itemChangeAction);
    }

    public Recording getRecording() {
        return recording;
    }

    public List<ModsListenerImpl> getModsListenerList() {
        return modsListenerList;
    }

    public ActionsData getActionsData() {
        return actionsData;
    }
}
