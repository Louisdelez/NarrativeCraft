package fr.loudo.narrativecraft.mixin.invoker;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker
{
    @Invoker ItemEntity callCreateItemStackToDrop(ItemStack stack, boolean randomizeMotion, boolean includeThrower);
}