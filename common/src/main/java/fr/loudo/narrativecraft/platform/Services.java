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

package fr.loudo.narrativecraft.platform;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.compat.api.ICapabilityChecker;
import fr.loudo.narrativecraft.compat.api.IColorCompat;
import fr.loudo.narrativecraft.compat.api.IResourceCompat;
import fr.loudo.narrativecraft.compat.api.IUtilCompat;
import fr.loudo.narrativecraft.compat.api.IVersionAdapter;
import fr.loudo.narrativecraft.compat.api.VersionCapability;
import fr.loudo.narrativecraft.platform.services.IPlatformHelper;
import java.util.ServiceLoader;

public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    // Version compatibility layer - loaded via ServiceLoader from compat modules
    private static IVersionAdapter versionAdapter;
    private static ICapabilityChecker capabilityChecker;

    /**
     * Get the version adapter for the current Minecraft version.
     * Lazy-loaded on first access.
     *
     * @return The version adapter implementation
     */
    public static IVersionAdapter getVersionAdapter() {
        if (versionAdapter == null) {
            try {
                versionAdapter = load(IVersionAdapter.class);
                NarrativeCraftMod.LOGGER.info(
                        "Loaded version adapter for MC {} ({})",
                        versionAdapter.getMcVersion(),
                        versionAdapter.isNeoForge() ? "NeoForge" : "Fabric");
            } catch (Exception e) {
                NarrativeCraftMod.LOGGER.warn("Failed to load version adapter, using default", e);
                versionAdapter = createDefaultVersionAdapter();
            }
        }
        return versionAdapter;
    }

    /**
     * Get the capability checker for runtime feature detection.
     * Lazy-loaded on first access.
     *
     * @return The capability checker implementation
     */
    public static ICapabilityChecker getCapabilityChecker() {
        if (capabilityChecker == null) {
            try {
                capabilityChecker = load(ICapabilityChecker.class);
            } catch (Exception e) {
                // Fall back to default capability checker
                capabilityChecker = createDefaultCapabilityChecker();
            }
        }
        return capabilityChecker;
    }

    private static IVersionAdapter createDefaultVersionAdapter() {
        return new IVersionAdapter() {
            @Override
            public String getMcMajor() {
                return "1.21";
            }

            @Override
            public String getMcVersion() {
                return "1.21.11";
            }

            @Override
            public boolean supportsFeature(String featureId) {
                return true;
            }

            @Override
            public fr.loudo.narrativecraft.compat.api.IGuiRenderCompat getGuiRenderCompat() {
                return null;
            }

            @Override
            public fr.loudo.narrativecraft.compat.api.ICameraCompat getCameraCompat() {
                return null;
            }

            @Override
            public fr.loudo.narrativecraft.compat.api.IAudioCompat getAudioCompat() {
                return null;
            }

            @Override
            public IColorCompat getColorCompat() {
                // Default color compat - manual implementation
                return new IColorCompat() {
                    @Override
                    public int color(int alpha, int red, int green, int blue) {
                        return (alpha << 24) | (red << 16) | (green << 8) | blue;
                    }

                    @Override
                    public int color(int alpha, int color) {
                        return (alpha << 24) | (color & 0x00FFFFFF);
                    }

                    @Override
                    public int colorFromFloat(float alpha, float red, float green, float blue) {
                        return color((int) (alpha * 255), (int) (red * 255), (int) (green * 255), (int) (blue * 255));
                    }
                };
            }

            @Override
            public IResourceCompat getResourceCompat() {
                // Default implementation - callers must use the actual MC Identifier
                return null;
            }

            @Override
            public boolean isNeoForge() {
                try {
                    Class.forName("net.neoforged.fml.ModList");
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }

            @Override
            public int getRequiredJavaVersion() {
                return 21;
            }

            @Override
            public Object getRenderType(fr.loudo.narrativecraft.compat.api.RenderChannel channel) {
                // Default fallback - actual implementation comes from compat modules
                return null;
            }

            @Override
            public IUtilCompat getUtilCompat() {
                // Default fallback - actual implementation comes from compat modules
                return null;
            }

            @Override
            public fr.loudo.narrativecraft.compat.api.IIdBridge getIdBridge() {
                // Default fallback - actual implementation comes from compat modules
                return null;
            }

            @Override
            public fr.loudo.narrativecraft.compat.api.IInputCompat getInputCompat() {
                // Default fallback - actual implementation comes from compat modules
                return null;
            }
        };
    }

    private static ICapabilityChecker createDefaultCapabilityChecker() {
        return new ICapabilityChecker() {
            @Override
            public boolean hasCapability(VersionCapability capability) {
                // Default: all capabilities available on MC 1.21.x
                return true;
            }

            @Override
            public String getCapabilityVersion(VersionCapability capability) {
                return hasCapability(capability) ? "1.21.x" : null;
            }
        };
    }

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        NarrativeCraftMod.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
