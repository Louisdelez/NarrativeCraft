/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc121x;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.compat.api.IInputCompat;
import fr.loudo.narrativecraft.compat.api.NcKeyEvent;
import fr.loudo.narrativecraft.compat.api.NcMouseEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

/**
 * MC 1.21.x implementation of input compatibility.
 * Uses the new KeyEvent and MouseButtonEvent types.
 */
public class Mc121xInputCompat implements IInputCompat {

    @Override
    public boolean isKeyDown(int keyCode) {
        // In 1.21.x, InputConstants.isKeyDown takes Window directly
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), keyCode);
    }

    @Override
    public boolean isMouseDown(int button) {
        // Use mouseHandler to check button state in 1.21.x
        return switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> Minecraft.getInstance().mouseHandler.isLeftPressed();
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> Minecraft.getInstance().mouseHandler.isRightPressed();
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> Minecraft.getInstance().mouseHandler.isMiddlePressed();
            default -> false;
        };
    }

    @Override
    public NcKeyEvent toNcKeyEvent(Object nativeEvent) {
        if (nativeEvent instanceof KeyEvent keyEvent) {
            // In 1.21.x KeyEvent has: key(), scancode(), modifiers(), isDown()
            // repeat info may not be available directly - default to false
            return new NcKeyEvent(
                    keyEvent.key(),
                    keyEvent.scancode(),
                    keyEvent.modifiers(),
                    keyEvent.isDown(),
                    false // repeat status not available in KeyEvent
            );
        }
        return null;
    }

    @Override
    public NcMouseEvent toNcMouseEvent(Object nativeEvent) {
        if (nativeEvent instanceof MouseButtonEvent mouseEvent) {
            // In 1.21.x MouseButtonEvent has: button(), modifiers(), isDown()
            return new NcMouseEvent(
                    mouseEvent.button(),
                    mouseEvent.modifiers(),
                    mouseEvent.isDown()
            );
        }
        return null;
    }
}
