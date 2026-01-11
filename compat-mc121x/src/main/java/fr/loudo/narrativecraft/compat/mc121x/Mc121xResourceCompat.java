/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc121x;

import fr.loudo.narrativecraft.compat.api.IResourceCompat;
import net.minecraft.resources.Identifier;

/**
 * Resource compatibility implementation for MC 1.21.x.
 * Uses net.minecraft.resources.Identifier (Yarn/Parchment mappings for 1.21.x).
 */
public class Mc121xResourceCompat implements IResourceCompat {

    @Override
    public Object create(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    @Override
    public Object parse(String location) {
        return Identifier.parse(location);
    }

    @Override
    public String getNamespace(Object resourceLocation) {
        return ((Identifier) resourceLocation).getNamespace();
    }

    @Override
    public String getPath(Object resourceLocation) {
        return ((Identifier) resourceLocation).getPath();
    }
}
