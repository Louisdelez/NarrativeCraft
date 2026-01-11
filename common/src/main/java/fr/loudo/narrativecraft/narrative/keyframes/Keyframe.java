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

package fr.loudo.narrativecraft.narrative.keyframes;

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.mixin.invoker.ArmorStandInvoker;
import fr.loudo.narrativecraft.util.MathHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

/**
 * Base keyframe class for camera positioning.
 * This version uses compatible APIs that work across MC versions.
 */
public class Keyframe {
    protected transient ArmorStand camera;
    protected final int id;
    protected KeyframeLocation keyframeLocation;

    public Keyframe(int id, KeyframeLocation keyframeLocation) {
        this.id = id;
        this.keyframeLocation = keyframeLocation;
    }

    public void showKeyframe(ServerPlayer player) {
        camera = new ArmorStand(EntityType.ARMOR_STAND, player.level());
        ((ArmorStandInvoker) camera).callSetSmall(true);
        camera.setNoGravity(true);
        camera.setInvisible(true);
        camera.setNoBasePlate(true);
        camera.setBodyPose(new Rotations(180f, 0, 0));
        camera.setLeftLegPose(new Rotations(180f, 0, 0));
        camera.setRightLegPose(new Rotations(180f, 0, 0));
        BlockPos blockPos = new BlockPos(
                (int) keyframeLocation.getX(), (int) keyframeLocation.getY(), (int) keyframeLocation.getZ());
        player.connection.send(new ClientboundAddEntityPacket(camera, 0, blockPos));
        player.connection.send(new ClientboundSetEquipmentPacket(
                camera.getId(), List.of(new Pair<>(EquipmentSlot.HEAD, CutsceneEditItems.camera))));
        updateEntityData(player);
    }

    public void hideKeyframe(ServerPlayer player) {
        player.connection.send(new ClientboundRemoveEntitiesPacket(camera.getId()));
    }

    public void updateEntityData(ServerPlayer player) {
        float pitchHeadPos = MathHelper.wrapDegrees360(keyframeLocation.getPitch());
        camera.setHeadPose(new Rotations(
                pitchHeadPos == 0 ? 0.000001f : MathHelper.wrapDegrees360(keyframeLocation.getPitch()),
                0,
                keyframeLocation.getRoll()));

        camera.setXRot(keyframeLocation.getPitch());
        camera.setYRot(keyframeLocation.getYaw());
        camera.setYHeadRot(keyframeLocation.getYaw());

        // Position the entity - use teleportTo which is compatible across versions
        camera.teleportTo(
                keyframeLocation.getX(),
                keyframeLocation.getY() - 1,
                keyframeLocation.getZ());
        camera.setYRot(keyframeLocation.getYaw());
        camera.setXRot(keyframeLocation.getPitch());

        // Send entity data update
        player.connection.send(new ClientboundSetEntityDataPacket(
                camera.getId(), camera.getEntityData().getNonDefaultValues()));
    }

    public int getId() {
        return id;
    }

    public ArmorStand getCamera() {
        return camera;
    }

    public KeyframeLocation getKeyframeLocation() {
        return keyframeLocation;
    }

    public void setKeyframeLocation(KeyframeLocation keyframeLocation) {
        this.keyframeLocation = keyframeLocation;
    }
}
