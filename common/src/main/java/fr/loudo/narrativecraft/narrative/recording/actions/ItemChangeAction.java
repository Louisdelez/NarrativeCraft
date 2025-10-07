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

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import fr.loudo.narrativecraft.util.Util;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemChangeAction extends Action {

    private final int itemId;
    private final int oldItemId;
    private final String data;
    private final String oldData;
    private final String equipmentSlot;

    public ItemChangeAction(
            int waitTick,
            String equipmentSlot,
            ItemStack itemStack,
            ItemStack oldItemStack,
            RegistryAccess registryAccess) {
        super(waitTick, ActionType.ITEM_CHANGE);
        this.itemId = BuiltInRegistries.ITEM.getId(itemStack.getItem());
        this.oldItemId = BuiltInRegistries.ITEM.getId(oldItemStack.getItem());
        this.equipmentSlot = equipmentSlot;
        this.data = getItemComponents(itemStack, registryAccess);
        this.oldData = getItemComponents(oldItemStack, registryAccess);
    }

    private String getItemComponents(ItemStack itemStack, RegistryAccess registryAccess) {
        if (itemStack.isEmpty()) return null;
        Tag tag = Util.getItemTag(itemStack, registryAccess);
        Tag componentsTag = ((CompoundTag) tag).get("components");
        if (componentsTag != null) {
            return componentsTag.toString();
        }
        return null;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        if (playbackData.getEntity() instanceof LivingEntity livingEntity) {
            changeItem(livingEntity, itemId, data);
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {
        if (playbackData.getEntity() instanceof LivingEntity livingEntity) {
            changeItem(livingEntity, oldItemId, oldData);
        }
    }

    private void changeItem(LivingEntity entity, int itemId, String data) {
        Item item = BuiltInRegistries.ITEM.byId(itemId);
        ItemStack itemStack = new ItemStack(item);
        if (data != null) {
            CompoundTag tag = Util.tagFromIdAndComponents(item, data);
            if (tag != null) {
                itemStack = Util.generateItemStackFromNBT(tag, entity.registryAccess());
            }
        }
        entity.level()
                .getServer()
                .getPlayerList()
                .broadcastAll(new ClientboundSetEquipmentPacket(
                        entity.getId(), List.of(new Pair<>(EquipmentSlot.valueOf(equipmentSlot), itemStack))));
        entity.setItemSlot(EquipmentSlot.valueOf(equipmentSlot), itemStack);
    }
}
