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

package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = NarrativeCraftMod.MOD_ID, value = Dist.CLIENT)
public class PipelineRegisterEvent {

    @SubscribeEvent
    private static void onPipelineRegister(RegisterRenderPipelinesEvent event) {
//        RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET)
//                .withLocation("pipeline/narrativecraft_dialog_background")
//                .withVertexShader("core/rendertype_text_background_see_through")
//                .withFragmentShader("core/rendertype_text_background_see_through")
//                .withDepthWrite(false)
//                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
//                .build();
//        event.registerPipeline(pipeline);
//        NarrativeCraftMod.dialogBackgroundRenderType = RenderTypes.create(
//                "narrativecraft_dialog_background",
//                1536,
//                false,
//                true,
//                pipeline,
//                RenderTypes.CompositeState.builder()
//                        .setTextureState(RenderStateShard.NO_TEXTURE)
//                        .setLightmapState(RenderStateShard.LIGHTMAP)
//                        .createCompositeState(false));
    }
}
