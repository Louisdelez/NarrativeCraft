/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Pure Java enum representing rendering channels/layers.
 * Maps to version-specific RenderType instances in compat implementations.
 *
 * IMPORTANT: This is a pure Java type with NO Minecraft dependencies.
 * The actual RenderType mapping happens in compat-mc120x/compat-mc121x modules.
 */
public enum RenderChannel {
    /**
     * Dialog background rendering - semi-transparent text background.
     * Maps to:
     * - MC 1.21.x: RenderTypes.textBackgroundSeeThrough()
     * - MC 1.20.x: RenderType.textBackgroundSeeThrough()
     */
    DIALOG_BACKGROUND,

    /**
     * Debug line rendering for visualizations (area triggers, cutscene paths).
     * Maps to:
     * - MC 1.21.x: RenderTypes.lines()
     * - MC 1.20.x: RenderType.lines()
     */
    DEBUG_LINES
}
