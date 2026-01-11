/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc121x;

import fr.loudo.narrativecraft.compat.api.IColorCompat;
import net.minecraft.util.ARGB;

/**
 * Color compatibility implementation for MC 1.21.x.
 * Delegates to net.minecraft.util.ARGB which is available in 1.21.x.
 */
public class Mc121xColorCompat implements IColorCompat {

    @Override
    public int color(int alpha, int red, int green, int blue) {
        return ARGB.color(alpha, red, green, blue);
    }

    @Override
    public int color(int alpha, int color) {
        return ARGB.color(alpha, color);
    }

    @Override
    public int colorFromFloat(float alpha, float red, float green, float blue) {
        return ARGB.colorFromFloat(alpha, red, green, blue);
    }
}
