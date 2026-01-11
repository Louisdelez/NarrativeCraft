/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc119x;

import fr.loudo.narrativecraft.compat.api.IIdBridge;
import fr.loudo.narrativecraft.compat.api.NcId;
import net.minecraft.resources.ResourceLocation;

/**
 * ID bridge for MC 1.19.x.
 * Converts between NcId and net.minecraft.resources.ResourceLocation.
 */
public class Mc119xIdBridge implements IIdBridge {

    @Override
    public Object toMc(NcId ncId) {
        return new ResourceLocation(ncId.namespace(), ncId.path());
    }

    @Override
    public NcId fromMc(Object mcId) {
        if (mcId instanceof ResourceLocation rl) {
            return new NcId(rl.getNamespace(), rl.getPath());
        }
        throw new IllegalArgumentException("Expected ResourceLocation, got: " + mcId.getClass().getName());
    }
}
