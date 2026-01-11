/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc121x;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DynamicOps;
import fr.loudo.narrativecraft.compat.api.IUtilCompat;
import fr.loudo.narrativecraft.compat.api.NarrativeSoundInstance;
import fr.loudo.narrativecraft.compat.api.NcId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * MC 1.21.x implementation of utility compatibility methods.
 */
public class Mc121xUtilCompat implements IUtilCompat {

    @Override
    public Object createValueInputFromNbt(Object entity, String nbtString) {
        try {
            Entity e = (Entity) entity;
            CompoundTag tag = TagParser.parseCompoundAsArgument(new StringReader(nbtString));
            return TagValueInput.create(ProblemReporter.DISCARDING, e.registryAccess(), tag);
        } catch (CommandSyntaxException ex) {
            throw new RuntimeException("Failed to parse NBT string", ex);
        }
    }

    @Override
    public Object getItemTag(Object itemStack, Object registryAccess) {
        ItemStack stack = (ItemStack) itemStack;
        RegistryAccess registry = (RegistryAccess) registryAccess;
        DynamicOps<Tag> ops = registry.createSerializationContext(NbtOps.INSTANCE);
        try {
            return ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
        } catch (Exception e) {
            return new CompoundTag();
        }
    }

    @Override
    public Object tagFromIdAndComponents(Object item, String componentsData) {
        Item i = (Item) item;
        CompoundTag tag = new CompoundTag();
        try {
            tag.put("components", TagParser.parseCompoundAsArgument(new StringReader(componentsData)));
        } catch (CommandSyntaxException e) {
            return null;
        }
        tag.put("id", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(i).toString()));
        tag.put("count", IntTag.valueOf(1));
        return tag;
    }

    @Override
    public Object generateItemStackFromNBT(Object compoundTag, Object registryAccess) {
        CompoundTag tag = (CompoundTag) compoundTag;
        RegistryAccess registry = (RegistryAccess) registryAccess;
        DynamicOps<Tag> ops = registry.createSerializationContext(NbtOps.INSTANCE);
        if (tag == null) {
            return ItemStack.EMPTY;
        }
        try {
            return ItemStack.CODEC.parse(ops, tag).getOrThrow();
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public Object getBlockStateFromData(String data, Object registryAccess) {
        try {
            RegistryAccess registry = (RegistryAccess) registryAccess;
            CompoundTag compoundTag = TagParser.parseCompoundAsArgument(new StringReader(data));
            return NbtUtils.readBlockState(registry.lookupOrThrow(Registries.BLOCK), compoundTag);
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    @Override
    public Object createEntityFromCharacter(Object characterStory, Object level) {
        // This method requires access to CharacterStory, FakePlayer, and AvatarAccessor
        // which are in common/common-mc121, not in compat modules.
        // This will be handled by a callback or the Util class in common-mc121.
        throw new UnsupportedOperationException(
                "createEntityFromCharacter requires CharacterStory access - use Util class directly");
    }

    @Override
    public void spawnEntity(Object entity, Object level) {
        // Similarly requires FakePlayer and PlayerListAccessor from common-mc121
        throw new UnsupportedOperationException(
                "spawnEntity requires FakePlayer access - use Util class directly");
    }

    @Override
    public void addFakePlayerUUID(Object fakePlayer, Object server) {
        // Requires FakePlayer and PlayerListAccessor from common-mc121
        throw new UnsupportedOperationException(
                "addFakePlayerUUID requires FakePlayer access - use Util class directly");
    }

    @Override
    public void removeFakePlayerUUID(Object fakePlayer, Object server) {
        // Requires FakePlayer and PlayerListAccessor from common-mc121
        throw new UnsupportedOperationException(
                "removeFakePlayerUUID requires FakePlayer access - use Util class directly");
    }

    @Override
    public int[] getImageResolution(Object resourceLocation) {
        Identifier rl = toIdentifier(resourceLocation);
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
        Identifier rl = toIdentifier(resourceLocation);
        return Minecraft.getInstance().getResourceManager().getResource(rl).isPresent();
    }

    private Identifier toIdentifier(Object resourceLocation) {
        if (resourceLocation instanceof Identifier id) {
            return id;
        } else if (resourceLocation instanceof NcId ncId) {
            return Identifier.fromNamespaceAndPath(ncId.namespace(), ncId.path());
        }
        throw new IllegalArgumentException("Expected Identifier or NcId, got: " + resourceLocation.getClass().getName());
    }

    @Override
    public float getLetterWidth(int letterCode, Object minecraft) {
        Minecraft mc = (Minecraft) minecraft;
        Font font = mc.font;
        StringSplitter splitter = font.getSplitter();
        // Access via reflection or mixin - for now use a simpler approach
        // The StringSplitterAccessor mixin is in common-mc121, not compat
        // Use font.width as approximation
        return font.width(String.valueOf(Character.toChars(letterCode)));
    }

    @Override
    public void disconnectPlayer(Object minecraft) {
        Minecraft mc = (Minecraft) minecraft;
        mc.disconnectFromWorld(Component.empty());
    }

    @Override
    public Object createEntityFromType(Object entityType, Object level) {
        EntityType<?> type = (EntityType<?>) entityType;
        Level lvl = (Level) level;
        return type.create(lvl, EntitySpawnReason.MOB_SUMMONED);
    }

    @Override
    public void loadEntityFromNbt(Object entity, String nbtString) {
        Entity e = (Entity) entity;
        try {
            CompoundTag tag = TagParser.parseCompoundAsArgument(new StringReader(nbtString));
            ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, e.registryAccess(), tag);
            e.load(input);
        } catch (CommandSyntaxException ex) {
            throw new RuntimeException("Failed to parse NBT for entity load", ex);
        }
    }

    @Override
    public String serializeEntityToNbt(Object entity) {
        Entity e = (Entity) entity;
        net.minecraft.world.level.storage.TagValueOutput nbt =
                net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                        ProblemReporter.DISCARDING, e.registryAccess());
        e.saveWithoutId(nbt);
        CompoundTag compoundTag = nbt.buildResult();
        compoundTag.remove("UUID");
        compoundTag.remove("Pos");
        compoundTag.remove("Motion");
        return compoundTag.toString();
    }

    @Override
    public void registerTexture(NcId ncId, Object texture) {
        Identifier id = Identifier.fromNamespaceAndPath(ncId.namespace(), ncId.path());
        Minecraft.getInstance().getTextureManager().register(id, (net.minecraft.client.renderer.texture.DynamicTexture) texture);
    }

    @Override
    public void releaseTexture(NcId ncId) {
        Identifier id = Identifier.fromNamespaceAndPath(ncId.namespace(), ncId.path());
        Minecraft.getInstance().getTextureManager().release(id);
    }

    @Override
    public String getKeyMappingCategory(NcId ncId) {
        // In 1.21.x, KeyMapping.Category constructor takes Identifier
        // but we still return a string since KeyMapping stores it as String internally
        return "key.categories." + ncId.namespace();
    }

    @Override
    public Object createSoundEvent(NcId ncId) {
        Identifier id = Identifier.fromNamespaceAndPath(ncId.namespace(), ncId.path());
        return SoundEvent.createVariableRangeEvent(id);
    }

    @Override
    public Object getSoundEvent(Object soundManager, NcId ncId) {
        SoundManager sm = (SoundManager) soundManager;
        Identifier id = Identifier.fromNamespaceAndPath(ncId.namespace(), ncId.path());
        return sm.getSoundEvent(id);
    }

    @Override
    public Object createSimpleSoundInstance(NcId ncId, float volume, float pitch) {
        Identifier id = Identifier.fromNamespaceAndPath(ncId.namespace(), ncId.path());
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(id);
        return SimpleSoundInstance.forUI(soundEvent, volume, pitch);
    }

    @Override
    public Object createSoundInstance(NcId ncId, Object source, float volume, float pitch,
            Object random, boolean looping, int delay, int attenuation,
            double x, double y, double z, boolean relative) {
        Identifier id = Identifier.fromNamespaceAndPath(ncId.namespace(), ncId.path());
        SoundSource soundSource = (SoundSource) source;
        RandomSource randomSource = (RandomSource) random;
        AbstractSoundInstance.Attenuation att = attenuation == 0 ?
                AbstractSoundInstance.Attenuation.NONE : AbstractSoundInstance.Attenuation.LINEAR;

        return new NarrativeSoundInstanceImpl(id, soundSource, volume, pitch, randomSource, looping, delay, att, x, y, z, relative);
    }

    @Override
    public Object withFont(Object baseStyle, NcId fontId) {
        Style style = (Style) baseStyle;
        Identifier id = Identifier.fromNamespaceAndPath(fontId.namespace(), fontId.path());
        return style.withFont(new FontDescription.Resource(id));
    }

    @Override
    public void openPath(Object path) {
        // 1.21.x uses openPath(Path)
        if (path instanceof java.nio.file.Path p) {
            net.minecraft.util.Util.getPlatform().openPath(p);
        } else if (path instanceof java.io.File f) {
            net.minecraft.util.Util.getPlatform().openPath(f.toPath());
        } else {
            throw new IllegalArgumentException("Expected Path or File, got: " + path.getClass().getName());
        }
    }

    @Override
    public Object getPlayerGameMode(Object player) {
        // 1.21.x uses player.gameMode()
        ServerPlayer serverPlayer = (ServerPlayer) player;
        return serverPlayer.gameMode();
    }

    @Override
    public boolean isPlayerLookingAtEntity(Object entity, Object player, double dotThreshold, double targetY) {
        // 1.21.x: Use LivingEntity.isLookingAtMe()
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity livingEntity)) {
            return false;
        }
        if (!(player instanceof net.minecraft.world.entity.player.Player p)) {
            return false;
        }
        // isLookingAtMe(player, threshold, checkSpectate, checkInvisible, targetY...)
        return livingEntity.isLookingAtMe(p, dotThreshold, true, false, targetY);
    }

    @Override
    public boolean isBoatType(Object entity) {
        // 1.21.x uses AbstractBoat class
        return entity instanceof net.minecraft.world.entity.vehicle.boat.AbstractBoat;
    }

    @Override
    public boolean hasSaddleSlot() {
        // 1.21.x: EquipmentSlot.SADDLE exists
        return true;
    }

    @Override
    public boolean hasBodySlot() {
        // 1.21.x: EquipmentSlot.BODY exists
        return true;
    }

    /**
     * Internal implementation of NarrativeSoundInstance for MC 1.21.x.
     */
    private static class NarrativeSoundInstanceImpl extends AbstractSoundInstance implements NarrativeSoundInstance {
        NarrativeSoundInstanceImpl(Identifier id, SoundSource source, float volume, float pitch,
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
