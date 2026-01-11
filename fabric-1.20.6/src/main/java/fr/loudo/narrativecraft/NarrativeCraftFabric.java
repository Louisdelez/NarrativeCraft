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

import fr.loudo.narrativecraft.compat.api.CompatLogger;
import fr.loudo.narrativecraft.registers.CommandsRegister;
import fr.loudo.narrativecraft.registers.EventsRegister;
import fr.loudo.narrativecraft.registers.ModKeysRegister;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric mod entry point for Minecraft 1.20.6.
 * Note: Some 1.21.x-specific features (custom render pipelines) are not available.
 */
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

        // Note: Custom render pipelines (dialog background) require MC 1.21.x
        // They will be unavailable on 1.20.6
    }
}
