/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc119x;

import fr.loudo.narrativecraft.compat.api.ICameraCompat;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Camera compatibility implementation for Minecraft 1.19.x.
 * Uses reflection for protected method access.
 */
public class Mc119xCameraCompat implements ICameraCompat {

    private static Method setPositionMethod;
    private static Method setRotationMethod;
    private static Field rotationField;

    static {
        try {
            setPositionMethod = Camera.class.getDeclaredMethod("setPosition", double.class, double.class, double.class);
            setPositionMethod.setAccessible(true);

            setRotationMethod = Camera.class.getDeclaredMethod("setRotation", float.class, float.class);
            setRotationMethod.setAccessible(true);

            rotationField = Camera.class.getDeclaredField("rotation");
            rotationField.setAccessible(true);
        } catch (Exception e) {
            // Will be handled when methods are called
        }
    }

    @Override
    public void setPosition(Object camera, double x, double y, double z) {
        try {
            if (setPositionMethod != null) {
                setPositionMethod.invoke(camera, x, y, z);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set camera position", e);
        }
    }

    @Override
    public double getX(Object camera) {
        Vec3 pos = getCameraPosition((Camera) camera);
        return pos != null ? pos.x : 0;
    }

    @Override
    public double getY(Object camera) {
        Vec3 pos = getCameraPosition((Camera) camera);
        return pos != null ? pos.y : 0;
    }

    @Override
    public double getZ(Object camera) {
        Vec3 pos = getCameraPosition((Camera) camera);
        return pos != null ? pos.z : 0;
    }

    private Vec3 getCameraPosition(Camera camera) {
        try {
            Method getPos = Camera.class.getMethod("getPosition");
            return (Vec3) getPos.invoke(camera);
        } catch (Exception e) {
            try {
                Field posField = Camera.class.getDeclaredField("position");
                posField.setAccessible(true);
                return (Vec3) posField.get(camera);
            } catch (Exception e2) {
                return Vec3.ZERO;
            }
        }
    }

    @Override
    public void setRotation(Object camera, float yaw, float pitch) {
        try {
            if (setRotationMethod != null) {
                setRotationMethod.invoke(camera, yaw, pitch);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set camera rotation", e);
        }
    }

    @Override
    public float getYaw(Object camera) {
        try {
            Method getYRot = Camera.class.getMethod("getYRot");
            return (Float) getYRot.invoke(camera);
        } catch (Exception e) {
            try {
                Field yRotField = Camera.class.getDeclaredField("yRot");
                yRotField.setAccessible(true);
                return yRotField.getFloat(camera);
            } catch (Exception e2) {
                return 0;
            }
        }
    }

    @Override
    public float getPitch(Object camera) {
        try {
            Method getXRot = Camera.class.getMethod("getXRot");
            return (Float) getXRot.invoke(camera);
        } catch (Exception e) {
            try {
                Field xRotField = Camera.class.getDeclaredField("xRot");
                xRotField.setAccessible(true);
                return xRotField.getFloat(camera);
            } catch (Exception e2) {
                return 0;
            }
        }
    }

    @Override
    public void setDetached(Object camera, boolean detached) {
        // Stub
    }

    @Override
    public boolean isDetached(Object camera) {
        try {
            Method isDetached = Camera.class.getMethod("isDetached");
            return (Boolean) isDetached.invoke(camera);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void applyQuaternion(Object camera, float[] quaternion) {
        try {
            if (rotationField != null) {
                Quaternionf rotation = (Quaternionf) rotationField.get(camera);
                rotation.set(quaternion[0], quaternion[1], quaternion[2], quaternion[3]);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply camera quaternion", e);
        }
    }

    @Override
    public float[] getQuaternion(Object camera) {
        try {
            if (rotationField != null) {
                Quaternionf rotation = (Quaternionf) rotationField.get(camera);
                return new float[] {rotation.x, rotation.y, rotation.z, rotation.w};
            }
        } catch (Exception e) {
            // Ignore
        }
        return new float[] {0, 0, 0, 1};
    }

    @Override
    public float[] getLeftVector(Object camera) {
        Camera cam = (Camera) camera;
        try {
            org.joml.Vector3f left = cam.getLeftVector();
            return new float[] {left.x, left.y, left.z};
        } catch (Exception e) {
            return new float[] {1, 0, 0};
        }
    }

    @Override
    public float[] getUpVector(Object camera) {
        Camera cam = (Camera) camera;
        try {
            org.joml.Vector3f up = cam.getUpVector();
            return new float[] {up.x, up.y, up.z};
        } catch (Exception e) {
            return new float[] {0, 1, 0};
        }
    }

    @Override
    public double[] getPosition(Object camera) {
        Camera cam = (Camera) camera;
        try {
            Vec3 pos = cam.getPosition();
            return new double[] {pos.x, pos.y, pos.z};
        } catch (Exception e) {
            return new double[] {0, 0, 0};
        }
    }

    @Override
    public Object getEntity(Object camera) {
        Camera cam = (Camera) camera;
        try {
            return cam.getEntity();
        } catch (Exception e) {
            return null;
        }
    }
}
