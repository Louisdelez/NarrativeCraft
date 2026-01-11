/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Camera compatibility layer for version-specific camera manipulation.
 *
 * Pure Java interface - uses Object for MC-specific types.
 * camera parameter is net.minecraft.client.Camera
 */
public interface ICameraCompat {

    // ===== Position =====

    void setPosition(Object camera, double x, double y, double z);

    double getX(Object camera);

    double getY(Object camera);

    double getZ(Object camera);

    // ===== Rotation =====

    void setRotation(Object camera, float yaw, float pitch);

    float getYaw(Object camera);

    float getPitch(Object camera);

    // ===== State =====

    void setDetached(Object camera, boolean detached);

    boolean isDetached(Object camera);

    // ===== Quaternion (for smooth camera) =====

    void applyQuaternion(Object camera, float[] quaternion);

    float[] getQuaternion(Object camera);

    // ===== Vectors (for rendering) =====

    /**
     * Get the camera's left vector.
     * In 1.20.x uses camera.getLeftVector().
     * In 1.21.x uses camera.leftVector().
     *
     * @param camera The Camera object
     * @return float array [x, y, z] representing the left vector
     */
    float[] getLeftVector(Object camera);

    /**
     * Get the camera's up vector.
     * In 1.20.x uses camera.getUpVector().
     * In 1.21.x uses camera.upVector().
     *
     * @param camera The Camera object
     * @return float array [x, y, z] representing the up vector
     */
    float[] getUpVector(Object camera);

    /**
     * Get the camera's position as Vec3.
     * In 1.20.x uses camera.getPosition().
     * In 1.21.x uses camera.position().
     *
     * @param camera The Camera object
     * @return double array [x, y, z] representing the position
     */
    double[] getPosition(Object camera);

    /**
     * Get the entity the camera is attached to.
     * In 1.20.x uses camera.getEntity().
     * In 1.21.x uses camera.entity().
     *
     * @param camera The Camera object
     * @return The Entity object, or null if no entity
     */
    Object getEntity(Object camera);
}
