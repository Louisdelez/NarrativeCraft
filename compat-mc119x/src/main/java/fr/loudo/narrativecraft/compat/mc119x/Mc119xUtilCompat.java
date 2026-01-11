/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc119x;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.compat.api.IUtilCompat;
import fr.loudo.narrativecraft.compat.api.NarrativeSoundInstance;
import fr.loudo.narrativecraft.compat.api.NcId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Style;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * MC 1.19.x implementation of utility compatibility methods.
 * Uses ResourceLocation and direct CompoundTag instead of TagValueInput.
 * Note: Some APIs differ significantly from 1.20.x.
 */
public class Mc119xUtilCompat implements IUtilCompat {

    @Override
    public Object createValueInputFromNbt(Object entity, String nbtString) {
        // In 1.19.x, we return the CompoundTag directly
        try {
            return TagParser.parseTag(nbtString);
        } catch (CommandSyntaxException ex) {
            throw new RuntimeException("Failed to parse NBT string", ex);
        }
    }

    @Override
    public Object getItemTag(Object itemStack, Object registryAccess) {
        ItemStack stack = (ItemStack) itemStack;
        // 1.19.x uses simpler save() method
        return stack.save(new CompoundTag());
    }

    @Override
    public Object tagFromIdAndComponents(Object item, String componentsData) {
        Item i = (Item) item;
        CompoundTag tag = new CompoundTag();
        try {
            // 1.19.x doesn't have components, but we can still store extra data in "tag"
            tag.put("tag", TagParser.parseTag(componentsData));
        } catch (CommandSyntaxException e) {
            return null;
        }
        tag.putString("id", BuiltInRegistries.ITEM.getKey(i).toString());
        tag.putInt("Count", 1);
        return tag;
    }

    @Override
    public Object generateItemStackFromNBT(Object compoundTag, Object registryAccess) {
        CompoundTag tag = (CompoundTag) compoundTag;
        if (tag == null) {
            return ItemStack.EMPTY;
        }
        try {
            // 1.19.x uses ItemStack.of(CompoundTag)
            return ItemStack.of(tag);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public Object getBlockStateFromData(String data, Object registryAccess) {
        try {
            CompoundTag compoundTag = TagParser.parseTag(data);
            // 1.19.x uses Registry instead of RegistryAccess lookup
            return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), compoundTag);
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    @Override
    public Object createEntityFromCharacter(Object characterStory, Object level) {
        throw new UnsupportedOperationException(
                "createEntityFromCharacter requires CharacterStory access - use Util class directly");
    }

    @Override
    public void spawnEntity(Object entity, Object level) {
        throw new UnsupportedOperationException(
                "spawnEntity requires FakePlayer access - use Util class directly");
    }

    @Override
    public void addFakePlayerUUID(Object fakePlayer, Object server) {
        throw new UnsupportedOperationException(
                "addFakePlayerUUID requires FakePlayer access - use Util class directly");
    }

    @Override
    public void removeFakePlayerUUID(Object fakePlayer, Object server) {
        throw new UnsupportedOperationException(
                "removeFakePlayerUUID requires FakePlayer access - use Util class directly");
    }

    @Override
    public int[] getImageResolution(Object resourceLocation) {
        ResourceLocation rl = toResourceLocation(resourceLocation);
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Optional<Resource> resource = resourceManager.getResource(rl);
        if (resource.isPresent()) {
            try (InputStream stream = resource.get().open()) {
                BufferedImage image = ImageIO.read(stream);
                return new int[]{image.getWidth(), image.getHeight()};
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    @Override
    public boolean resourceExists(Object resourceLocation) {
        ResourceLocation rl = toResourceLocation(resourceLocation);
        return Minecraft.getInstance().getResourceManager().getResource(rl).isPresent();
    }

    private ResourceLocation toResourceLocation(Object resourceLocation) {
        if (resourceLocation instanceof ResourceLocation rl) {
            return rl;
        } else if (resourceLocation instanceof NcId ncId) {
            return new ResourceLocation(ncId.namespace(), ncId.path());
        }
        throw new IllegalArgumentException("Expected ResourceLocation or NcId, got: " + resourceLocation.getClass().getName());
    }

    @Override
    public float getLetterWidth(int letterCode, Object minecraft) {
        Minecraft mc = (Minecraft) minecraft;
        Font font = mc.font;
        return font.width(String.valueOf(Character.toChars(letterCode)));
    }

    @Override
    public void disconnectPlayer(Object minecraft) {
        Minecraft mc = (Minecraft) minecraft;
        // 1.19.x uses clearLevel() instead of disconnect()
        mc.clearLevel();
    }

    @Override
    public Object createEntityFromType(Object entityType, Object level) {
        EntityType<?> type = (EntityType<?>) entityType;
        Level lvl = (Level) level;
        return type.create(lvl);
    }

    @Override
    public void loadEntityFromNbt(Object entity, String nbtString) {
        Entity e = (Entity) entity;
        try {
            CompoundTag tag = TagParser.parseTag(nbtString);
            e.load(tag);
        } catch (CommandSyntaxException ex) {
            throw new RuntimeException("Failed to parse NBT for entity load", ex);
        }
    }

    @Override
    public String serializeEntityToNbt(Object entity) {
        Entity e = (Entity) entity;
        CompoundTag compoundTag = new CompoundTag();
        e.saveWithoutId(compoundTag);
        compoundTag.remove("UUID");
        compoundTag.remove("Pos");
        compoundTag.remove("Motion");
        return compoundTag.toString();
    }

    @Override
    public void registerTexture(NcId ncId, Object texture) {
        ResourceLocation id = new ResourceLocation(ncId.namespace(), ncId.path());
        Minecraft.getInstance().getTextureManager().register(id, (net.minecraft.client.renderer.texture.DynamicTexture) texture);
    }

    @Override
    public void releaseTexture(NcId ncId) {
        ResourceLocation id = new ResourceLocation(ncId.namespace(), ncId.path());
        Minecraft.getInstance().getTextureManager().release(id);
    }

    @Override
    public String getKeyMappingCategory(NcId ncId) {
        return "key.categories." + ncId.namespace();
    }

    @Override
    public Object createSoundEvent(NcId ncId) {
        ResourceLocation id = new ResourceLocation(ncId.namespace(), ncId.path());
        return SoundEvent.createVariableRangeEvent(id);
    }

    @Override
    public Object getSoundEvent(Object soundManager, NcId ncId) {
        SoundManager sm = (SoundManager) soundManager;
        ResourceLocation id = new ResourceLocation(ncId.namespace(), ncId.path());
        return sm.getSoundEvent(id);
    }

    @Override
    public Object createSimpleSoundInstance(NcId ncId, float volume, float pitch) {
        ResourceLocation id = new ResourceLocation(ncId.namespace(), ncId.path());
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(id);
        return SimpleSoundInstance.forUI(soundEvent, volume, pitch);
    }

    @Override
    public Object createSoundInstance(NcId ncId, Object source, float volume, float pitch,
            Object random, boolean looping, int delay, int attenuation,
            double x, double y, double z, boolean relative) {
        ResourceLocation id = new ResourceLocation(ncId.namespace(), ncId.path());
        SoundSource soundSource = (SoundSource) source;
        RandomSource randomSource = (RandomSource) random;
        AbstractSoundInstance.Attenuation att = attenuation == 0 ?
                AbstractSoundInstance.Attenuation.NONE : AbstractSoundInstance.Attenuation.LINEAR;

        return new NarrativeSoundInstanceImpl(id, soundSource, volume, pitch, randomSource, looping, delay, att, x, y, z, relative);
    }

    @Override
    public Object withFont(Object baseStyle, NcId fontId) {
        Style style = (Style) baseStyle;
        ResourceLocation id = new ResourceLocation(fontId.namespace(), fontId.path());
        return style.withFont(id);
    }

    @Override
    public void openPath(Object path) {
        // 1.19.x uses openFile(File)
        if (path instanceof java.nio.file.Path p) {
            net.minecraft.Util.getPlatform().openFile(p.toFile());
        } else if (path instanceof java.io.File f) {
            net.minecraft.Util.getPlatform().openFile(f);
        } else {
            throw new IllegalArgumentException("Expected Path or File, got: " + path.getClass().getName());
        }
    }

    @Override
    public Object getPlayerGameMode(Object player) {
        net.minecraft.server.level.ServerPlayer serverPlayer = (net.minecraft.server.level.ServerPlayer) player;
        return serverPlayer.gameMode.getGameModeForPlayer();
    }

    @Override
    public boolean isPlayerLookingAtEntity(Object entity, Object player, double dotThreshold, double targetY) {
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity livingEntity)) {
            return false;
        }
        if (!(player instanceof net.minecraft.world.entity.player.Player p)) {
            return false;
        }

        net.minecraft.world.phys.Vec3 playerEyePos = p.getEyePosition();
        net.minecraft.world.phys.Vec3 targetPos = new net.minecraft.world.phys.Vec3(
                livingEntity.getX(), targetY, livingEntity.getZ());
        net.minecraft.world.phys.Vec3 toEntity = targetPos.subtract(playerEyePos).normalize();
        net.minecraft.world.phys.Vec3 playerLook = p.getLookAngle();
        double dot = playerLook.dot(toEntity);

        return dot > dotThreshold;
    }

    @Override
    public boolean isBoatType(Object entity) {
        return entity instanceof net.minecraft.world.entity.vehicle.Boat;
    }

    @Override
    public boolean hasSaddleSlot() {
        return false;
    }

    @Override
    public boolean hasBodySlot() {
        return false;
    }

    /**
     * Internal implementation of NarrativeSoundInstance for MC 1.19.x.
     */
    private static class NarrativeSoundInstanceImpl extends AbstractSoundInstance implements NarrativeSoundInstance {
        NarrativeSoundInstanceImpl(ResourceLocation id, SoundSource source, float volume, float pitch,
                RandomSource random, boolean looping, int delay, Attenuation attenuation,
                double x, double y, double z, boolean relative) {
            super(id, source, random);
            this.volume = volume;
            this.pitch = pitch;
            this.x = x;
            this.y = y;
            this.z = z;
            this.looping = looping;
            this.delay = delay;
            this.attenuation = attenuation;
            this.relative = relative;
        }
    }
}
