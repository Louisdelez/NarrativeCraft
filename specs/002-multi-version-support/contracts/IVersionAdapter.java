package fr.loudo.narrativecraft.compat.api;

/**
 * Version adapter interface for abstracting Minecraft version differences.
 *
 * Each supported Minecraft major version (1.19, 1.20, 1.21) provides an
 * implementation of this interface loaded via ServiceLoader at runtime.
 *
 * Usage:
 *   IVersionAdapter adapter = Services.load(IVersionAdapter.class);
 *   if (adapter.supportsFeature("advanced_hud")) {
 *       // Use advanced HUD features
 *   }
 */
public interface IVersionAdapter {

    /**
     * Get the Minecraft major version this adapter supports.
     *
     * @return Version string like "1.19", "1.20", or "1.21"
     */
    String getMcMajor();

    /**
     * Get the full Minecraft version string.
     *
     * @return Version string like "1.19.4", "1.20.6", or "1.21.11"
     */
    String getMcVersion();

    /**
     * Check if a feature is supported on this Minecraft version.
     *
     * @param featureId The feature identifier (e.g., "advanced_hud", "screen_effects")
     * @return true if the feature is available
     */
    boolean supportsFeature(String featureId);

    /**
     * Get the GUI rendering compatibility layer for this version.
     *
     * @return Implementation of IGuiRenderCompat for this MC version
     */
    IGuiRenderCompat getGuiRenderCompat();

    /**
     * Get the camera compatibility layer for this version.
     *
     * @return Implementation of ICameraCompat for this MC version
     */
    ICameraCompat getCameraCompat();

    /**
     * Get the audio compatibility layer for this version.
     *
     * @return Implementation of IAudioCompat for this MC version
     */
    IAudioCompat getAudioCompat();

    /**
     * Check if running on NeoForge loader.
     *
     * @return true if NeoForge, false if Fabric
     */
    boolean isNeoForge();

    /**
     * Check if running on Fabric loader.
     *
     * @return true if Fabric, false if NeoForge
     */
    default boolean isFabric() {
        return !isNeoForge();
    }

    /**
     * Get the minimum Java version required for this target.
     *
     * @return Java version number (17 or 21)
     */
    int getRequiredJavaVersion();
}
