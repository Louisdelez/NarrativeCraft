/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Version-neutral mouse button event representation.
 * Maps to MouseButtonEvent in MC 1.21.x and mouse callbacks in MC 1.20.x.
 *
 * Pure Java record - no Minecraft dependencies.
 *
 * @param button GLFW button code (0=LEFT, 1=RIGHT, 2=MIDDLE, 3+=other)
 * @param modifiers Modifier flags (Shift=1, Ctrl=2, Alt=4, Super=8)
 * @param pressed true if button was pressed, false if released
 */
public record NcMouseEvent(
        int button,
        int modifiers,
        boolean pressed
) {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int MIDDLE = 2;

    public boolean isLeft() {
        return button == LEFT;
    }

    public boolean isRight() {
        return button == RIGHT;
    }

    public boolean isMiddle() {
        return button == MIDDLE;
    }

    public boolean hasShift() {
        return (modifiers & 1) != 0;
    }

    public boolean hasCtrl() {
        return (modifiers & 2) != 0;
    }

    public boolean hasAlt() {
        return (modifiers & 4) != 0;
    }
}
