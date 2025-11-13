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
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DynamicOps;
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
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

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
                                style.withHoverEvent(new HoverEvent.ShowText(Component.literal(finalMessage)))),
                false);
        NarrativeCraftMod.LOGGER.error("Unexpected error occurred on NarrativeCraft: ", exception);
        if (Services.PLATFORM.isDevelopmentEnvironment()) {
            player.displayClientMessage(Component.literal("-- " + finalMessage).withStyle(ChatFormatting.RED), false);
        }
    }

    // https://github.com/mt1006/mc-mocap-mod/blob/1.21.1/common/src/main/java/net/mt1006/mocap/utils/Utils.java#L61
    public static CompoundTag nbtFromString(String nbtString) throws CommandSyntaxException {
        return TagParser.parseCompoundAsArgument(new StringReader(nbtString));
    }

    public static BlockState getBlockStateFromData(String data, RegistryAccess registry) {
        try {
            CompoundTag compoundTag = Util.nbtFromString(data);
            return NbtUtils.readBlockState(registry.lookupOrThrow(Registries.BLOCK), compoundTag);
        } catch (CommandSyntaxException ignored) {
            return null;
        }
    }

    public static ValueInput valueInputFromCompoundTag(RegistryAccess registryAccess, String nbtString)
            throws CommandSyntaxException {
        return TagValueInput.create(ProblemReporter.DISCARDING, registryAccess, nbtFromString(nbtString));
    }

    public static Tag getItemTag(ItemStack itemStack, RegistryAccess registryAccess) {
        DynamicOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
        Tag tag;
        try {
            tag = ItemStack.CODEC.encodeStart(ops, itemStack).getOrThrow();
        } catch (Exception exception) {
            tag = new CompoundTag();
        }
        return tag;
    }

    public static CompoundTag tagFromIdAndComponents(Item item, String data) {
        CompoundTag tag = new CompoundTag();

        try {
            tag.put("components", nbtFromString(data));
        } catch (CommandSyntaxException e) {
            return null;
        }

        tag.put("id", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
        tag.put("count", IntTag.valueOf(1));
        return tag;
    }

    public static ItemStack generateItemStackFromNBT(CompoundTag compoundTag, RegistryAccess registryAccess) {
        DynamicOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
        if (compoundTag == null) {
            return ItemStack.EMPTY;
        }
        try {
            return ItemStack.CODEC.parse(ops, compoundTag).getOrThrow();
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

    public static LivingEntity createEntityFromCharacter(CharacterStory characterStory, Level level) {
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), characterStory.getName());

        LivingEntity entity;
        if (BuiltInRegistries.ENTITY_TYPE.getId(characterStory.getEntityType())
                == BuiltInRegistries.ENTITY_TYPE.getId(EntityType.PLAYER)) {
            entity = new FakePlayer((ServerLevel) level, gameProfile);
            entity.getEntityData().set(AvatarAccessor.getDATA_PLAYER_MODE_CUSTOMISATION(), (byte) 0b01111111);
        } else {
            entity = (LivingEntity) characterStory.getEntityType().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (entity != null) {
                entity.setInvulnerable(true);
            }
            if (entity instanceof Mob mob) mob.setNoAi(true);
        }

        if (entity instanceof FakePlayer fakePlayer) {
            ((PlayerListAccessor) level.getServer().getPlayerList())
                    .getPlayersByUUID()
                    .put(fakePlayer.getUUID(), fakePlayer);
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
        return entity;
    }

    public static void disconnectPlayer(Minecraft minecraft) {
        minecraft.disconnectFromWorld(Component.empty());
    }

    public static float getLetterWidth(int letterCode, Minecraft minecraft) {
        Font font = minecraft.font;
        StringSplitter splitter = font.getSplitter();
        return ((StringSplitterAccessor) splitter).getWidthProvider().getWidth(letterCode, Style.EMPTY);
    }

    public static int[] getImageResolution(ResourceLocation resourceLocation) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        Optional<Resource> resource = resourceManager.getResource(resourceLocation);
        if (resource.isPresent()) {
            try (InputStream stream = resource.get().open()) {
                BufferedImage image = ImageIO.read(stream);
                int width = image.getWidth();
                int height = image.getHeight();

                return new int[] {width, height};
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    public static int getDynamicHeight(int[] resolution, int newWidth) {
        float ratio = (float) resolution[1] / resolution[0];
        return Math.round(ratio * newWidth);
    }

    public static boolean resourceExists(ResourceLocation resourceLocation) {
        return Minecraft.getInstance()
                .getResourceManager()
                .getResource(resourceLocation)
                .isPresent();
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
            float width = 0;
            for (int i = 0; i < line.length(); i++) {
                width += Util.getLetterWidth(line.codePointAt(i), minecraft);
            }
            if (width > longerSentenceWidth) {
                longerSentenceWidth = width;
                longerText = line;
            }
        }
        return longerText;
    }
}
