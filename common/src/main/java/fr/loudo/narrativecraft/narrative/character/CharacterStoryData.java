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

package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.util.Util;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class CharacterStoryData {

    private transient CharacterRuntime characterRuntime;
    private final Location location;
    private final List<ItemSlotData> itemSlotDataList = new ArrayList<>();

    private String poseName = Pose.STANDING.name();
    private byte entityByte;
    private byte livingEntityByte;
    private String skinName = "main.png";
    private transient String oldSkinName = "main.png";
    private boolean isTemplate;

    public CharacterStoryData(CharacterStory characterStory, Location location, boolean isTemplate, Scene scene) {
        this.characterRuntime = new CharacterRuntime(characterStory, skinName, null, scene);
        this.location = location;
        this.isTemplate = isTemplate;
    }

    public void spawn(Level level, Environment environment) {
        if (isTemplate && environment == Environment.PRODUCTION && characterRuntime.getCharacterStory() == null) return;
        characterRuntime.getCharacterSkinController().setSkinName(skinName);
        characterRuntime.getCharacterSkinController().cacheSkins();
        characterRuntime.setEntity(Util.createEntityFromCharacter(characterRuntime.getCharacterStory(), level));
        characterRuntime.getEntity().teleportTo(location.x(), location.y(), location.z());
        characterRuntime.getEntity().setXRot(location.pitch());
        characterRuntime.getEntity().setYRot(location.yaw());
        characterRuntime.getEntity().setYHeadRot(location.yaw());
        characterRuntime.getEntity().setOnGround(location.onGround());
        try {
            characterRuntime.getEntity().setPose(Pose.valueOf(poseName));
        } catch (Exception ignored) {
        }

        applyBytes(characterRuntime.getEntity());
        applyItems();
    }

    public void applyBytes(LivingEntity entity) {
        entity.getEntityData().set(EntityAccessor.getDATA_SHARED_FLAGS_ID(), entityByte);
        entity.getEntityData().set(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS(), livingEntityByte);
    }

    public void applyItems() {
        for (ItemSlotData itemSlotData : itemSlotDataList) {
            try {
                characterRuntime
                        .getEntity()
                        .setItemSlot(
                                EquipmentSlot.valueOf(itemSlotData.equipmentSlot),
                                itemSlotData.getItem());
            } catch (Exception ignored) {
            }
        }
    }

    public void kill() {
        if (characterRuntime.getEntity() == null) return;
        characterRuntime.getEntity().remove(Entity.RemovalReason.KILLED);
    }

    public void setItems(LivingEntity entity) {
        itemSlotDataList.clear();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = entity.getItemBySlot(equipmentSlot);
            if (!itemStack.isEmpty()) {
                Tag componentsTag = itemStack.getTag();
                String itemData = componentsTag == null ? "" : componentsTag.toString();
                itemSlotDataList.add(new ItemSlotData(
                        BuiltInRegistries.ITEM.getId(itemStack.getItem()), itemData, equipmentSlot.name()));
            } else {
                itemSlotDataList.add(
                        new ItemSlotData(BuiltInRegistries.ITEM.getId(Items.AIR), "", equipmentSlot.name()));
            }
        }
    }

    public byte getLivingEntityByte() {
        return livingEntityByte;
    }

    public void setLivingEntityByte(byte livingEntityByte) {
        this.livingEntityByte = livingEntityByte;
    }

    public byte getEntityByte() {
        return entityByte;
    }

    public void setEntityByte(byte entityByte) {
        this.entityByte = entityByte;
    }

    public Location getLocation() {
        return location;
    }

    public Pose getPose() {
        try {
            return Pose.valueOf(poseName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setPose(Pose pose) {
        this.poseName = pose.name();
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        oldSkinName = this.skinName;
        this.skinName = skinName;
    }

    public String getOldSkinName() {
        if (oldSkinName == null) oldSkinName = "main.png";
        return oldSkinName;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setTemplate(boolean template) {
        isTemplate = template;
    }

    public CharacterRuntime getCharacterRuntime() {
        return characterRuntime;
    }

    public void setCharacterRuntime(CharacterRuntime characterRuntime) {
        this.characterRuntime = characterRuntime;
    }

    public CharacterStory getCharacterStory() {
        return characterRuntime.getCharacterStory();
    }

    private record ItemSlotData(int id, String data, String equipmentSlot) {
        public ItemStack getItem() {
            Item item = BuiltInRegistries.ITEM.byId(id);
            ItemStack itemStack = new ItemStack(item);
            CompoundTag tag = Util.tagFromIdAndComponents(item, data);
            itemStack.setTag(tag);
            return itemStack;
        }
    }
}
