/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Marker interface for sound instances created by NarrativeCraft.
 * Used by mixins to identify narrative-controlled sounds and skip
 * default volume adjustments.
 *
 * This is a pure Java interface - no Minecraft dependencies.
 */
public interface NarrativeSoundInstance {
    // Marker interface - no methods
}
