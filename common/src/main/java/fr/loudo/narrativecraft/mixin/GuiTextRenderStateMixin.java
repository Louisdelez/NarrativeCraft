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

import fr.loudo.narrativecraft.gui.IGuiTextAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Same class as GuiTextRenderState, but has float as x and y value.
// It's needed for me, to make dialog effects such as waving or shaking no wacky and smooth
@Mixin(GuiTextRenderState.class)
public class GuiTextRenderStateMixin implements IGuiTextAccessor {
    private float xFloat;
    private float yFloat;
    
    @Redirect(
            method = "ensurePrepared",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;")
    )
    private Font.PreparedText narrativecraft$ensurePrepared(Font instance, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, boolean backgroundColor, int j) {
        float finalX = (xFloat != 0) ? xFloat : x;
        float finalY = (yFloat != 0) ? yFloat : y;
        return instance.prepareText(text, finalX, finalY, color, dropShadow, backgroundColor, j);
    }

    @Override
    public void narrativecraft$setFloatX(float floatX) {
        xFloat = floatX;
    }

    @Override
    public void narrativecraft$setFloatY(float floatY) {
        yFloat = floatY;
    }
}
