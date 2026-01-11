/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc121x;

import fr.loudo.narrativecraft.compat.api.IIdBridge;
import fr.loudo.narrativecraft.compat.api.NcId;
import net.minecraft.resources.Identifier;

/**
 * ID bridge for MC 1.21.x.
 * Converts between NcId and net.minecraft.resources.Identifier.
 */
public class Mc121IdBridge implements IIdBridge {

    @Override
    public Object toMc(NcId ncId) {
        return Identifier.fromNamespaceAndPath(ncId.namespace(), ncId.path());
    }

    @Override
    public NcId fromMc(Object mcId) {
        if (mcId instanceof Identifier id) {
            return new NcId(id.getNamespace(), id.getPath());
        }
        throw new IllegalArgumentException("Expected Identifier, got: " + mcId.getClass().getName());
    }
}
