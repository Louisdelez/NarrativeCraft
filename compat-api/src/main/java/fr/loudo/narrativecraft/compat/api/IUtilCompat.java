/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Utility compatibility interface for abstracting version-specific utility methods.
 * Contains methods that have different implementations between MC versions due to:
 * - NBT/TagValue API changes (TagValueInput in 1.21.x vs CompoundTag in 1.20.x)
 * - Entity spawn reason differences
 * - Identifier vs ResourceLocation
 * - Mixin accessor differences
 *
 * Pure Java interface - no Minecraft dependencies.
 */
public interface IUtilCompat {

    /**
     * Create a ValueInput from NBT string for entity loading.
     * MC 1.21.x uses TagValueInput, MC 1.20.x uses direct CompoundTag parsing.
     *
     * @param entity The entity (used for registry access)
     * @param nbtString The NBT string to parse
     * @return Object that can be passed to entity.load() - version specific type
     */
    Object createValueInputFromNbt(Object entity, String nbtString);

    /**
     * Get item tag from ItemStack for serialization.
     *
     * @param itemStack The ItemStack
     * @param registryAccess The registry access
     * @return The NBT Tag representation
     */
    Object getItemTag(Object itemStack, Object registryAccess);

    /**
     * Create CompoundTag from item ID and components string.
     *
     * @param item The Item
     * @param componentsData The components NBT string
     * @return CompoundTag or null if parsing fails
     */
    Object tagFromIdAndComponents(Object item, String componentsData);

    /**
     * Generate ItemStack from NBT CompoundTag.
     *
     * @param compoundTag The CompoundTag (or null for empty)
     * @param registryAccess The registry access
     * @return The ItemStack
     */
    Object generateItemStackFromNBT(Object compoundTag, Object registryAccess);

    /**
     * Parse BlockState from NBT data string.
     *
     * @param data The NBT string representation
     * @param registryAccess The registry access
     * @return The BlockState or null if parsing fails
     */
    Object getBlockStateFromData(String data, Object registryAccess);

    /**
     * Create a living entity from a character story definition.
     * Handles EntitySpawnReason differences between versions.
     *
     * @param characterStory The character story object
     * @param level The level/world
     * @return The created LivingEntity
     */
    Object createEntityFromCharacter(Object characterStory, Object level);

    /**
     * Spawn an entity in the world, handling FakePlayer specially.
     *
     * @param entity The entity to spawn
     * @param level The level/world
     */
    void spawnEntity(Object entity, Object level);

    /**
     * Add a FakePlayer's UUID to the server's player list.
     *
     * @param fakePlayer The FakePlayer
     * @param server The MinecraftServer (can be null, will no-op)
     */
    void addFakePlayerUUID(Object fakePlayer, Object server);

    /**
     * Remove a FakePlayer's UUID from the server's player list.
     *
     * @param fakePlayer The FakePlayer
     * @param server The MinecraftServer (can be null, will no-op)
     */
    void removeFakePlayerUUID(Object fakePlayer, Object server);

    /**
     * Get image resolution from a resource location.
     *
     * @param resourceLocation The resource location (Identifier or ResourceLocation)
     * @return int array [width, height] or null if not found
     */
    int[] getImageResolution(Object resourceLocation);

    /**
     * Check if a resource exists at the given location.
     *
     * @param resourceLocation The resource location
     * @return true if the resource exists
     */
    boolean resourceExists(Object resourceLocation);

    /**
     * Get the width of a letter/character for text rendering.
     *
     * @param letterCode The Unicode code point
     * @param minecraft The Minecraft instance
     * @return The width in pixels
     */
    float getLetterWidth(int letterCode, Object minecraft);

    /**
     * Disconnect the player from the current world.
     *
     * @param minecraft The Minecraft client instance
     */
    void disconnectPlayer(Object minecraft);

    /**
     * Create an entity from an EntityType.
     * Handles EntitySpawnReason differences between MC versions.
     * In 1.21.x uses EntitySpawnReason.MOB_SUMMONED, in 1.20.x uses MobSpawnType or null.
     *
     * @param entityType The EntityType to create
     * @param level The level/world
     * @return The created Entity, or null if creation failed
     */
    Object createEntityFromType(Object entityType, Object level);

    /**
     * Load NBT data into an entity.
     * Handles the different load() method signatures between MC versions.
     * In 1.21.x uses ValueInput, in 1.20.x uses CompoundTag directly.
     *
     * @param entity The entity to load data into
     * @param nbtString The NBT string to parse and load
     */
    void loadEntityFromNbt(Object entity, String nbtString);

    /**
     * Serialize an entity to NBT string for storage.
     * Handles the different saveWithoutId() method signatures between MC versions.
     * In 1.21.x uses TagValueOutput, in 1.20.x uses CompoundTag directly.
     *
     * @param entity The entity to serialize
     * @return The NBT string representation (without UUID, Pos, Motion)
     */
    String serializeEntityToNbt(Object entity);

    /**
     * Register a texture with the texture manager.
     * Handles Identifier vs ResourceLocation differences.
     *
     * @param ncId The NcId for the texture location
     * @param texture The DynamicTexture to register
     */
    void registerTexture(NcId ncId, Object texture);

    /**
     * Release/unregister a texture from the texture manager.
     * Handles Identifier vs ResourceLocation differences.
     *
     * @param ncId The NcId for the texture location
     */
    void releaseTexture(NcId ncId);

    /**
     * Create a KeyMapping category string.
     * In 1.21.x KeyMapping.Category constructor takes Identifier.
     * In 1.20.x it takes String.
     * This method returns a String that works for both versions.
     *
     * @param ncId The NcId for the category
     * @return A category string suitable for KeyMapping
     */
    String getKeyMappingCategory(NcId ncId);

    /**
     * Create a SoundEvent from a resource location.
     * Wraps SoundEvent.createVariableRangeEvent() which takes Identifier/ResourceLocation.
     *
     * @param ncId The NcId for the sound
     * @return The SoundEvent object
     */
    Object createSoundEvent(NcId ncId);

    /**
     * Check if a sound exists in the SoundManager.
     *
     * @param soundManager The SoundManager instance
     * @param ncId The NcId for the sound
     * @return The SoundEvent if it exists, null otherwise
     */
    Object getSoundEvent(Object soundManager, NcId ncId);

    /**
     * Create a SimpleSoundInstance for playing UI sounds.
     *
     * @param ncId The NcId for the sound
     * @param volume The volume (1.0 for normal)
     * @param pitch The pitch (1.0 for normal)
     * @return The SimpleSoundInstance object
     */
    Object createSimpleSoundInstance(NcId ncId, float volume, float pitch);

    /**
     * Create an AbstractSoundInstance subclass for custom sound playback.
     * Used by SoundInkAction for narrative sound effects.
     *
     * @param ncId The NcId for the sound
     * @param source The SoundSource category
     * @param volume The volume
     * @param pitch The pitch
     * @param random The RandomSource
     * @param looping Whether the sound loops
     * @param delay Delay before playing
     * @param attenuation The attenuation type (as int: 0=NONE, 1=LINEAR)
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param relative Whether position is relative
     * @return The AbstractSoundInstance object
     */
    Object createSoundInstance(NcId ncId, Object source, float volume, float pitch,
            Object random, boolean looping, int delay, int attenuation,
            double x, double y, double z, boolean relative);

    /**
     * Create a Style with a custom font resource.
     * Wraps FontDescription.Resource which takes Identifier/ResourceLocation.
     *
     * @param baseStyle The base Style to modify
     * @param fontId The NcId for the font resource
     * @return A new Style with the font applied
     */
    Object withFont(Object baseStyle, NcId fontId);

    /**
     * Open a file or path in the system file browser.
     * In 1.20.x uses Util.getPlatform().openFile(file).
     * In 1.21.x uses Util.getPlatform().openPath(path).
     *
     * @param path The path to open (File or Path object)
     */
    void openPath(Object path);

    /**
     * Get the player's current game mode.
     * In 1.20.x uses player.gameMode.getGameModeForPlayer().
     * In 1.21.x uses player.gameMode().
     *
     * @param player The ServerPlayer
     * @return The GameType enum value
     */
    Object getPlayerGameMode(Object player);

    /**
     * Check if a player is looking at an entity (isLookingAtMe pattern).
     * In 1.21.x uses LivingEntity.isLookingAtMe(player, threshold, checkSpectate, checkInvisible, targetY).
     * In 1.20.x uses custom ray-cast implementation.
     *
     * @param entity The entity being looked at (LivingEntity)
     * @param player The player doing the looking
     * @param dotThreshold Dot product threshold for look detection (e.g., 0.2)
     * @param targetY The Y coordinate to target (typically entity.getY() + entity.getBbHeight()/2)
     * @return true if the player is looking at the entity
     */
    boolean isPlayerLookingAtEntity(Object entity, Object player, double dotThreshold, double targetY);

    /**
     * Check if entity is a boat type.
     * Boat in 1.20.x, AbstractBoat in 1.21.x.
     *
     * @param entity The entity to check
     * @return true if it's a boat-type entity
     */
    boolean isBoatType(Object entity);

    /**
     * Check if EquipmentSlot.SADDLE exists in this version.
     * Added in 1.21.x for horse equipment.
     *
     * @return true if SADDLE slot is available
     */
    boolean hasSaddleSlot();

    /**
     * Check if EquipmentSlot.BODY exists in this version.
     * Added in 1.21.x for armor entities.
     *
     * @return true if BODY slot is available
     */
    boolean hasBodySlot();
}
