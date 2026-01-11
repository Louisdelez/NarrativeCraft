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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.screens.components.NarrativeCraftLogoRenderer;
import fr.loudo.narrativecraft.screens.credits.CreditScreen;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC 1.21.x-specific mixin for WinScreen.
 * Uses RenderPipelines.GUI_TEXTURED for texture blitting.
 */
@Mixin(WinScreen.class)
public class WinScreenMixin {

    @Mutable
    @Shadow
    @Final
    private float unmodifiedScrollSpeed;

    @Shadow
    private float scrollSpeed;

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void narrativecraft$creditSpeed(CallbackInfo ci) {
        WinScreen winScreen = (WinScreen) (Object) this;
        if (winScreen instanceof CreditScreen) {
            this.unmodifiedScrollSpeed = 1.4F;
            this.scrollSpeed = this.unmodifiedScrollSpeed;
        }
    }

    @Redirect(
            method = "render",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/LogoRenderer;renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IFI)V"))
    private void narrativecraft$renderLogo(
            LogoRenderer instance, GuiGraphics guiGraphics, int p_281512_, float p_281290_, int p_282296_) {
        WinScreen winScreen = (WinScreen) (Object) this;
        if (winScreen instanceof CreditScreen creditsScreen) {
            if (Util.resourceExists(CreditScreen.LOGO)) {
                NarrativeCraftLogoRenderer narrativeCraftLogoRenderer =
                        NarrativeCraftMod.getInstance().getNarrativeCraftLogoRenderer();
                Identifier logoId = (Identifier) Services.getVersionAdapter().getIdBridge().toMc(CreditScreen.LOGO);
                guiGraphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        logoId,
                        creditsScreen.width / 2 - 128,
                        creditsScreen.height + 50 - narrativeCraftLogoRenderer.getImageHeight() / 2,
                        0,
                        0,
                        256,
                        narrativeCraftLogoRenderer.getImageHeight(),
                        256,
                        narrativeCraftLogoRenderer.getImageHeight(),
                        ARGB.colorFromFloat(1, 1, 1, 1));
            }
        } else {
            instance.renderLogo(guiGraphics, winScreen.width, 1.0F, winScreen.height + 50);
        }
    }
}
