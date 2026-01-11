/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Color utility compatibility interface for abstracting ARGB differences.
 * MC 1.21.x has net.minecraft.util.ARGB, earlier versions don't.
 *
 * Pure Java interface - no Minecraft dependencies.
 */
public interface IColorCompat {

    /**
     * Create a color from ARGB components (0-255 range).
     *
     * @param alpha Alpha component (0-255)
     * @param red   Red component (0-255)
     * @param green Green component (0-255)
     * @param blue  Blue component (0-255)
     * @return Packed ARGB color integer
     */
    int color(int alpha, int red, int green, int blue);

    /**
     * Create a color with the given alpha and base color.
     *
     * @param alpha Alpha component (0-255)
     * @param color Base RGB color
     * @return Packed ARGB color integer
     */
    int color(int alpha, int color);

    /**
     * Create a color from float components (0.0-1.0 range).
     *
     * @param alpha Alpha component (0.0-1.0)
     * @param red   Red component (0.0-1.0)
     * @param green Green component (0.0-1.0)
     * @param blue  Blue component (0.0-1.0)
     * @return Packed ARGB color integer
     */
    int colorFromFloat(float alpha, float red, float green, float blue);
}
