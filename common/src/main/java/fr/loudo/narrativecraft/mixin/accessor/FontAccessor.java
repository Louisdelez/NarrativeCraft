package fr.loudo.narrativecraft.mixin.accessor;

import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Font.class)
public interface FontAccessor {
    @Accessor boolean getFilterFishyGlyphs();
}
