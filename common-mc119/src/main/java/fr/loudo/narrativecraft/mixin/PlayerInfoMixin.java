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
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC 1.19.x version of PlayerInfoMixin.
 * Key differences from 1.20.x+:
 * - Uses getSkinLocation() instead of getSkin().texture()
 * - No PlayerSkin record class
 * - Different skin API structure
 */
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

    /**
     * 1.19.x: Uses getSkinLocation() which returns ResourceLocation directly
     * instead of 1.20.x+ getSkin() which returns PlayerSkin record.
     */
    @Inject(method = "getSkinLocation", at = @At("RETURN"), cancellable = true)
    private void narrativecraft$getSkinLocation(CallbackInfoReturnable<ResourceLocation> callbackInfo) {
        ResourceLocation customTexture = narrativecraft$getCustomSkinLocation();
        if (customTexture != null) {
            callbackInfo.setReturnValue(customTexture);
        }
    }

    @Unique
    private ResourceLocation narrativecraft$getCustomSkinLocation() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return null;

        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(minecraft.player);
        if (playerSession == null) return null;

        if (minecraft.player.getGameProfile().equals(this.profile) && playerSession.getStoryHandler() != null) {
            CharacterStory mainCharacter =
                    NarrativeCraftMod.getInstance().getCharacterManager().getMainCharacter();
            if (mainCharacter != null
                    && mainCharacter.getMainCharacterAttribute().isSameSkinAsTheir()) {
                NcId ncId = NarrativeCraftFile.getMainCharacterSkin();
                if (ncId != null) {
                    return (ResourceLocation) Services.getVersionAdapter().getIdBridge().toMc(ncId);
                }
            }
        }

        for (CharacterRuntime characterRuntime : new ArrayList<>(playerSession.getCharacterRuntimes())) {
            if (characterRuntime.getEntity() == null) continue;

            CharacterStory characterStory = characterRuntime.getCharacterStory();
            var mainCharacterAttribute = characterStory.getMainCharacterAttribute();

            File currentSkinFile = characterRuntime.getCharacterSkinController().getCurrentSkin();
            if (currentSkinFile == null) continue;

            ResourceLocation skinLocation = new ResourceLocation(
                    NarrativeCraftMod.MOD_ID,
                    "character/" + Util.snakeCase(characterStory.getName()) + "/"
                            + Util.snakeCase(currentSkinFile.getName()));

            if (this.profile.getName().equals(characterStory.getName())) {
                if (mainCharacterAttribute.isMainCharacter() && mainCharacterAttribute.isSameSkinAsPlayer()) {
                    PlayerInfo localPlayerInfo = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
                    if (localPlayerInfo != null) {
                        // 1.19.x: Use getSkinLocation() directly
                        return localPlayerInfo.getSkinLocation();
                    }
                }
                return skinLocation;
            }
        }
        return null;
    }

    /**
     * 1.19.x: Uses getModelName() which returns String directly
     * instead of 1.20.x+ getSkin().model().
     */
    @Inject(method = "getModelName", at = @At("RETURN"), cancellable = true)
    private void narrativecraft$getModelName(CallbackInfoReturnable<String> callbackInfo) {
        String customModel = narrativecraft$getCustomModelName();
        if (customModel != null) {
            callbackInfo.setReturnValue(customModel);
        }
    }

    @Unique
    private String narrativecraft$getCustomModelName() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return null;

        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getSessionByPlayer(minecraft.player);
        if (playerSession == null) return null;

        if (minecraft.player.getGameProfile().equals(this.profile) && playerSession.getStoryHandler() != null) {
            CharacterStory mainCharacter =
                    NarrativeCraftMod.getInstance().getCharacterManager().getMainCharacter();
            if (mainCharacter != null
                    && mainCharacter.getMainCharacterAttribute().isSameSkinAsTheir()) {
                return mainCharacter.getModel().name().toLowerCase();
            }
        }

        for (CharacterRuntime characterRuntime : new ArrayList<>(playerSession.getCharacterRuntimes())) {
            if (characterRuntime.getEntity() == null) continue;

            CharacterStory characterStory = characterRuntime.getCharacterStory();

            if (this.profile.getName().equals(characterStory.getName())) {
                return characterStory.getModel().name().toLowerCase();
            }
        }
        return null;
    }
}
