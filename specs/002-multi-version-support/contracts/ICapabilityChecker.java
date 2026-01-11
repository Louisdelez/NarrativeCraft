package fr.loudo.narrativecraft.compat.api;

import java.util.Set;

/**
 * Runtime capability checker for version-dependent features.
 *
 * Provides a centralized way to check feature availability and display
 * appropriate warnings when features are not supported on the current
 * Minecraft version.
 *
 * Usage:
 *   ICapabilityChecker checker = Services.load(ICapabilityChecker.class);
 *
 *   // Check before using a feature
 *   if (checker.isFeatureAvailable("screen_effects")) {
 *       applyScreenEffect();
 *   }
 *
 *   // Or warn and gracefully degrade
 *   checker.warnIfUnavailable("advanced_hud", "story: my_story.ink");
 */
public interface ICapabilityChecker {

    // ===== Feature IDs (constants for type safety) =====

    String FEATURE_DIALOG = "dialog";
    String FEATURE_CHOICES = "choices";
    String FEATURE_VARIABLES = "variables";
    String FEATURE_CUTSCENE = "cutscene";
    String FEATURE_RECORDING = "recording";
    String FEATURE_CAMERA = "camera";
    String FEATURE_TRIGGERS = "triggers";
    String FEATURE_SCREEN_EFFECTS = "screen_effects";
    String FEATURE_ADVANCED_HUD = "advanced_hud";
    String FEATURE_EMOTES = "emotes";

    // ===== Core Methods =====

    /**
     * Check if a feature is available on the current Minecraft version.
     *
     * @param featureId The feature identifier
     * @return true if the feature is available and can be used
     */
    boolean isFeatureAvailable(String featureId);

    /**
     * Get all features available on the current Minecraft version.
     *
     * @return Set of available feature IDs
     */
    Set<String> getAvailableFeatures();

    /**
     * Get all features that are unavailable on the current Minecraft version.
     *
     * @return Set of unavailable feature IDs
     */
    Set<String> getUnavailableFeatures();

    // ===== Warning System =====

    /**
     * Log a warning if a feature is not available.
     * The warning is shown once per session per feature.
     *
     * @param featureId The feature identifier
     * @param context Additional context (e.g., story name, tag name)
     */
    void warnIfUnavailable(String featureId, String context);

    /**
     * Get the degradation message for an unavailable feature.
     *
     * @param featureId The feature identifier
     * @return User-friendly message explaining the limitation, or null if available
     */
    String getDegradationMessage(String featureId);

    /**
     * Check if a warning has already been shown for a feature in this session.
     *
     * @param featureId The feature identifier
     * @return true if warning was already shown
     */
    boolean wasWarningShown(String featureId);

    /**
     * Reset warning state (useful for testing or new story sessions).
     */
    void resetWarnings();

    // ===== Capability Levels =====

    /**
     * Capability level indicating feature importance.
     */
    enum CapabilityLevel {
        /** Must work on all versions */
        CORE,
        /** May be degraded on older versions */
        ENHANCED,
        /** May not work on all versions */
        EXPERIMENTAL
    }

    /**
     * Get the capability level for a feature.
     *
     * @param featureId The feature identifier
     * @return The capability level
     */
    CapabilityLevel getCapabilityLevel(String featureId);

    // ===== Version Info =====

    /**
     * Get the current Minecraft major version.
     *
     * @return Version string like "1.19", "1.20", or "1.21"
     */
    String getCurrentMcMajor();

    /**
     * Get a formatted string describing current version capabilities.
     * Useful for logging at startup.
     *
     * @return Human-readable capability summary
     */
    String getCapabilitySummary();
}
