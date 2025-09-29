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

package fr.loudo.narrativecraft;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.loudo.narrativecraft.mixin.accessor.RenderStateShardForgeAccessor;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.fml.common.Mod;

@Mod(NarrativeCraftMod.MOD_ID)
public class NarrativeCraftForge {

    public NarrativeCraftForge() {
        NarrativeCraftMod.commonInit();
        NarrativeCraftMod.dialogBackgroundRenderType = RenderType.create(
                "narrativecraft_dialog_background",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShardForgeAccessor.getPOSITION_COLOR_SHADER())
                        .setTransparencyState(RenderStateShardForgeAccessor.getTRANSLUCENT_TRANSPARENCY())
                        .setTextureState(RenderStateShardForgeAccessor.getNO_TEXTURE())
                        .setDepthTestState(RenderStateShardForgeAccessor.getNO_DEPTH_TEST())
                        .setCullState(RenderStateShardForgeAccessor.getNO_CULL())
                        .setWriteMaskState(RenderStateShardForgeAccessor.getCOLOR_WRITE())
                        .createCompositeState(false));
    }
}
