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

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {

    @Shadow
    @Final
    private GameProfile profile;

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void narrativecraft$getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(Minecraft.getInstance().player);
        if (playerSession == null) return;
        List<CharacterRuntime> characterRuntimes = new ArrayList<>(playerSession.getCharacterRuntimes());
        for (CharacterRuntime characterRuntime : characterRuntimes) {
            if (characterRuntime.getEntity() == null) continue;
            if (!this.profile
                    .getName()
                    .equals(characterRuntime.getCharacterStory().getName())) continue;
            PlayerSkin.Model model = PlayerSkin.Model.WIDE;
            try {
                model = PlayerSkin.Model.valueOf(
                        characterRuntime.getCharacterStory().getModel().name());
            } catch (IllegalArgumentException ignored) {
            }
            File currentSkin = characterRuntime.getCharacterSkinController().getCurrentSkin();
            if (currentSkin == null) return;
            ResourceLocation skinLocation = ResourceLocation.fromNamespaceAndPath(
                    NarrativeCraftMod.MOD_ID,
                    "character/"
                            + Util.snakeCase(
                                    characterRuntime.getCharacterStory().getName()) + "/"
                            + Util.snakeCase(currentSkin.getName()));

            PlayerSkin playerSkin = new PlayerSkin(skinLocation, null, null, null, model, true);

            cir.setReturnValue(playerSkin);
        }
    }
}
