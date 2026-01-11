/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc120x;

import fr.loudo.narrativecraft.compat.api.IAudioCompat;
import fr.loudo.narrativecraft.compat.api.ICameraCompat;
import fr.loudo.narrativecraft.compat.api.IColorCompat;
import fr.loudo.narrativecraft.compat.api.IGuiRenderCompat;
import fr.loudo.narrativecraft.compat.api.IIdBridge;
import fr.loudo.narrativecraft.compat.api.IInputCompat;
import fr.loudo.narrativecraft.compat.api.IResourceCompat;
import fr.loudo.narrativecraft.compat.api.IUtilCompat;
import fr.loudo.narrativecraft.compat.api.IVersionAdapter;
import fr.loudo.narrativecraft.compat.api.RenderChannel;
import net.minecraft.client.renderer.RenderType;

/**
 * Version adapter implementation for Minecraft 1.20.x.
 * Supports MC 1.20.1 through 1.20.6 with both Fabric and NeoForge.
 */
public class Mc120xVersionAdapter implements IVersionAdapter {

    private final IGuiRenderCompat guiRenderCompat = new Mc120xGuiRenderCompat();
    private final ICameraCompat cameraCompat = new Mc120xCameraCompat();
    private final IAudioCompat audioCompat = new Mc120xAudioCompat();
    private final IColorCompat colorCompat = new Mc120xColorCompat();
    private final IResourceCompat resourceCompat = new Mc120xResourceCompat();
    private final IUtilCompat utilCompat = new Mc120xUtilCompat();
    private final IIdBridge idBridge = new Mc120IdBridge();
    private final IInputCompat inputCompat = new Mc120xInputCompat();

    @Override
    public String getMcMajor() {
        return "1.20";
    }

    @Override
    public String getMcVersion() {
        return "1.20.6";
    }

    @Override
    public boolean supportsFeature(String featureId) {
        return switch (featureId) {
            // General features
            case "gui_graphics" -> true;
            case "parchment_mappings" -> true;
            case "mod_dev" -> true;
            case "resource_pack_v15" -> true;
            case "data_pack_v12" -> true;
            // 1.20.x specific: these features are NOT supported
            case "blurred_background" -> false; // renderBlurredBackground() doesn't exist
            case "modern_vertex_consumer" -> false; // Uses old .vertex().color()... chain
            case "entity_snap_to" -> false; // Entity.snapTo() doesn't exist
            case "permission_set" -> false; // Uses int permission level
            case "vec3_lerp" -> false; // Mth.lerp(double, Vec3, Vec3) doesn't exist
            case "is_looking_at_me" -> false; // LivingEntity.isLookingAtMe() doesn't exist
            default -> false;
        };
    }

    @Override
    public IGuiRenderCompat getGuiRenderCompat() {
        return guiRenderCompat;
    }

    @Override
    public ICameraCompat getCameraCompat() {
        return cameraCompat;
    }

    @Override
    public IAudioCompat getAudioCompat() {
        return audioCompat;
    }

    @Override
    public IColorCompat getColorCompat() {
        return colorCompat;
    }

    @Override
    public IResourceCompat getResourceCompat() {
        return resourceCompat;
    }

    @Override
    public IUtilCompat getUtilCompat() {
        return utilCompat;
    }

    @Override
    public IIdBridge getIdBridge() {
        return idBridge;
    }

    @Override
    public IInputCompat getInputCompat() {
        return inputCompat;
    }

    @Override
    public boolean isNeoForge() {
        // Will be determined at runtime based on loaded platform
        try {
            Class.forName("net.neoforged.fml.ModList");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public int getRequiredJavaVersion() {
        return 21;
    }

    @Override
    public Object getRenderType(RenderChannel channel) {
        return switch (channel) {
            case DIALOG_BACKGROUND -> RenderType.textBackgroundSeeThrough();
            case DEBUG_LINES -> RenderType.lines();
        };
    }
}
