/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc121x;

import fr.loudo.narrativecraft.compat.api.ICapabilityChecker;
import fr.loudo.narrativecraft.compat.api.VersionCapability;

/**
 * Capability checker for Minecraft 1.21.x.
 * All standard capabilities are available in this version.
 */
public class Mc121xCapabilityChecker implements ICapabilityChecker {

    @Override
    public boolean hasCapability(VersionCapability capability) {
        return switch (capability) {
            case GUI_GRAPHICS_API -> true;
            case POSE_STACK_API -> true;
            case COMPONENT_API -> true;
            case RESOURCE_LOCATION_PARSE -> true;
            case RENDER_SYSTEM_SETUP -> true;
            case CUSTOM_RENDER_PIPELINE -> true;
            case SOUND_ENGINE_PAUSE -> true;
            case CAMERA_DETACH_API -> true;
        };
    }

    @Override
    public String getCapabilityVersion(VersionCapability capability) {
        return hasCapability(capability) ? "1.21.x" : null;
    }
}
