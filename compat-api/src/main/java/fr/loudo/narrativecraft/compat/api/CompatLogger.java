/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Centralized logging for compatibility layer.
 * Logs version info at boot and warns about unsupported/degraded features.
 *
 * Uses callbacks for actual logging since compat-api has no logging dependency.
 * The loader modules (fabric-1.20.6, fabric-1.21.11) set the log callbacks.
 */
public final class CompatLogger {

    private static final List<String> degradedFeatures = new ArrayList<>();
    private static boolean initialized = false;

    // Log callbacks set by loader modules
    private static Consumer<String> infoLogger = System.out::println;
    private static Consumer<String> warnLogger = s -> System.out.println("[WARN] " + s);
    private static Consumer<String> debugLogger = s -> {}; // No-op by default
    private static BiConsumer<String, Throwable> errorLogger = (s, t) -> {
        System.err.println("[ERROR] " + s);
        if (t != null) t.printStackTrace();
    };

    private CompatLogger() {
        // Utility class
    }

    /**
     * Set the log callbacks. Call this before logBootInfo().
     *
     * @param info Info level logger
     * @param warn Warn level logger
     * @param debug Debug level logger
     * @param error Error level logger (takes message and optional throwable)
     */
    public static void setLoggers(
            Consumer<String> info,
            Consumer<String> warn,
            Consumer<String> debug,
            BiConsumer<String, Throwable> error) {
        infoLogger = info != null ? info : System.out::println;
        warnLogger = warn != null ? warn : s -> System.out.println("[WARN] " + s);
        debugLogger = debug != null ? debug : s -> {};
        errorLogger = error != null ? error : (s, t) -> System.err.println("[ERROR] " + s);
    }

    /**
     * Log version and adapter information at mod boot.
     * Should be called once during mod initialization.
     *
     * @param modVersion The mod version string
     * @param loaderName The mod loader name (e.g., "Fabric", "NeoForge")
     * @param loaderVersion The mod loader version
     */
    public static void logBootInfo(String modVersion, String loaderName, String loaderVersion) {
        if (initialized) {
            warnLogger.accept("[NarrativeCraft/Compat] logBootInfo() called multiple times!");
            return;
        }
        initialized = true;

        IVersionAdapter adapter = VersionAdapterLoader.getAdapter();

        infoLogger.accept("[NarrativeCraft/Compat] ========================================");
        infoLogger.accept("[NarrativeCraft/Compat] NarrativeCraft Compatibility Layer");
        infoLogger.accept("[NarrativeCraft/Compat] ========================================");
        infoLogger.accept("[NarrativeCraft/Compat] Mod Version: " + modVersion);
        infoLogger.accept("[NarrativeCraft/Compat] MC Version: " + adapter.getMcVersion());
        infoLogger.accept("[NarrativeCraft/Compat] MC Major: " + adapter.getMcMajor());
        infoLogger.accept("[NarrativeCraft/Compat] Loader: " + loaderName + " " + loaderVersion);
        infoLogger.accept("[NarrativeCraft/Compat] Adapter: " + adapter.getClass().getSimpleName());
        infoLogger.accept("[NarrativeCraft/Compat] NeoForge: " + (adapter.isNeoForge() ? "Yes" : "No"));
        infoLogger.accept("[NarrativeCraft/Compat] Java Version: " + System.getProperty("java.version") +
                " (required: " + adapter.getRequiredJavaVersion() + ")");
        infoLogger.accept("[NarrativeCraft/Compat] ========================================");

        // Log supported features
        logFeatureSupport(adapter);
    }

    private static void logFeatureSupport(IVersionAdapter adapter) {
        // Check key features
        checkFeature(adapter, "blurred_background", "Blurred background rendering");
        checkFeature(adapter, "modern_vertex_consumer", "Modern VertexConsumer API");
        checkFeature(adapter, "entity_snap_to", "Entity.snapTo() method");
        checkFeature(adapter, "permission_set", "PermissionSet for commands");

        if (degradedFeatures.isEmpty()) {
            infoLogger.accept("[NarrativeCraft/Compat] Features: All supported");
        } else {
            warnLogger.accept("[NarrativeCraft/Compat] Degraded Features: " + degradedFeatures.size());
            for (String feature : degradedFeatures) {
                warnLogger.accept("[NarrativeCraft/Compat]   - " + feature);
            }
        }
    }

    private static void checkFeature(IVersionAdapter adapter, String featureId, String displayName) {
        if (!adapter.supportsFeature(featureId)) {
            degradedFeatures.add(displayName + " (" + featureId + ")");
        }
    }

    /**
     * Warn about an unsupported feature being accessed.
     * Use this when code attempts to use a feature not available in the current MC version.
     *
     * @param featureId Short identifier for the feature
     * @param description Human-readable description
     * @param fallback Description of the fallback behavior being used
     */
    public static void warnUnsupportedFeature(String featureId, String description, String fallback) {
        warnLogger.accept("[NarrativeCraft/Compat] Unsupported feature: " + featureId + " - " + description);
        warnLogger.accept("[NarrativeCraft/Compat]   Fallback: " + fallback);
    }

    /**
     * Warn about a deprecated compatibility path being used.
     *
     * @param oldApi The old API being used
     * @param newApi The new API that should be used in newer versions
     */
    public static void warnDeprecatedApi(String oldApi, String newApi) {
        debugLogger.accept("[NarrativeCraft/Compat] Using deprecated API: " + oldApi + " (modern: " + newApi + ")");
    }

    /**
     * Log when a bridge method is invoked (debug level).
     *
     * @param bridgeName The bridge class/method name
     * @param operation The operation being performed
     */
    public static void logBridgeCall(String bridgeName, String operation) {
        debugLogger.accept("[NarrativeCraft/Compat] Bridge call: " + bridgeName + "." + operation);
    }

    /**
     * Log a compatibility error that occurred at runtime.
     *
     * @param context Where the error occurred
     * @param error The exception or error message
     */
    public static void logCompatError(String context, Throwable error) {
        errorLogger.accept("[NarrativeCraft/Compat] Error in " + context + ": " + error.getMessage(), error);
    }

    /**
     * Get the list of degraded features detected at boot.
     *
     * @return List of degraded feature descriptions
     */
    public static List<String> getDegradedFeatures() {
        return new ArrayList<>(degradedFeatures);
    }

    /**
     * Check if a specific feature is degraded.
     *
     * @param featureId The feature ID to check
     * @return true if the feature is degraded/unsupported
     */
    public static boolean isFeatureDegraded(String featureId) {
        return degradedFeatures.stream().anyMatch(f -> f.contains(featureId));
    }

    /**
     * Reset the logger state. Primarily for testing.
     */
    public static void reset() {
        initialized = false;
        degradedFeatures.clear();
    }
}
