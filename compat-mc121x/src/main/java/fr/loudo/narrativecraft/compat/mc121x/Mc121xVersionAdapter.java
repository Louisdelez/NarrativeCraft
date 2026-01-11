/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc121x;

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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import java.util.Set;

/**
 * Version adapter implementation for Minecraft 1.21.x.
 * All features are fully supported on this version.
 */
public class Mc121xVersionAdapter implements IVersionAdapter {

    private static final String MC_MAJOR = "1.21";
    private static final String MC_VERSION = "1.21.11";
    private static final int JAVA_VERSION = 21;

    // All supported features on 1.21.x
    private static final Set<String> SUPPORTED_FEATURES = Set.of(
            // Core narrative features
            "dialog",
            "choices",
            "variables",
            "cutscene",
            "recording",
            "camera",
            "triggers",
            "screen_effects",
            "advanced_hud",
            "emotes",
            // 1.21.x API features (all supported)
            "blurred_background", // renderBlurredBackground() exists
            "modern_vertex_consumer", // Uses new .addVertex().setColor()... chain
            "entity_snap_to", // Entity.snapTo() exists
            "permission_set", // Uses PermissionSet for commands
            "vec3_lerp", // Mth.lerp(double, Vec3, Vec3) exists
            "is_looking_at_me" // LivingEntity.isLookingAtMe() exists
    );

    private final IGuiRenderCompat guiRenderCompat;
    private final ICameraCompat cameraCompat;
    private final IAudioCompat audioCompat;
    private final IColorCompat colorCompat;
    private final IResourceCompat resourceCompat;
    private final IUtilCompat utilCompat;
    private final IIdBridge idBridge;
    private final IInputCompat inputCompat;
    private final boolean isNeoForge;

    public Mc121xVersionAdapter() {
        this.guiRenderCompat = new Mc121xGuiRenderCompat();
        this.cameraCompat = new Mc121xCameraCompat();
        this.audioCompat = new Mc121xAudioCompat();
        this.colorCompat = new Mc121xColorCompat();
        this.resourceCompat = new Mc121xResourceCompat();
        this.utilCompat = new Mc121xUtilCompat();
        this.idBridge = new Mc121IdBridge();
        this.inputCompat = new Mc121xInputCompat();
        this.isNeoForge = detectNeoForge();
    }

    private boolean detectNeoForge() {
        try {
            Class.forName("net.neoforged.fml.ModList");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String getMcMajor() {
        return MC_MAJOR;
    }

    @Override
    public String getMcVersion() {
        return MC_VERSION;
    }

    @Override
    public boolean supportsFeature(String featureId) {
        return SUPPORTED_FEATURES.contains(featureId);
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
        return isNeoForge;
    }

    @Override
    public int getRequiredJavaVersion() {
        return JAVA_VERSION;
    }

    @Override
    public Object getRenderType(RenderChannel channel) {
        return switch (channel) {
            case DIALOG_BACKGROUND -> RenderTypes.textBackgroundSeeThrough();
            case DEBUG_LINES -> RenderTypes.lines();
        };
    }
}
