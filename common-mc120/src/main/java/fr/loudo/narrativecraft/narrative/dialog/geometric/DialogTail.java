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

package fr.loudo.narrativecraft.narrative.dialog.geometric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.compat.api.RenderChannel;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer3D;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * MC 1.20.x version of DialogTail.
 * Key differences from 1.21.x:
 * - Camera methods: getLeftVector()/getUpVector() instead of leftVector()/upVector()
 * - VertexConsumer API: Uses .vertex().color().uv().overlayCoords().uv2().normal().endVertex()
 *   instead of .addVertex().setColor().setLight()
 */
public class DialogTail {

    private final DialogRenderer3D dialog;
    private float width, height, offset;

    public DialogTail(DialogRenderer3D dialog, float width, float height, float offset) {
        this.dialog = dialog;
        this.width = width;
        this.height = height;
        this.offset = offset;
    }

    public void render(
            PoseStack poseStack, float partialTick, MultiBufferSource.BufferSource bufferSource, Camera camera) {

        TailDirection tailDirection = TailDirection.TOP;
        if (dialog.getDialogOffset().x > 0) {
            tailDirection = TailDirection.LEFT;
        } else if (dialog.getDialogOffset().x < 0) {
            tailDirection = TailDirection.RIGHT;
        } else if (dialog.getDialogOffset().y > 0) {
            tailDirection = TailDirection.BOTTOM;
        }

        Vec2 tailOffset = getTailOffset(camera);

        poseStack.pushPose();

        if (dialog.getDialogOffset().x != 0) {
            if (dialog.getDialogOffset().y < 0) {
                poseStack.translate(0, (height / 2.0F) - width / 2.0F, 0);
            } else if (dialog.getDialogOffset().y > 0) {
                poseStack.translate(0, -(height / 2.0F) + width / 2.0F, 0);
            }
        }

        if (tailDirection == TailDirection.RIGHT || tailDirection == TailDirection.LEFT) {
            float halfHeight = dialog.getHeight() / 2f;
            float minY = -halfHeight + width / 2f;
            float maxY = halfHeight - width / 2f;

            if (dialog.getDialogOffset().y != 0) {
                if (tailOffset.y < minY) {
                    tailDirection = TailDirection.valueOf(tailDirection.name() + "_UP_CORNER");
                } else if (tailOffset.y > maxY) {
                    tailDirection = TailDirection.valueOf(tailDirection.name() + "_DOWN_CORNER");
                }
            }
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(NarrativeCraftMod.getRenderType(RenderChannel.DIALOG_BACKGROUND));
        Matrix4f matrix4f = poseStack.last().pose();

        float topRight = -width / 2 + offset;
        float topLeft = width / 2 + offset;

        switch (tailDirection) {
            case TOP -> drawTailTop(matrix4f, vertexConsumer, topRight, topLeft);
            case BOTTOM -> drawTailBottom(matrix4f, vertexConsumer, topRight, topLeft);
            case RIGHT -> drawTailRight(matrix4f, vertexConsumer);
            case RIGHT_UP_CORNER -> drawTailUpRightCorner(matrix4f, vertexConsumer);
            case RIGHT_DOWN_CORNER -> drawTailDownRightCorner(matrix4f, vertexConsumer);
            case LEFT -> drawTailLeft(matrix4f, vertexConsumer);
            case LEFT_UP_CORNER -> drawTailUpLeftCorner(matrix4f, vertexConsumer);
            case LEFT_DOWN_CORNER -> drawTailDownLeftCorner(matrix4f, vertexConsumer);
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }

    public Vec2 getTailOffset(Camera camera) {
        Vec3 entityPos = dialog.translateToRelative(dialog.getDialogPosition());
        Vec3 dialogPos = dialog.translateToRelativeApplyOffset(dialog.getDialogPosition());
        Vec3 toDialog = dialogPos.subtract(entityPos);

        // 1.20.x API: getLeftVector()/getUpVector() instead of leftVector()/upVector()
        Vector3f leftVec = camera.getLeftVector();
        Vector3f upVec = camera.getUpVector();
        Vec3 camRight = new Vec3(leftVec.x(), leftVec.y(), leftVec.z()).scale(-1);
        Vec3 camUp = new Vec3(upVec.x(), upVec.y(), upVec.z());

        float scale = dialog.getScale() * 0.025F;

        return new Vec2((float) (toDialog.dot(camRight) / scale), (float) (toDialog.dot(camUp) / scale));
    }

    enum TailDirection {
        TOP,
        BOTTOM,
        LEFT,
        LEFT_UP_CORNER,
        LEFT_DOWN_CORNER,
        RIGHT,
        RIGHT_UP_CORNER,
        RIGHT_DOWN_CORNER
    }

    /**
     * Helper method for 1.20.x VertexConsumer API.
     * 1.20.x uses: .vertex().color().uv().overlayCoords().uv2().normal().endVertex()
     * 1.21.x uses: .addVertex().setColor().setLight()
     */
    private void addColoredVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int color, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(color)
                .uv(0, 0)
                .overlayCoords(0, 10)
                .uv2(light)
                .normal(0, 0, 1)
                .endVertex();
    }

    void drawTailTop(Matrix4f matrix4f, VertexConsumer vertexConsumer, float topRight, float topLeft) {
        int color = dialog.getBackgroundColor();
        int light = LightTexture.FULL_BRIGHT;
        addColoredVertex(vertexConsumer, matrix4f, 0, -dialog.getHeight() - height, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, -topRight, -dialog.getHeight(), 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, -topLeft, -dialog.getHeight(), 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, -topRight, -dialog.getHeight(), 0, color, light);
    }

    void drawTailBottom(Matrix4f matrix4f, VertexConsumer vertexConsumer, float topRight, float topLeft) {
        int color = dialog.getBackgroundColor();
        int light = LightTexture.FULL_BRIGHT;
        addColoredVertex(vertexConsumer, matrix4f, -topRight, 0, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, -topLeft, 0, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, 0, height, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, -topRight, 0, 0, color, light);
    }

    void drawTailLeft(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        int color = dialog.getBackgroundColor();
        int light = LightTexture.FULL_BRIGHT;
        addColoredVertex(vertexConsumer, matrix4f, -height, 0, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, 0, -width / 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, 0, width / 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, 0, -width / 2, 0, color, light);
    }

    void drawTailRight(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        int color = dialog.getBackgroundColor();
        int light = LightTexture.FULL_BRIGHT;
        addColoredVertex(vertexConsumer, matrix4f, height, 0, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, 0, width / 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, 0, -width / 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, 0, width / 2, 0, color, light);
    }

    void drawTailUpRightCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        int color = dialog.getBackgroundColor();
        int light = LightTexture.FULL_BRIGHT;
        addColoredVertex(vertexConsumer, matrix4f, height / 2, -4, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, -width, -width / 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, 0, width / 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, height / 2, -4, 0, color, light);
    }

    void drawTailDownRightCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        int color = dialog.getBackgroundColor();
        int light = LightTexture.FULL_BRIGHT;
        addColoredVertex(vertexConsumer, matrix4f, height / 2, 4, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, 0, -width / 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, -width, width / 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, height / 2, 4, 0, color, light);
    }

    void drawTailUpLeftCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        int color = dialog.getBackgroundColor();
        int light = LightTexture.FULL_BRIGHT;
        addColoredVertex(vertexConsumer, matrix4f, -height / 2, -4, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, 0, width / 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, width, -width / 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, -height / 2, -4, 0, color, light);
    }

    void drawTailDownLeftCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        int color = dialog.getBackgroundColor();
        int light = LightTexture.FULL_BRIGHT;
        addColoredVertex(vertexConsumer, matrix4f, -height / 2, 4, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, width, width / 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, width, -width * 2, 0, color, light);
        addColoredVertex(vertexConsumer, matrix4f, -height / 2, 4, 0, color, light);
    }
}
