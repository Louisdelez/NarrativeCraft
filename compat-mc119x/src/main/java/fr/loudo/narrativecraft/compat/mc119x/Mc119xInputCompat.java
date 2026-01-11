/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc119x;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.compat.api.IInputCompat;
import fr.loudo.narrativecraft.compat.api.NcKeyEvent;
import fr.loudo.narrativecraft.compat.api.NcMouseEvent;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * MC 1.19.x implementation of input compatibility.
 * Uses direct GLFW calls since KeyEvent/MouseButtonEvent don't exist in 1.19.x.
 */
public class Mc119xInputCompat implements IInputCompat {

    @Override
    public boolean isKeyDown(int keyCode) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, keyCode);
    }

    @Override
    public boolean isMouseDown(int button) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    @Override
    public NcKeyEvent toNcKeyEvent(Object nativeEvent) {
        // In 1.19.x there's no KeyEvent class - events are raw parameters
        // If called with an int[] {keyCode, scanCode, action, modifiers}, convert it
        if (nativeEvent instanceof int[] params && params.length >= 4) {
            return createKeyEvent(params[0], params[1], params[2], params[3]);
        }
        return null;
    }

    @Override
    public NcMouseEvent toNcMouseEvent(Object nativeEvent) {
        // In 1.19.x there's no MouseButtonEvent class - events are raw parameters
        // If called with an int[] {button, action, modifiers}, convert it
        if (nativeEvent instanceof int[] params && params.length >= 3) {
            return createMouseEvent(params[0], params[1], params[2]);
        }
        return null;
    }
}
