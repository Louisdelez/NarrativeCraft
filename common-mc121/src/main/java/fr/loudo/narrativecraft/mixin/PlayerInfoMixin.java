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
import fr.loudo.narrativecraft.compat.api.NcId;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.util.Util;
import java.io.File;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
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
        if (!"_username_".equals(this.profile.name())) return;
        GameProfile originalProfile = callbackInfo.getReturnValue();
        String playerName = Minecraft.getInstance().player.getName().getString();
        callbackInfo.setReturnValue(new GameProfile(originalProfile.id(), playerName));
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
                NcId ncId = NarrativeCraftFile.getMainCharacterSkin();
                Identifier mainCharacterSkin = ncId != null
                        ? (Identifier) Services.getVersionAdapter().getIdBridge().toMc(ncId)
                        : null;
                PlayerModelType playerModelType;
                if (mainCharacterSkin != null) {
                    try {
                        playerModelType =
                                PlayerModelType.valueOf(mainCharacter.getModel().name());
                    } catch (IllegalArgumentException exception) {
                        playerModelType = PlayerModelType.WIDE;
                    }
                    PlayerSkin playerSkin = PlayerSkin.insecure(
                            new ClientAsset.ResourceTexture(mainCharacterSkin, mainCharacterSkin),
                            null,
                            null,
                            playerModelType);
                    callbackInfo.setReturnValue(playerSkin);
                    return;
                }
            }
        }

        for (CharacterRuntime characterRuntime : new ArrayList<>(playerSession.getCharacterRuntimes())) {
            if (characterRuntime.getEntity() == null) continue;

            CharacterStory characterStory = characterRuntime.getCharacterStory();
            var mainCharacterAttribute = characterStory.getMainCharacterAttribute();

            PlayerModelType playerModelType;
            try {
                playerModelType =
                        PlayerModelType.valueOf(characterStory.getModel().name());
            } catch (IllegalArgumentException exception) {
                playerModelType = PlayerModelType.WIDE;
            }

            File currentSkinFile = characterRuntime.getCharacterSkinController().getCurrentSkin();
            if (currentSkinFile == null) continue;

            Identifier skinLocation = Identifier.fromNamespaceAndPath(
                    NarrativeCraftMod.MOD_ID,
                    "character/" + Util.snakeCase(characterStory.getName()) + "/"
                            + Util.snakeCase(currentSkinFile.getName()));

            PlayerSkin playerSkin = PlayerSkin.insecure(
                    new ClientAsset.ResourceTexture(skinLocation, skinLocation), null, null, playerModelType);

            if (this.profile.name().equals(characterStory.getName())) {
                if (mainCharacterAttribute.isMainCharacter() && mainCharacterAttribute.isSameSkinAsPlayer()) {
                    callbackInfo.setReturnValue(minecraft.player.getSkin());
                    return;
                }
                callbackInfo.setReturnValue(playerSkin);
            }
        }
    }
}
