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

package fr.loudo.narrativecraft.util;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.compat.api.IUtilCompat;
import fr.loudo.narrativecraft.compat.api.VersionAdapterLoader;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.platform.Services;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Core utility class with version-agnostic methods.
 * For version-specific utilities, use {@link #getUtilCompat()}.
 */
public class Util {

    public static final String REGEX_FLOAT = "^-?\\d*(\\.\\d*)?$";
    public static final String REGEX_FLOAT_ONLY_POSITIVE = "^\\d*(\\.\\d*)?$";
    public static final String REGEX_INT = "^\\d*$";
    public static final String REGEX_NO_SPECIAL_CHARACTERS = "[a-zA-Z0-9 _-]*";

    /**
     * Get the version-specific utility compat layer.
     * Use this for methods that differ between MC versions.
     */
    public static IUtilCompat getUtilCompat() {
        return VersionAdapterLoader.getAdapter().getUtilCompat();
    }

    public static String snakeCase(String value) {
        return String.join("_", value.toLowerCase().split(" "));
    }

    public static void sendCrashMessage(Player player, Exception exception) {
        String message = exception.getMessage();
        if (message == null) {
            message = "";
        }
        String finalMessage = message;
        player.displayClientMessage(
                Translation.message("crash.global-message")
                        .withStyle(ChatFormatting.RED)
                        .withStyle((style) ->
                                style.withHoverEvent(new HoverEvent.ShowText(Component.literal(finalMessage)))),
                false);
        NarrativeCraftMod.LOGGER.error("Unexpected error occurred on NarrativeCraft: ", exception);
        if (Services.PLATFORM.isDevelopmentEnvironment()) {
            player.displayClientMessage(Component.literal("-- " + finalMessage).withStyle(ChatFormatting.RED), false);
        }
    }

    public static boolean isSameEntity(Entity entity1, Entity entity2) {
        if (entity1 == null || entity2 == null) return false;
        return entity1.getUUID().equals(entity2.getUUID());
    }

    public static boolean isSamePlayer(ServerPlayer player1, ServerPlayer player2) {
        if (player1 == null || player2 == null) return false;
        return player1.getUUID().equals(player2.getUUID());
    }

    public static int getDynamicHeight(int[] resolution, int newWidth) {
        float ratio = (float) resolution[1] / resolution[0];
        return Math.round(ratio * newWidth);
    }

    public static List<String> splitText(String text, Font font, int width) {
        List<String> finalString = new ArrayList<>();
        List<FormattedCharSequence> charSequences = font.split(FormattedText.of(text), width);
        for (FormattedCharSequence chara : charSequences) {
            StringBuilder stringBuilder = new StringBuilder();
            chara.accept((i, style, i1) -> {
                stringBuilder.appendCodePoint(i1);
                return true;
            });
            finalString.add(stringBuilder.toString());
        }
        return finalString;
    }

    public static String getLongerTextLine(List<String> lines, Minecraft minecraft) {
        float longerSentenceWidth = 0;
        String longerText = "";
        for (String line : lines) {
            float width = getLetterWidth(line, minecraft);
            if (width > longerSentenceWidth) {
                longerSentenceWidth = width;
                longerText = line;
            }
        }
        return longerText;
    }

    /**
     * Get width of a string using the font.
     */
    public static float getLetterWidth(String text, Minecraft minecraft) {
        return minecraft.font.width(text);
    }

    /**
     * Get width of a single character using the compat layer.
     */
    public static float getLetterWidth(int letterCode, Minecraft minecraft) {
        return getUtilCompat().getLetterWidth(letterCode, minecraft);
    }

    // ========== Version-specific delegate methods ==========
    // These delegate to the compat layer for version-specific implementations

    public static BlockState getBlockStateFromData(String data, RegistryAccess registry) {
        Object result = getUtilCompat().getBlockStateFromData(data, registry);
        return result != null ? (BlockState) result : null;
    }

    public static Object valueInputFromCompoundTag(RegistryAccess registryAccess, String nbtString, Entity entity) {
        return getUtilCompat().createValueInputFromNbt(entity, nbtString);
    }

    public static Tag getItemTag(ItemStack itemStack, RegistryAccess registryAccess) {
        return (Tag) getUtilCompat().getItemTag(itemStack, registryAccess);
    }

    public static LivingEntity createEntityFromCharacter(CharacterStory characterStory, Level level) {
        com.mojang.authlib.GameProfile gameProfile =
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), characterStory.getName());

        LivingEntity entity;
        if (net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getId(characterStory.getEntityType())
                == net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getId(
                        net.minecraft.world.entity.EntityType.PLAYER)) {
            entity = new FakePlayer((net.minecraft.server.level.ServerLevel) level, gameProfile);
            // Set player skin layers - uses AvatarAccessor which is in common
            entity.getEntityData()
                    .set(
                            fr.loudo.narrativecraft.mixin.accessor.AvatarAccessor.getDATA_PLAYER_MODE_CUSTOMISATION(),
                            (byte) 0b01111111);
        } else {
            // Use compat layer for entity creation (handles EntitySpawnReason differences)
            entity = (LivingEntity) getUtilCompat().createEntityFromType(characterStory.getEntityType(), level);
            if (entity != null) {
                entity.setInvulnerable(true);
            }
            if (entity instanceof net.minecraft.world.entity.Mob mob) mob.setNoAi(true);
        }
        return entity;
    }

    public static void spawnEntity(Entity entity, Level level) {
        if (entity instanceof FakePlayer fakePlayer) {
            ((fr.loudo.narrativecraft.mixin.accessor.PlayerListAccessor)
                            level.getServer().getPlayerList())
                    .getPlayersByUUID()
                    .put(fakePlayer.getUUID(), fakePlayer);
            level.getServer()
                    .getPlayerList()
                    .broadcastAll(new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket(
                            net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                            fakePlayer));
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.addNewPlayer(fakePlayer);
            }
            addFakePlayerUUID(fakePlayer);
        } else {
            level.addFreshEntity(entity);
        }
    }

    public static CompoundTag tagFromIdAndComponents(Item item, String data) {
        Object result = getUtilCompat().tagFromIdAndComponents(item, data);
        return result != null ? (CompoundTag) result : null;
    }

    public static ItemStack generateItemStackFromNBT(CompoundTag compoundTag, RegistryAccess registryAccess) {
        Object result = getUtilCompat().generateItemStackFromNBT(compoundTag, registryAccess);
        return result != null ? (ItemStack) result : ItemStack.EMPTY;
    }

    public static void disconnectPlayer(Minecraft minecraft) {
        getUtilCompat().disconnectPlayer(minecraft);
    }

    public static int[] getImageResolution(Object resourceLocation) {
        return getUtilCompat().getImageResolution(resourceLocation);
    }

    public static boolean resourceExists(Object resourceLocation) {
        return getUtilCompat().resourceExists(resourceLocation);
    }

    public static void addFakePlayerUUID(FakePlayer fakePlayer) {
        if (NarrativeCraftMod.server == null) return;
        ((fr.loudo.narrativecraft.mixin.accessor.PlayerListAccessor) NarrativeCraftMod.server.getPlayerList())
                .getPlayersByUUID()
                .put(fakePlayer.getUUID(), fakePlayer);
    }

    public static void removeFakePlayerUUID(FakePlayer fakePlayer) {
        if (NarrativeCraftMod.server == null) return;
        ((fr.loudo.narrativecraft.mixin.accessor.PlayerListAccessor) NarrativeCraftMod.server.getPlayerList())
                .getPlayersByUUID()
                .remove(fakePlayer.getUUID());
    }

    // ========== Screen Factory Registration ==========
    // Used for version-specific screens that can't be referenced from common

    private static BiFunction<Object, Object, Object> cutsceneEasingsScreenFactory;

    /**
     * Register the factory for creating CutsceneKeyframeEasingsScreen.
     * Called from version-specific modules during initialization.
     *
     * @param factory Function(parentScreen, cutsceneKeyframe) -> Screen
     */
    public static void registerCutsceneEasingsScreenFactory(BiFunction<Object, Object, Object> factory) {
        cutsceneEasingsScreenFactory = factory;
    }

    /**
     * Create a CutsceneKeyframeEasingsScreen using the registered factory.
     *
     * @param parentScreen The parent screen to return to
     * @param keyframe The CutsceneKeyframe to configure
     * @return The created Screen, cast from Object
     */
    public static Object createCutsceneEasingsScreen(Object parentScreen, Object keyframe) {
        if (cutsceneEasingsScreenFactory == null) {
            throw new IllegalStateException(
                    "CutsceneEasingsScreen factory not registered - version module not initialized?");
        }
        return cutsceneEasingsScreenFactory.apply(parentScreen, keyframe);
    }
}
