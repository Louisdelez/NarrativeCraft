package fr.loudo.narrativecraft.mixin.accessor;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardForgeAccessor {

    @Accessor
    static RenderStateShard.ShaderStateShard getPOSITION_COLOR_SHADER() { return null; }

    @Accessor
    static RenderStateShard.TransparencyStateShard getTRANSLUCENT_TRANSPARENCY() { return null; }

    @Accessor
    static RenderStateShard.EmptyTextureStateShard getNO_TEXTURE() { return null; }

    @Accessor
    static RenderStateShard.DepthTestStateShard getNO_DEPTH_TEST() { return null; }

    @Accessor
    static RenderStateShard.CullStateShard getNO_CULL() { return null; }

    @Accessor
    static RenderStateShard.WriteMaskStateShard getCOLOR_WRITE() { return null; }

}
