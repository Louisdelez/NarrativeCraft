package fr.loudo.narrativecraft.mixin.accessor;

import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInfo.class)
public interface PlayerInfoAccessor {
    @Accessor
    void setSkinModel(String skinModel);
}
