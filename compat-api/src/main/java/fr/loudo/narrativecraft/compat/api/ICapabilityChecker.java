/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Checks for version-specific capabilities.
 *
 * Pure Java interface.
 */
public interface ICapabilityChecker {

    boolean hasCapability(VersionCapability capability);

    String getCapabilityVersion(VersionCapability capability);
}
