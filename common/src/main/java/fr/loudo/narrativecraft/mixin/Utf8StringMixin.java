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

package fr.loudo.narrativecraft.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Utf8String;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Bypass Minecraft 16 characters name limit
@Mixin(Utf8String.class)
public abstract class Utf8StringMixin {

    @ModifyVariable(method = "write", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static int modifyWriteMaxLength(int maxLength) {
        return getMaxTagNameLength(maxLength);
    }

    @ModifyVariable(method = "read", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static int modifyReadMaxLength(int maxLength) {
        return getMaxTagNameLength(maxLength);
    }

    private static int getMaxTagNameLength(int maxLength) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            if (minecraft.isSingleplayer()) {
                return maxLength == 16 ? 64 : maxLength;
            } else {
                return maxLength;
            }
        }
        return maxLength;
    }
}
