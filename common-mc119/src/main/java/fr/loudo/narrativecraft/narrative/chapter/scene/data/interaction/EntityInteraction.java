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

package fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction;

import fr.loudo.narrativecraft.narrative.Environment;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

/**
 * MC 1.20.x version of EntityInteraction.
 * Key differences:
 * - Entity.snapTo(Vec3) doesn't exist in 1.20.x - use setPos(x, y, z) instead
 */
public class EntityInteraction extends StitchInteraction {

    private transient ArmorStand armorStand;
    private final double x;
    private final double y;
    private final double z;

    public EntityInteraction(String stitch, Vec3 position) {
        super(stitch);
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }

    public void spawn(ServerPlayer player, Environment environment) {
        armorStand = new ArmorStand(EntityType.ARMOR_STAND, player.getLevel());
        armorStand.setInvisible(true);
        armorStand.setGlowingTag(environment == Environment.DEVELOPMENT);
        armorStand.setNoGravity(true);
        armorStand.setNoBasePlate(true);
        // 1.20.x: snapTo(Vec3) doesn't exist - use setPos(x, y, z) instead
        armorStand.setPos(x, y, z);
        player.connection.send(new ClientboundAddEntityPacket(
                armorStand.getId(),
                armorStand.getUUID(),
                x,
                y,
                z,
                0,
                0,
                EntityType.ARMOR_STAND,
                0,
                new Vec3(0, 0, 0),
                0));
        player.connection.send(new ClientboundSetEntityDataPacket(
                armorStand.getId(), armorStand.getEntityData().getNonDefaultValues()));
    }

    public void kill(ServerPlayer player) {
        player.connection.send(new ClientboundRemoveEntitiesPacket(armorStand.getId()));
    }

    public Vec3 getPosition() {
        return new Vec3(x, y, z);
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }
}
