/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Version-neutral key event representation.
 * Maps to KeyEvent in MC 1.21.x and keyboard callbacks in MC 1.20.x.
 *
 * Pure Java record - no Minecraft dependencies.
 *
 * @param keyCode GLFW key code (e.g., GLFW_KEY_A = 65)
 * @param scanCode Platform-specific scan code
 * @param modifiers Modifier flags (Shift=1, Ctrl=2, Alt=4, Super=8)
 * @param pressed true if key was pressed, false if released
 * @param repeat true if this is a repeat event from key being held
 */
public record NcKeyEvent(
        int keyCode,
        int scanCode,
        int modifiers,
        boolean pressed,
        boolean repeat
) {
    /**
     * Check if Shift modifier is active.
     */
    public boolean hasShift() {
        return (modifiers & 1) != 0;
    }

    /**
     * Check if Ctrl modifier is active.
     */
    public boolean hasCtrl() {
        return (modifiers & 2) != 0;
    }

    /**
     * Check if Alt modifier is active.
     */
    public boolean hasAlt() {
        return (modifiers & 4) != 0;
    }

    /**
     * Check if Super (Windows/Command) modifier is active.
     */
    public boolean hasSuper() {
        return (modifiers & 8) != 0;
    }
}
