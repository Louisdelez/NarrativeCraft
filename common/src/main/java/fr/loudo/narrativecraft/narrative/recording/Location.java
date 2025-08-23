package fr.loudo.narrativecraft.narrative.recording;

import net.minecraft.world.phys.Vec3;

public record Location(double x, double y, double z, float pitch, float yaw, boolean onGround) {
    public Vec3 asVec3() {
        return new Vec3(x, y, z);
    }
    public static Location deltaLocation(Location oldLoc, Location newLoc) {
        double dX, dY, dZ;
        float dPitch, dYaw;
        dX = newLoc.x - oldLoc.x;
        dY = newLoc.y - oldLoc.y;
        dZ = newLoc.z - oldLoc.z;
        dPitch = newLoc.pitch - oldLoc.pitch;
        dYaw = newLoc.yaw - oldLoc.yaw;
        return new Location(dX, dY, dZ, dPitch, dYaw, newLoc.onGround());
    }
}
