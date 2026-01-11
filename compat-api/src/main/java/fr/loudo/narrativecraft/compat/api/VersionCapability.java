/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Enum of version-specific capabilities.
 *
 * Pure Java enum - no MC dependencies.
 */
public enum VersionCapability {
    GUI_GRAPHICS_API,
    POSE_STACK_API,
    COMPONENT_API,
    RESOURCE_LOCATION_PARSE,
    RENDER_SYSTEM_SETUP,
    CUSTOM_RENDER_PIPELINE,
    SOUND_ENGINE_PAUSE,
    CAMERA_DETACH_API
}
