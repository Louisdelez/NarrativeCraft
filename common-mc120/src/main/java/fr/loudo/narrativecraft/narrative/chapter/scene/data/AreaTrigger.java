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

package fr.loudo.narrativecraft.narrative.chapter.scene.data;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.compat.api.RenderChannel;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * MC 1.20.x version of AreaTrigger.
 * Key differences:
 * - Camera: getEntity().position() instead of entity().position()
 * - VertexConsumer: .vertex().color().normal().endVertex() instead of .addVertex().setColor().setNormal()
 */
public class AreaTrigger extends SceneData {

    private double x1, y1, z1;
    private double x2, y2, z2;
    private boolean isUnique;
    private String stitch;

    public AreaTrigger(String name, String description, Scene scene, String stitch, boolean isUnique) {
        super(name, description, scene);
        this.stitch = stitch;
        this.isUnique = isUnique;
    }

    public void setPosition1(Vec3 vec3) {
        this.x1 = Math.floor(vec3.x());
        this.y1 = Math.floor(vec3.y());
        this.z1 = Math.floor(vec3.z());
        if (x2 == 0 && y2 == 0 && z2 == 0) {
            this.x2 = Math.floor(vec3.x());
            this.y2 = Math.floor(vec3.y());
            this.z2 = Math.floor(vec3.z());
        }
    }

    public void setPosition2(Vec3 vec3) {
        this.x2 = Math.floor(vec3.x());
        this.y2 = Math.floor(vec3.y());
        this.z2 = Math.floor(vec3.z());
    }

    public AABB getBoundingBox() {
        double minX = Math.min(x1, x2);
        double minY = Math.min(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxX = Math.max(x1, x2) + 1.0;
        double maxY = Math.max(y1, y2) + 1.0;
        double maxZ = Math.max(z1, z2) + 1.0;

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public Vec3 getPosition1() {
        return new Vec3(x1, y1, z1);
    }

    public Vec3 getPosition2() {
        return new Vec3(x2, y2, z2);
    }

    public String getStitch() {
        return stitch;
    }

    public void setStitch(String stitch) {
        this.stitch = stitch;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }

    public void drawSquareLine(PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        // 1.20.x: getEntity().position() instead of entity().position()
        Vec3 camPos = camera.getEntity().position();

        PoseStack.Pose matrix4f = poseStack.last();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertex = bufferSource.getBuffer(NarrativeCraftMod.getRenderType(RenderChannel.DEBUG_LINES));

        double minX = Math.min(x1, x2);
        double minY = Math.min(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxX = Math.max(x1, x2);
        double maxY = Math.max(y1, y2) + 1.0;
        double maxZ = Math.max(z1, z2);

        minX -= camPos.x;
        minY -= camPos.y;
        minZ -= camPos.z;
        maxX -= camPos.x;
        maxY -= camPos.y;
        maxZ -= camPos.z;

        Matrix4f matrix = matrix4f.pose();
        float r = 0.09F, g = 0.38F, b = 0.85F, a = 1.0F;

        Vec3 p000 = new Vec3(minX, minY, minZ);
        Vec3 p001 = new Vec3(minX, minY, maxZ);
        Vec3 p010 = new Vec3(minX, maxY, minZ);
        Vec3 p011 = new Vec3(minX, maxY, maxZ);
        Vec3 p100 = new Vec3(maxX, minY, minZ);
        Vec3 p101 = new Vec3(maxX, minY, maxZ);
        Vec3 p110 = new Vec3(maxX, maxY, minZ);
        Vec3 p111 = new Vec3(maxX, maxY, maxZ);

        drawEdge(vertex, matrix, p000, p100, r, g, b, a);
        drawEdge(vertex, matrix, p100, p101, r, g, b, a);
        drawEdge(vertex, matrix, p101, p001, r, g, b, a);
        drawEdge(vertex, matrix, p001, p000, r, g, b, a);

        drawEdge(vertex, matrix, p010, p110, r, g, b, a);
        drawEdge(vertex, matrix, p110, p111, r, g, b, a);
        drawEdge(vertex, matrix, p111, p011, r, g, b, a);
        drawEdge(vertex, matrix, p011, p010, r, g, b, a);

        drawEdge(vertex, matrix, p000, p010, r, g, b, a);
        drawEdge(vertex, matrix, p100, p110, r, g, b, a);
        drawEdge(vertex, matrix, p101, p111, r, g, b, a);
        drawEdge(vertex, matrix, p001, p011, r, g, b, a);

        bufferSource.endBatch(NarrativeCraftMod.getRenderType(RenderChannel.DEBUG_LINES));
    }

    /**
     * 1.20.x: Use .vertex().color().normal().endVertex() chain instead of .addVertex().setColor().setNormal()
     */
    private void drawEdge(
            VertexConsumer vertex, Matrix4f matrix, Vec3 a, Vec3 b, float r, float g, float bcol, float alpha) {
        vertex.vertex(matrix, (float) a.x, (float) a.y, (float) a.z)
                .color(r, g, bcol, alpha)
                .normal(0, 1, 0)
                .endVertex();
        vertex.vertex(matrix, (float) b.x, (float) b.y, (float) b.z)
                .color(r, g, bcol, alpha)
                .normal(0, 1, 0)
                .endVertex();
    }

    public static boolean isInside(AreaTrigger areaTrigger, Vec3 pPosition) {
        Vec3 pos1 = areaTrigger.getPosition1();
        Vec3 pos2 = areaTrigger.getPosition2();
        double minX = Math.min(pos1.x, pos2.x);
        double maxX = Math.max(pos1.x, pos2.x);
        double minY = Math.min(pos1.y, pos2.y);
        double maxY = Math.max(pos1.y, pos2.y);
        double minZ = Math.min(pos1.z, pos2.z);
        double maxZ = Math.max(pos1.z, pos2.z);

        return pPosition.x >= minX
                && pPosition.x <= maxX
                && pPosition.y >= minY
                && pPosition.y <= maxY
                && pPosition.z >= minZ
                && pPosition.z <= maxZ;
    }
}
