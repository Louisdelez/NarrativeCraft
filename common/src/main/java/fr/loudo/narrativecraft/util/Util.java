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

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DynamicOps;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.mixin.accessor.PlayerListAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

public class Util {

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
        ((PlayerListAccessor) NarrativeCraftMod.server.getPlayerList())
                .getPlayersByUUID()
                .put(fakePlayer.getUUID(), fakePlayer);
    }

    public static void removeFakePlayerUUID(FakePlayer fakePlayer) {
        ((PlayerListAccessor) NarrativeCraftMod.server.getPlayerList())
                .getPlayersByUUID()
                .remove(fakePlayer.getUUID());
    }
}
