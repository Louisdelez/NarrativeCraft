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

package fr.loudo.narrativecraft.items;

import com.mojang.authlib.properties.Property;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CutsceneEditItems {

    private static final Property CAMERA_TEXTURE = new Property(
            "textures",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTg2OWRiODU4M2I4NjdmODRhMjc3YTliNGY5MDE3ZmM1ZTIyNzQ0MTMzMzkxZjcwZDQ1M2I2NzljMzIzZjljZCJ9fX0=");
    private static final Property TRIGGER_TEXTURE = new Property(
            "textures",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ2NWMxMjE5NThjMDUyMmUzZGNjYjNkMTRkNjg2MTJkNjMxN2NkMzgwYjBlNjQ2YjYxYjc0MjBiOTA0YWYwMiJ9fX0=");
    public static ItemStack camera;
    public static ItemStack trigger;

    public static void init(RegistryAccess access) {
        camera = getItemWithTexture("", access, Items.PLAYER_HEAD, CAMERA_TEXTURE);
        trigger = getItemWithTexture("", access, Items.PLAYER_HEAD, TRIGGER_TEXTURE);
    }

    private static ItemStack getItem(String name, RegistryAccess registryAccess, Item item) {

        CompoundTag tag = Util.tagFromIdAndComponents(item, "{\"minecraft:custom_name\":\"" + name + "\"}");

        return Util.generateItemStackFromNBT(tag, registryAccess);
    }

    private static ItemStack getItemWithTexture(
            String name, RegistryAccess registryAccess, Item item, Property textures) {

        CompoundTag tag = Util.tagFromIdAndComponents(
                item,
                "{\"minecraft:custom_name\":\"" + name + "\", \"minecraft:profile\":{properties:[{name: \""
                        + textures.name() + "\", value: \"" + textures.value() + "\"}]}}");

        return Util.generateItemStackFromNBT(tag, registryAccess);
    }
}
