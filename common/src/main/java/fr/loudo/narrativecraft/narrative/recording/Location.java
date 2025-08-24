/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
