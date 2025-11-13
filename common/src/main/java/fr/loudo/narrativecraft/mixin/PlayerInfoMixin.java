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
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Util;
import java.io.File;
import java.util.ArrayList;
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

    @Inject(method = "getProfile", at = @At("RETURN"), cancellable = true)
    private void narrativecraft$getProfile(CallbackInfoReturnable<GameProfile> callbackInfo) {
        if (!"_username_".equals(this.profile.getName())) return;
        GameProfile originalProfile = callbackInfo.getReturnValue();
        String playerName = Minecraft.getInstance().player.getName().getString();
        callbackInfo.setReturnValue(new GameProfile(originalProfile.getId(), playerName));
    }

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void narrativecraft$getSkin(CallbackInfoReturnable<PlayerSkin> callbackInfo) {
        Minecraft minecraft = Minecraft.getInstance();
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(minecraft.player);
        if (playerSession == null) return;
        if (minecraft.player.getGameProfile().equals(this.profile) && playerSession.getStoryHandler() != null) {
            CharacterStory mainCharacter =
                    NarrativeCraftMod.getInstance().getCharacterManager().getMainCharacter();
            if (mainCharacter != null
                    && mainCharacter.getMainCharacterAttribute().isSameSkinAsTheir()) {
                ResourceLocation mainCharacterSkin = NarrativeCraftFile.getMainCharacterSkin();
                PlayerSkin.Model playerModelType;
                if (mainCharacterSkin != null) {
                    try {
                        playerModelType = PlayerSkin.Model.valueOf(
                                mainCharacter.getModel().name());
                    } catch (IllegalArgumentException exception) {
                        playerModelType = PlayerSkin.Model.WIDE;
                    }
                    PlayerSkin playerSkin = new PlayerSkin(mainCharacterSkin, null, null, null, playerModelType, true);
                    callbackInfo.setReturnValue(playerSkin);
                    return;
                }
            }
        }

        for (CharacterRuntime characterRuntime : new ArrayList<>(playerSession.getCharacterRuntimes())) {
            if (characterRuntime.getEntity() == null) continue;

            CharacterStory characterStory = characterRuntime.getCharacterStory();
            var mainCharacterAttribute = characterStory.getMainCharacterAttribute();

            PlayerSkin.Model playerModelType;
            try {
                playerModelType =
                        PlayerSkin.Model.valueOf(characterStory.getModel().name());
            } catch (IllegalArgumentException exception) {
                playerModelType = PlayerSkin.Model.WIDE;
            }

            File currentSkinFile = characterRuntime.getCharacterSkinController().getCurrentSkin();
            if (currentSkinFile == null) continue;

            ResourceLocation skinLocation = ResourceLocation.fromNamespaceAndPath(
                    NarrativeCraftMod.MOD_ID,
                    "character/" + Util.snakeCase(characterStory.getName()) + "/"
                            + Util.snakeCase(currentSkinFile.getName()));

            PlayerSkin playerSkin = new PlayerSkin(skinLocation, null, null, null, playerModelType, true);

            if (this.profile.getName().equals(characterStory.getName())) {
                if (mainCharacterAttribute.isMainCharacter() && mainCharacterAttribute.isSameSkinAsPlayer()) {
                    callbackInfo.setReturnValue(minecraft.player.getSkin());
                    return;
                }
                callbackInfo.setReturnValue(playerSkin);
            }
        }
    }
}
