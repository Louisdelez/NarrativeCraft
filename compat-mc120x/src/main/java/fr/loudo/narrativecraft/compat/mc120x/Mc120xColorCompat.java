/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc120x;

import fr.loudo.narrativecraft.compat.api.IColorCompat;

/**
 * Color compatibility implementation for MC 1.20.x.
 * Manual implementation since net.minecraft.util.ARGB doesn't exist in 1.20.x.
 */
public class Mc120xColorCompat implements IColorCompat {

    @Override
    public int color(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @Override
    public int color(int alpha, int color) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    @Override
    public int colorFromFloat(float alpha, float red, float green, float blue) {
        int a = (int) (alpha * 255.0f);
        int r = (int) (red * 255.0f);
        int g = (int) (green * 255.0f);
        int b = (int) (blue * 255.0f);
        return color(a, r, g, b);
    }
}
