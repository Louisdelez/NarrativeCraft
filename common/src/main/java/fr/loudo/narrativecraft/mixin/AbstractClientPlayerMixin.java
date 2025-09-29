package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.mixin.accessor.PlayerInfoAccessor;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

    @Shadow private PlayerInfo playerInfo;

    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    private void narrativecraft$getSkinOfCharacter(CallbackInfoReturnable<ResourceLocation> cir) {
        if (this.playerInfo == null) return;
        PlayerSession playerSession = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getSessionByPlayer(Minecraft.getInstance().player);
        if (playerSession == null) return;
        List<CharacterRuntime> characterRuntimes = new ArrayList<>(playerSession.getCharacterRuntimes());
        for (CharacterRuntime characterRuntime : characterRuntimes) {
            if (characterRuntime.getEntity() == null) continue;
            if (!this.playerInfo.getProfile()
                    .getName()
                    .equals(characterRuntime.getCharacterStory().getName())) continue;
            ((PlayerInfoAccessor)playerInfo).setSkinModel(characterRuntime.getCharacterStory().getModel().name().toLowerCase());
            File currentSkin = characterRuntime.getCharacterSkinController().getCurrentSkin();
            if (currentSkin == null) return;
            ResourceLocation skinLocation = new ResourceLocation(
                    NarrativeCraftMod.MOD_ID,
                    "character/"
                            + Util.snakeCase(
                            characterRuntime.getCharacterStory().getName()) + "/"
                            + Util.snakeCase(currentSkin.getName()));

            cir.setReturnValue(skinLocation);
        }
    }
}
