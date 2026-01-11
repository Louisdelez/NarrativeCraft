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
// 1.20.x package path: animal.horse instead of animal.equine
import net.minecraft.world.entity.animal.horse.AbstractHorse;
// 1.20.x: No AbstractBoat class, use Boat directly
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * MC 1.20.x version of ActionDifferenceListener.
 * Key differences from 1.21.x:
 * - AbstractHorse: net.minecraft.world.entity.animal.horse instead of animal.equine
 * - No AbstractBoat class: use Boat directly
 * - EquipmentSlot.SADDLE doesn't exist: removed from equipment slot list
 * - EquipmentSlot.BODY doesn't exist: removed from equipment slot list
 */
public class ActionDifferenceListener {

    // 1.20.x: No SADDLE or BODY equipment slots
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

    private int abstractBoatEntityBubbleState;
    private boolean abstractBoatEntityLeftPaddleState;
    private boolean abstractBoatEntityRightPaddleState;

    private final HashMap<EquipmentSlot, ItemStack> currentItemInEquipmentSlot;
    private List<ModsListenerImpl> modsListenerList;

    public ActionDifferenceListener(ActionsData actionsData, Recording recording) {
        this.actionsData = actionsData;
        this.currentItemInEquipmentSlot = new HashMap<>();
        this.recording = recording;
        abstractBoatEntityLeftPaddleState = false;
        abstractBoatEntityRightPaddleState = false;
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

    public void abstractBoatEntityBubbleListener(int abstractBoatCurrentBubble) {
        // 1.20.x: Use Boat instead of AbstractBoat
        if (actionsData.getEntity() instanceof Boat) {
            if (abstractBoatEntityBubbleState != abstractBoatCurrentBubble) {
                AbstractBoatBubbleAction action = new AbstractBoatBubbleAction(
                        recording.getTick(), abstractBoatCurrentBubble, abstractBoatEntityBubbleState);
                abstractBoatEntityBubbleState = abstractBoatCurrentBubble;
                actionsData.addAction(action);
            }
        }
    }

    public void abstractBoatEntityPaddleListener(boolean left, boolean right) {
        // 1.20.x: Use Boat instead of AbstractBoat
        if (actionsData.getEntity() instanceof Boat) {
            if (abstractBoatEntityLeftPaddleState != left || abstractBoatEntityRightPaddleState != right) {
                AbstractBoatPaddleAction action = new AbstractBoatPaddleAction(
                        recording.getTick(),
                        left,
                        right,
                        abstractBoatEntityLeftPaddleState,
                        abstractBoatEntityRightPaddleState);
                abstractBoatEntityLeftPaddleState = left;
                abstractBoatEntityRightPaddleState = right;
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
                oldItemStack,
                // 1.19.x: Use getLevel().registryAccess() instead of entity.registryAccess()
                actionsData.getEntity().getLevel().registryAccess());
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
