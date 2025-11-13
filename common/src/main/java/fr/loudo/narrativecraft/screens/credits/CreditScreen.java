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

package fr.loudo.narrativecraft.screens.credits;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.mixin.invoker.WinScreenInvoker;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreen;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class CreditScreen extends WinScreen {

    public static final ResourceLocation LOGO =
            ResourceLocation.withDefaultNamespace("textures/narrativecraft_logo.png");

    private static final ResourceLocation BACKGROUND_IMAGE =
            ResourceLocation.withDefaultNamespace("textures/narrativecraft_credits/background.png");
    private static final ResourceLocation MUSIC = ResourceLocation.withDefaultNamespace("narrativecraft_credits.music");

    public static final SimpleSoundInstance MUSIC_INSTANCE =
            SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(MUSIC), 1, 1);

    private boolean alreadyInit;

    public CreditScreen(PlayerSession playerSession, boolean fromMainMenu, boolean showFinishScreen) {
        super(false, () -> {
            MainScreen mainScreen;
            if (showFinishScreen && !fromMainMenu) {
                mainScreen = new MainScreen(playerSession, true, false);
            } else {
                mainScreen = new MainScreen(playerSession, false, false);
            }
            Minecraft.getInstance().setScreen(mainScreen);
        });
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.getSoundManager().stop(MUSIC_INSTANCE);
    }

    @Override
    protected void init() {
        super.init();
        if (alreadyInit) return;
        alreadyInit = true;
        minecraft.getSoundManager().play(MUSIC_INSTANCE);
        ((WinScreenInvoker) this)
                .callAddCreditsLine(Component.literal("Tool Used").withStyle(ChatFormatting.GRAY), false);
        ((WinScreenInvoker) this)
                .callAddCreditsLine(
                        Component.literal("           ")
                                .append("Ink - Narrative Script Language by Inkle")
                                .withStyle(ChatFormatting.WHITE),
                        false);
        ((WinScreenInvoker) this)
                .callAddCreditsLine(
                        Component.literal("           ")
                                .append("Blade-ink-java - Ink java adaptation by BladeCoder")
                                .withStyle(ChatFormatting.WHITE),
                        false);
        ((WinScreenInvoker) this)
                .callAddCreditsLine(
                        Component.literal("           ")
                                .append("NarrativeCraft - Mod used to create this story by LOUDO")
                                .withStyle(ChatFormatting.WHITE),
                        false);
        ((WinScreenInvoker) this).callAddCreditsLine(Component.literal("           "), false);
    }

    @Override
    public boolean keyPressed(int p_169469_, int p_169470_, int p_169471_) {
        if (p_169469_ == InputConstants.KEY_ESCAPE) {
            minecraft.setScreen(null);
            onClose();
        }
        return super.keyPressed(p_169469_, p_169470_, p_169471_);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int p_294762_, int p_295473_, float p_296441_) {
        if (Util.resourceExists(BACKGROUND_IMAGE)) {
            guiGraphics.blit(
                    BACKGROUND_IMAGE,
                    0,
                    0,
                    0f,
                    0f,
                    guiGraphics.guiWidth(),
                    guiGraphics.guiHeight(),
                    guiGraphics.guiWidth(),
                    guiGraphics.guiHeight());
        } else {
            guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 0xFF000000);
        }
    }
}
