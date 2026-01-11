/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc119x;

import fr.loudo.narrativecraft.compat.api.ICapabilityChecker;
import fr.loudo.narrativecraft.compat.api.VersionCapability;

/**
 * Capability checker for Minecraft 1.19.x.
 */
public class Mc119xCapabilityChecker implements ICapabilityChecker {

    @Override
    public boolean hasCapability(VersionCapability capability) {
        return switch (capability) {
            case GUI_GRAPHICS_API -> false; // GuiGraphics doesn't exist in 1.19.x
            case POSE_STACK_API -> true;
            case COMPONENT_API -> true;
            case RESOURCE_LOCATION_PARSE -> true;
            case RENDER_SYSTEM_SETUP -> true;
            case CUSTOM_RENDER_PIPELINE -> false;
            case SOUND_ENGINE_PAUSE -> true;
            case CAMERA_DETACH_API -> true;
        };
    }

    @Override
    public String getCapabilityVersion(VersionCapability capability) {
        return hasCapability(capability) ? "1.19.x" : null;
    }
}
