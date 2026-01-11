/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc119x;

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
 * Version adapter implementation for Minecraft 1.19.x.
 * Supports MC 1.19.4 with Fabric only (NeoForge didn't exist until 1.20.4).
 */
public class Mc119xVersionAdapter implements IVersionAdapter {

    private final IGuiRenderCompat guiRenderCompat = new Mc119xGuiRenderCompat();
    private final ICameraCompat cameraCompat = new Mc119xCameraCompat();
    private final IAudioCompat audioCompat = new Mc119xAudioCompat();
    private final IColorCompat colorCompat = new Mc119xColorCompat();
    private final IResourceCompat resourceCompat = new Mc119xResourceCompat();
    private final IUtilCompat utilCompat = new Mc119xUtilCompat();
    private final IIdBridge idBridge = new Mc119xIdBridge();
    private final IInputCompat inputCompat = new Mc119xInputCompat();

    @Override
    public String getMcMajor() {
        return "1.19";
    }

    @Override
    public String getMcVersion() {
        return "1.19.4";
    }

    @Override
    public boolean supportsFeature(String featureId) {
        return switch (featureId) {
            // General features - 1.19.4 has fewer features than 1.20.x
            case "gui_graphics" -> false; // GuiGraphics doesn't exist in 1.19.4 - use PoseStack directly
            case "parchment_mappings" -> true;
            case "mod_dev" -> true;
            case "resource_pack_v15" -> false; // Uses older resource pack format
            case "data_pack_v12" -> false; // Uses older data pack format
            // 1.19.x specific: these features are NOT supported
            case "blurred_background" -> false;
            case "modern_vertex_consumer" -> false;
            case "entity_snap_to" -> false;
            case "permission_set" -> false;
            case "vec3_lerp" -> false;
            case "is_looking_at_me" -> false;
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
        // NeoForge didn't exist in 1.19.x - always Fabric
        return false;
    }

    @Override
    public int getRequiredJavaVersion() {
        return 17;
    }

    @Override
    public Object getRenderType(RenderChannel channel) {
        return switch (channel) {
            case DIALOG_BACKGROUND -> RenderType.textBackgroundSeeThrough();
            case DEBUG_LINES -> RenderType.lines();
        };
    }
}
