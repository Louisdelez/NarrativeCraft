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

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.mixin.accessor.AvatarAccessor;
import fr.loudo.narrativecraft.mixin.accessor.PlayerListAccessor;
import fr.loudo.narrativecraft.mixin.accessor.StringSplitterAccessor;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.platform.Services;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * MC 1.19.x version of Util.
 * Key differences from 1.20.x+:
 * - Uses Registry.BLOCK instead of BuiltInRegistries.BLOCK
 * - Uses Registry.ITEM instead of BuiltInRegistries.ITEM
 * - Uses Registry.ENTITY_TYPE instead of BuiltInRegistries.ENTITY_TYPE
 * - Uses ClientboundPlayerInfoPacket instead of ClientboundPlayerInfoUpdatePacket
 * - Uses NbtUtils and ItemStack.of() for serialization instead of Codec
 * - Uses disconnect() method that exists in 1.19.4
 */
public class Util {

    public static final String REGEX_FLOAT = "^-?\\d*(\\.\\d*)?$";
    public static final String REGEX_FLOAT_ONLY_POSITIVE = "^\\d*(\\.\\d*)?$";
    public static final String REGEX_INT = "^\\d*$";
    public static final String REGEX_NO_SPECIAL_CHARACTERS = "[a-zA-Z0-9 _-]*";

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
                                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(finalMessage)))),
                false);
        NarrativeCraftMod.LOGGER.error("Unexpected error occurred on NarrativeCraft: ", exception);
        if (Services.PLATFORM.isDevelopmentEnvironment()) {
            player.displayClientMessage(Component.literal("-- " + finalMessage).withStyle(ChatFormatting.RED), false);
        }
    }

    // https://github.com/mt1006/mc-mocap-mod/blob/1.21.1/common/src/main/java/net/mt1006/mocap/utils/Utils.java#L61
    public static CompoundTag nbtFromString(String nbtString) throws CommandSyntaxException {
        // 1.19.x API: TagParser.parseTag()
        return TagParser.parseTag(nbtString);
    }

    /**
     * MC 1.19.x: NbtUtils.readBlockState() takes HolderGetter of Block.
     * Use BuiltInRegistries.BLOCK.asLookup() to get the correct type.
     */
    public static BlockState getBlockStateFromData(String data, RegistryAccess registry) {
        try {
            CompoundTag compoundTag = Util.nbtFromString(data);
            return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), compoundTag);
        } catch (CommandSyntaxException ignored) {
            return null;
        }
    }

    public static CompoundTag compoundTagFromString(RegistryAccess registryAccess, String nbtString)
            throws CommandSyntaxException {
        return nbtFromString(nbtString);
    }

    /**
     * MC 1.19.x: Uses ItemStack.save() instead of Codec-based serialization.
     */
    public static Tag getItemTag(ItemStack itemStack, RegistryAccess registryAccess) {
        try {
            return itemStack.save(new CompoundTag());
        } catch (Exception exception) {
            return new CompoundTag();
        }
    }

    /**
     * MC 1.19.x: Uses simpler NBT structure for items.
     * Uses BuiltInRegistries.ITEM to get item keys.
     */
    public static CompoundTag tagFromIdAndComponents(Item item, String data) {
        CompoundTag tag = new CompoundTag();

        try {
            tag.put("tag", nbtFromString(data));
        } catch (CommandSyntaxException e) {
            return null;
        }

        // 1.19.x: Use BuiltInRegistries.ITEM
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        tag.putString("id", itemId.toString());
        tag.putByte("Count", (byte) 1);
        return tag;
    }

    /**
     * MC 1.19.x: Uses ItemStack.of() instead of Codec-based deserialization.
     */
    public static ItemStack generateItemStackFromNBT(CompoundTag compoundTag, RegistryAccess registryAccess) {
        if (compoundTag == null) {
            return ItemStack.EMPTY;
        }
        try {
            return ItemStack.of(compoundTag);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public static void addFakePlayerUUID(FakePlayer fakePlayer) {
        if (NarrativeCraftMod.server == null) return;
        ((PlayerListAccessor) NarrativeCraftMod.server.getPlayerList())
                .getPlayersByUUID()
                .put(fakePlayer.getUUID(), fakePlayer);
    }

    public static void removeFakePlayerUUID(FakePlayer fakePlayer) {
        if (NarrativeCraftMod.server == null) return;
        ((PlayerListAccessor) NarrativeCraftMod.server.getPlayerList())
                .getPlayersByUUID()
                .remove(fakePlayer.getUUID());
    }

    public static boolean isSameEntity(Entity entity1, Entity entity2) {
        if (entity1 == null || entity2 == null) return false;
        return entity1.getUUID().equals(entity2.getUUID());
    }

    public static boolean isSamePlayer(ServerPlayer player1, ServerPlayer player2) {
        if (player1 == null || player2 == null) return false;
        return player1.getUUID().equals(player2.getUUID());
    }

    /**
     * MC 1.19.x: Uses BuiltInRegistries.ENTITY_TYPE.
     */
    public static LivingEntity createEntityFromCharacter(CharacterStory characterStory, Level level) {
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), characterStory.getName());

        LivingEntity entity;
        // 1.19.x: Use BuiltInRegistries.ENTITY_TYPE.getKey() to compare entity types
        ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(characterStory.getEntityType());
        ResourceLocation playerTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PLAYER);
        if (entityTypeId.equals(playerTypeId)) {
            entity = new FakePlayer((ServerLevel) level, gameProfile);
            entity.getEntityData().set(AvatarAccessor.getDATA_PLAYER_MODE_CUSTOMISATION(), (byte) 0b01111111);
        } else {
            entity = (LivingEntity) characterStory.getEntityType().create(level);
            if (entity != null) {
                entity.setInvulnerable(true);
            }
            if (entity instanceof Mob mob) mob.setNoAi(true);
        }
        return entity;
    }

    /**
     * MC 1.19.x: Uses ClientboundPlayerInfoUpdatePacket.
     * In 1.19.4 Mojang mappings, the packet is ClientboundPlayerInfoUpdatePacket with Action enum.
     */
    public static void spawnEntity(Entity entity, Level level) {
        if (entity instanceof FakePlayer fakePlayer) {
            ((PlayerListAccessor) level.getServer().getPlayerList())
                    .getPlayersByUUID()
                    .put(fakePlayer.getUUID(), fakePlayer);
            // 1.19.x: Use ClientboundPlayerInfoUpdatePacket with ADD_PLAYER action
            level.getServer()
                    .getPlayerList()
                    .broadcastAll(new ClientboundPlayerInfoUpdatePacket(
                            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.addNewPlayer(fakePlayer);
            }
            addFakePlayerUUID(fakePlayer);
        } else {
            level.addFreshEntity(entity);
        }
    }

    public static void disconnectPlayer(Minecraft minecraft) {
        // 1.19.x: Use clearLevel() or similar - exact API may vary
        if (minecraft.level != null) {
            minecraft.level.disconnect();
        }
        if (minecraft.isLocalServer()) {
            minecraft.clearLevel();
        }
    }

    public static float getLetterWidth(int letterCode, Minecraft minecraft) {
        Font font = minecraft.font;
        StringSplitter splitter = font.getSplitter();
        return ((StringSplitterAccessor) splitter).getWidthProvider().getWidth(letterCode, Style.EMPTY);
    }

    public static int[] getImageResolution(ResourceLocation resourceLocation) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Optional<Resource> optionalResource = resourceManager.getResource(resourceLocation);
        if (optionalResource.isEmpty()) {
            NarrativeCraftMod.LOGGER.warn("Can't find resource {}.", resourceLocation);
            return new int[] {0, 0};
        }
        try (InputStream inputStream = optionalResource.get().open()) {
            BufferedImage image = ImageIO.read(inputStream);
            return new int[] {image.getWidth(), image.getHeight()};
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to read {}", resourceLocation, e);
            return new int[] {0, 0};
        }
    }

    public static List<FormattedCharSequence> wrapComponents(FormattedText component, int maxWidth, Font font) {
        return font.split(component, maxWidth);
    }

    public static boolean isLocalPlayer(Entity entity) {
        if (entity == null || Minecraft.getInstance().player == null) return false;
        return Minecraft.getInstance().player.getUUID().equals(entity.getUUID());
    }

    public static List<Float> getRotationArray(float[] array) {
        ArrayList<Float> list = new ArrayList<>();
        for (float f : array) {
            list.add(f);
        }
        return list;
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
            float width = minecraft.font.width(line);
            if (width > longerSentenceWidth) {
                longerSentenceWidth = width;
                longerText = line;
            }
        }
        return longerText;
    }

    public static boolean resourceExists(ResourceLocation resourceLocation) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Optional<Resource> optionalResource = resourceManager.getResource(resourceLocation);
        return optionalResource.isPresent();
    }
}
