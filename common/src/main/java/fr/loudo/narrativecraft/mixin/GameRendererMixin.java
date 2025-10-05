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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.events.OnHudRender;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.inkAction.ShakeScreenInkAction;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    public void narrativecraft$getZoomLevel(CallbackInfoReturnable<Double> callbackInfo) {
        LocalPlayer player = Minecraft.getInstance().player;
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(player);
        if (playerSession == null) return;
        if (playerSession.getCurrentCamera() == null) return;
        callbackInfo.setReturnValue((double) playerSession.getCurrentCamera().getFov());
    }

    @Inject(method = "bobHurt", at = @At("RETURN"))
    public void narrativecraft$applyInkShakeScreen(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(player);
        if (playerSession == null) return;
        List<InkAction> inkActionsShake = playerSession.getClientSideInkActions().stream()
                .filter(inkAction -> inkAction instanceof ShakeScreenInkAction)
                .toList();
        for (InkAction shakeScreenInkAction : inkActionsShake) {
            shakeScreenInkAction.render(poseStack, partialTicks);
        }
    }

    @Redirect(
            method = "render",
            at =
                    @At(
                            value = "NEW",
                            target =
                                    "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)Lnet/minecraft/client/gui/GuiGraphics;"))
    private GuiGraphics narrativecraft$hudRender(Minecraft minecraft, MultiBufferSource.BufferSource bufferSource) {
        GuiGraphics guigraphics = new GuiGraphics(minecraft, bufferSource);
        OnHudRender.controllerHudInfo(guigraphics);
        OnHudRender.inkActionRender(guigraphics, minecraft.getFrameTime());
        OnHudRender.dialogRender(guigraphics, minecraft.getFrameTime());
        OnHudRender.saveIconRender(guigraphics, minecraft.getFrameTime());
        OnHudRender.storyDebugRender(guigraphics, minecraft.getFrameTime());
        return guigraphics;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getXRot()F"))
    private void narrativecraft$rotateCamera(
            float partialTicks, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(player);
        if (playerSession == null) return;
        if (playerSession.getCurrentCamera() == null) return;
        poseStack.mulPose(Axis.ZP.rotation(
                (float) Math.toRadians(playerSession.getCurrentCamera().getRoll())));
    }
}
