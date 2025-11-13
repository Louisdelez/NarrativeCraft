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
import fr.loudo.narrativecraft.mixin.accessor.PlayerInfoAccessor;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Util;
import java.io.File;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

    @Shadow
    private PlayerInfo playerInfo;

    @Inject(method = "getPlayerInfo", at = @At("RETURN"), cancellable = true)
    private void narrativecraft$getProfile(CallbackInfoReturnable<PlayerInfo> callbackInfo) {
        if (!"_username_".equals(this.playerInfo.getProfile().getName())) return;
        GameProfile originalProfile = callbackInfo.getReturnValue().getProfile();
        String playerName = Minecraft.getInstance().player.getName().getString();
        callbackInfo.setReturnValue(new PlayerInfo(new GameProfile(originalProfile.getId(), playerName), true));
    }

    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    private void narrativecraft$getSkinOfCharacter(CallbackInfoReturnable<ResourceLocation> cir) {
        if (this.playerInfo == null) return;
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(Minecraft.getInstance().player);
        if (playerSession == null) return;
        if (Minecraft.getInstance().player.getGameProfile().equals(this.playerInfo.getProfile())
                && playerSession.getStoryHandler() != null) {
            CharacterStory mainCharacter =
                    NarrativeCraftMod.getInstance().getCharacterManager().getMainCharacter();
            if (mainCharacter != null
                    && mainCharacter.getMainCharacterAttribute().isSameSkinAsTheir()) {
                ResourceLocation mainCharacterSkin = NarrativeCraftFile.getMainCharacterSkin();
                if (mainCharacterSkin != null) {
                    ((PlayerInfoAccessor) playerInfo)
                            .setSkinModel(mainCharacter.getModel().name().toLowerCase());
                    cir.setReturnValue(mainCharacterSkin);
                    return;
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

            ((PlayerInfoAccessor) playerInfo)
                    .setSkinModel(characterRuntime
                            .getCharacterStory()
                            .getModel()
                            .name()
                            .toLowerCase());

            if (this.playerInfo.getProfile().getName().equals(characterStory.getName())) {
                if (mainCharacterAttribute.isMainCharacter() && mainCharacterAttribute.isSameSkinAsPlayer()) {
                    cir.setReturnValue(Minecraft.getInstance().player.getSkinTextureLocation());
                    return;
                }
                cir.setReturnValue(skinLocation);
            }
        }
    }
}
