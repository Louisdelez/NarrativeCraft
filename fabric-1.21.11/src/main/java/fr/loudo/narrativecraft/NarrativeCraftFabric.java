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

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.loudo.narrativecraft.compat.api.CompatLogger;
import fr.loudo.narrativecraft.registers.CommandsRegister;
import fr.loudo.narrativecraft.registers.EventsRegister;
import fr.loudo.narrativecraft.registers.ModKeysRegister;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class NarrativeCraftFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Set up compat loggers using mod's logger
        CompatLogger.setLoggers(
                NarrativeCraftMod.LOGGER::info,
                NarrativeCraftMod.LOGGER::warn,
                NarrativeCraftMod.LOGGER::debug,
                (msg, t) -> NarrativeCraftMod.LOGGER.error(msg, t)
        );

        // Log compat information at boot
        String loaderVersion = FabricLoader.getInstance()
                .getModContainer("fabricloader")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        CompatLogger.logBootInfo(NarrativeCraftMod.MAJOR_VERSION, "Fabric", loaderVersion);

        NarrativeCraftMod.commonInit();
        CommandsRegister.register();
        EventsRegister.register();
        ModKeysRegister.register();
//
//        RenderPipeline pipeline = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET)
//                .withLocation("pipeline/text_background_see_through")
//                .withVertexShader("core/rendertype_text_background_see_through")
//                .withFragmentShader("core/rendertype_text_background_see_through")
//                .withDepthWrite(false)
//                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
//                .build());
//
//        NarrativeCraftMod.dialogBackgroundRenderType = RenderTypes.create(
//                "text_background_see_through",
//                RenderSetup.builder(RenderPipelines.TEXT_BACKGROUND_SEE_THROUGH)
//                        .useLightmap()
//                        .sortOnUpload().
//                        createRenderSetup()
//        );
    }
}
