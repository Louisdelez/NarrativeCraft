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
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;
import fr.loudo.narrativecraft.narrative.recording.Location;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class AreaTrigger extends SceneData {

    private double x1, y1, z1;
    private double x2, y2, z2;
    private String stitch;

    public AreaTrigger(String name, String description, Scene scene, String stitch) {
        super(name, description, scene);
        this.stitch = stitch;
    }

    public void setLocation1(Location location) {
        this.x1 = (int) location.x() - 0.5;
        this.y1 = (int) location.y() - 0.5;
        this.z1 = (int) location.z() - 0.5;
    }

    public void setLocation1(Vec3 vec3) {
        this.x1 = (int) vec3.x();
        this.y1 = (int) vec3.y();
        this.z1 = (int) vec3.z();
        if (x1 < x2 || x2 == x1) {
            x1 -= 1.0;
        }
        if (z1 < z2 || z2 == z1) {
            z1 -= 1.0;
        }
        if (x2 == 0 && y2 == 0 && z2 == 0) {
            setLocation2(new Vec3(x1 + 1.0, y1, z1 + 1.0));
        }
    }

    public void setLocation2(Location location) {
        this.x2 = (int) location.x() - 0.5;
        this.y2 = (int) location.y() - 0.5;
        this.z2 = (int) location.z() - 0.5;
    }

    public void setLocation2(Vec3 vec3) {
        this.x2 = (int) vec3.x();
        this.y2 = (int) vec3.y();
        this.z2 = (int) vec3.z();
        if (x2 < x1 || x2 == x1) {
            x2 -= 1.0;
        }
        if (z2 < z1 || z2 == z1) {
            z2 -= 1.0;
        }
    }

    public Vec3 getLocation1() {
        return new Vec3(x1, y1, z1);
    }

    public Vec3 getLocation2() {
        return new Vec3(x2, y2, z2);
    }

    public String getStitch() {
        return stitch;
    }

    public void setStitch(String stitch) {
        this.stitch = stitch;
    }

    public void drawSquareLine(PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        PoseStack.Pose matrix4f = poseStack.last();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertex = bufferSource.getBuffer(RenderType.lines());

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
        float r = 1.0F, g = 1.0F, b = 0.0F, a = 1.0F;

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

        bufferSource.endBatch(RenderType.lines());
    }

    private void drawEdge(
            VertexConsumer vertex, Matrix4f matrix, Vec3 a, Vec3 b, float r, float g, float bcol, float alpha) {
        vertex.addVertex(matrix, (float) a.x, (float) a.y, (float) a.z)
                .setColor(r, g, bcol, alpha)
                .setNormal(0, 1, 0);
        vertex.addVertex(matrix, (float) b.x, (float) b.y, (float) b.z)
                .setColor(r, g, bcol, alpha)
                .setNormal(0, 1, 0);
    }
}
