/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc120x;

import fr.loudo.narrativecraft.compat.api.IResourceCompat;
import net.minecraft.resources.ResourceLocation;

/**
 * Resource compatibility implementation for MC 1.20.x.
 * Uses net.minecraft.resources.ResourceLocation (Mojang/Parchment mappings).
 */
public class Mc120xResourceCompat implements IResourceCompat {

    @Override
    public Object create(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    @Override
    public Object parse(String location) {
        return new ResourceLocation(location);
    }

    @Override
    public String getNamespace(Object resourceLocation) {
        return ((ResourceLocation) resourceLocation).getNamespace();
    }

    @Override
    public String getPath(Object resourceLocation) {
        return ((ResourceLocation) resourceLocation).getPath();
    }
}
