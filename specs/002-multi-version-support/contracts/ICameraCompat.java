package fr.loudo.narrativecraft.compat.api;

/**
 * Camera compatibility layer for version-specific camera APIs.
 *
 * Abstracts differences in camera manipulation between Minecraft versions.
 * The Camera class is relatively stable, but some internal methods and
 * state access patterns may differ.
 *
 * Usage:
 *   ICameraCompat compat = adapter.getCameraCompat();
 *   compat.setPosition(camera, x, y, z);
 *   compat.setRotation(camera, yaw, pitch);
 */
public interface ICameraCompat {

    /**
     * Set the camera position.
     *
     * @param camera The Minecraft Camera instance
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    void setPosition(Object camera, double x, double y, double z);

    /**
     * Get the camera X position.
     *
     * @param camera The Minecraft Camera instance
     * @return X coordinate
     */
    double getX(Object camera);

    /**
     * Get the camera Y position.
     *
     * @param camera The Minecraft Camera instance
     * @return Y coordinate
     */
    double getY(Object camera);

    /**
     * Get the camera Z position.
     *
     * @param camera The Minecraft Camera instance
     * @return Z coordinate
     */
    double getZ(Object camera);

    /**
     * Set the camera rotation.
     *
     * @param camera The Minecraft Camera instance
     * @param yaw Yaw rotation in degrees
     * @param pitch Pitch rotation in degrees
     */
    void setRotation(Object camera, float yaw, float pitch);

    /**
     * Get the camera yaw rotation.
     *
     * @param camera The Minecraft Camera instance
     * @return Yaw in degrees
     */
    float getYaw(Object camera);

    /**
     * Get the camera pitch rotation.
     *
     * @param camera The Minecraft Camera instance
     * @return Pitch in degrees
     */
    float getPitch(Object camera);

    /**
     * Set camera to detached mode (not following entity).
     *
     * @param camera The Minecraft Camera instance
     * @param detached true to detach from entity
     */
    void setDetached(Object camera, boolean detached);

    /**
     * Check if camera is in detached mode.
     *
     * @param camera The Minecraft Camera instance
     * @return true if detached
     */
    boolean isDetached(Object camera);

    /**
     * Apply a rotation quaternion to the camera.
     * Used for smooth camera interpolation in cutscenes.
     *
     * @param camera The Minecraft Camera instance
     * @param quaternion Rotation quaternion (x, y, z, w)
     */
    void applyQuaternion(Object camera, float[] quaternion);

    /**
     * Get the camera's current rotation as a quaternion.
     *
     * @param camera The Minecraft Camera instance
     * @return Quaternion array [x, y, z, w]
     */
    float[] getQuaternion(Object camera);

    /**
     * Interpolate camera position between two points.
     *
     * @param camera The Minecraft Camera instance
     * @param fromX Start X
     * @param fromY Start Y
     * @param fromZ Start Z
     * @param toX End X
     * @param toY End Y
     * @param toZ End Z
     * @param progress Interpolation progress (0.0 to 1.0)
     */
    default void lerpPosition(Object camera, double fromX, double fromY, double fromZ,
                               double toX, double toY, double toZ, float progress) {
        double x = fromX + (toX - fromX) * progress;
        double y = fromY + (toY - fromY) * progress;
        double z = fromZ + (toZ - fromZ) * progress;
        setPosition(camera, x, y, z);
    }

    /**
     * Interpolate camera rotation between two angles.
     *
     * @param camera The Minecraft Camera instance
     * @param fromYaw Start yaw
     * @param fromPitch Start pitch
     * @param toYaw End yaw
     * @param toPitch End pitch
     * @param progress Interpolation progress (0.0 to 1.0)
     */
    default void lerpRotation(Object camera, float fromYaw, float fromPitch,
                               float toYaw, float toPitch, float progress) {
        // Handle yaw wraparound
        float yawDiff = toYaw - fromYaw;
        if (yawDiff > 180) yawDiff -= 360;
        if (yawDiff < -180) yawDiff += 360;

        float yaw = fromYaw + yawDiff * progress;
        float pitch = fromPitch + (toPitch - fromPitch) * progress;
        setRotation(camera, yaw, pitch);
    }
}
