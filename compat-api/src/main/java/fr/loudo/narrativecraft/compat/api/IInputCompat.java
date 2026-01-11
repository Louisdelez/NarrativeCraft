/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Input compatibility interface for abstracting version-specific input handling.
 *
 * MC 1.21.x has new input event types (KeyEvent, MouseButtonEvent, InputWithModifiers).
 * MC 1.20.x uses older callback-based input handling.
 *
 * This interface provides a neutral abstraction.
 *
 * Pure Java interface - no Minecraft dependencies.
 */
public interface IInputCompat {

    /**
     * Check if a key is currently pressed.
     *
     * @param keyCode GLFW key code
     * @return true if the key is down
     */
    boolean isKeyDown(int keyCode);

    /**
     * Check if a mouse button is currently pressed.
     *
     * @param button GLFW button code (0=LEFT, 1=RIGHT, 2=MIDDLE)
     * @return true if the button is down
     */
    boolean isMouseDown(int button);

    /**
     * Convert a version-specific key event to NcKeyEvent.
     * In 1.21.x this takes a KeyEvent, in 1.20.x it constructs from raw values.
     *
     * @param nativeEvent The native event object (KeyEvent in 1.21.x, or raw params in 1.20.x)
     * @return NcKeyEvent representation, or null if conversion not possible
     */
    NcKeyEvent toNcKeyEvent(Object nativeEvent);

    /**
     * Convert a version-specific mouse event to NcMouseEvent.
     * In 1.21.x this takes a MouseButtonEvent, in 1.20.x it constructs from raw values.
     *
     * @param nativeEvent The native event object
     * @return NcMouseEvent representation, or null if conversion not possible
     */
    NcMouseEvent toNcMouseEvent(Object nativeEvent);

    /**
     * Create an NcKeyEvent from raw GLFW parameters.
     * Used for 1.20.x style callbacks and manual event creation.
     *
     * @param keyCode GLFW key code
     * @param scanCode Platform scan code
     * @param action GLFW action (0=RELEASE, 1=PRESS, 2=REPEAT)
     * @param modifiers GLFW modifier flags
     * @return NcKeyEvent representation
     */
    default NcKeyEvent createKeyEvent(int keyCode, int scanCode, int action, int modifiers) {
        boolean pressed = action != 0; // GLFW_RELEASE = 0
        boolean repeat = action == 2;  // GLFW_REPEAT = 2
        return new NcKeyEvent(keyCode, scanCode, modifiers, pressed, repeat);
    }

    /**
     * Create an NcMouseEvent from raw GLFW parameters.
     *
     * @param button GLFW button code
     * @param action GLFW action (0=RELEASE, 1=PRESS)
     * @param modifiers GLFW modifier flags
     * @return NcMouseEvent representation
     */
    default NcMouseEvent createMouseEvent(int button, int action, int modifiers) {
        boolean pressed = action == 1; // GLFW_PRESS = 1
        return new NcMouseEvent(button, modifiers, pressed);
    }
}
