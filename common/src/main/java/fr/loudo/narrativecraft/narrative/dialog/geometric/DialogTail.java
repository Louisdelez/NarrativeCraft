package fr.loudo.narrativecraft.narrative.dialog.geometric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.dialog.DialogRenderer3D;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class DialogTail {

    private final DialogRenderer3D dialogRenderer;
    private float width, height, offset;

    public DialogTail(DialogRenderer3D dialogRenderer, float width, float height, float offset) {
        this.dialogRenderer = dialogRenderer;
        this.width = width;
        this.height = height;
        this.offset = offset;
    }

    public void render(PoseStack poseStack, float partialTick, MultiBufferSource.BufferSource bufferSource, Camera camera) {
        Direction direction = getDirection(camera);
        Vec2 tailOffset = getTailOffset(camera);
        poseStack.pushPose();
        float tailOffsetX = 0;
        float tailOffsetY = 0;
        
        if(direction == Direction.TOP || direction == Direction.BOTTOM) {
            tailOffsetX = Math.clamp(tailOffset.x, -dialogRenderer.getWidth() + width / 2, dialogRenderer.getWidth() - width / 2);
        }

        if(direction == Direction.RIGHT) {
            if (dialogRenderer.isAnimating()) {
                tailOffsetX = dialogRenderer.getInterpolatedWidth(partialTick);
            } else {
                tailOffsetX = dialogRenderer.getWidth();
            }
        }

        if(direction == Direction.LEFT) {
            if (dialogRenderer.isAnimating()) {
                tailOffsetX = -dialogRenderer.getInterpolatedWidth(partialTick);
            } else {
                tailOffsetX = -dialogRenderer.getWidth();
            }
        }

        if(direction == Direction.RIGHT || direction == Direction.LEFT) {
            tailOffsetY = Math.clamp(tailOffset.y, -dialogRenderer.getHeight() + width / 2, -width / 2);
            if(tailOffset.y < -dialogRenderer.getHeight() + width / 2) {
                direction = Direction.valueOf(direction.name() + "_UP_CORNER");
            } else if(tailOffset.y > dialogRenderer.getHeight() + width / 2) {
                direction = Direction.valueOf(direction.name() + "_DOWN_CORNER");
            }
        }

        poseStack.translate(tailOffsetX, tailOffsetY, 0);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(NarrativeCraftMod.dialogBackgroundRenderType);
        Matrix4f matrix4f = poseStack.last().pose();

        float topRight = -width / 2 + offset;
        float topLeft = width / 2 + offset;

        switch (direction) {
            case TOP -> drawTailTop(matrix4f, vertexConsumer, topRight, topLeft);
            case BOTTOM -> drawTailBottom(matrix4f, vertexConsumer, topRight, topLeft);
            case RIGHT -> drawTailRight(matrix4f, vertexConsumer);
            case RIGHT_UP_CORNER -> drawTailUpRightCorner(matrix4f, vertexConsumer);
            case RIGHT_DOWN_CORNER -> drawTailDownRightCorner(matrix4f, vertexConsumer);
            case LEFT -> drawTailLeft(matrix4f, vertexConsumer);
            case LEFT_UP_CORNER -> drawTailUpLeftCorner(matrix4f, vertexConsumer);
            case LEFT_DOWN_CORNER -> drawTailDownLeftCorner(matrix4f, vertexConsumer);
        }

        bufferSource.endBatch(NarrativeCraftMod.dialogBackgroundRenderType);
        poseStack.popPose();

    }

    private Vec2 getTailOffset(Camera camera) {
        Vec3 entityPos = dialogRenderer.translateToRelative(dialogRenderer.getDialogPosition());
        Vec3 dialogPos = dialogRenderer.translateToRelativeApplyOffset(dialogRenderer.getDialogPosition());
        Vec3 toDialog = dialogPos.subtract(entityPos);

        Vector3f leftVec = camera.getLeftVector();
        Vec3 camRight = new Vec3(leftVec.x(), leftVec.y(), leftVec.z());
        Vector3f upVec = camera.getUpVector();
        Vec3 camUp = new Vec3(upVec.x(), upVec.y(), upVec.z());

        return new Vec2((float) (toDialog.dot(camRight) / dialogRenderer.getScale()), (float) (toDialog.dot(camUp) / dialogRenderer.getScale()));
    }

    private Direction getDirection(Camera camera) {
        Vec3 entityPos = dialogRenderer.translateToRelative(dialogRenderer.getDialogPosition());
        Vec3 dialogPos = dialogRenderer.translateToRelativeApplyOffset(dialogRenderer.getDialogPosition());
        Vec3 delta = entityPos.subtract(dialogPos);

        Vector3f camUp = camera.getUpVector();
        Vector3f camLeft = camera.getLeftVector();

        Vec3 up = new Vec3(camUp.x(), camUp.y(), camUp.z());
        Vec3 left = new Vec3(camLeft.x(), camLeft.y(), camLeft.z());

        double verticalProjection = delta.dot(up);
        double horizontalProjection = delta.dot(left);

        if (Math.abs(verticalProjection) > Math.abs(horizontalProjection)) {
            return verticalProjection > 0 ? Direction.TOP : Direction.BOTTOM;
        } else {
            return horizontalProjection > 0 ? Direction.LEFT : Direction.RIGHT;
        }
    }

    enum Direction {
        TOP, BOTTOM, LEFT, LEFT_UP_CORNER, LEFT_DOWN_CORNER, RIGHT, RIGHT_UP_CORNER, RIGHT_DOWN_CORNER
    }

    void drawTailTop(Matrix4f matrix4f, VertexConsumer vertexConsumer, float topRight, float topLeft) {
        vertexConsumer.addVertex(matrix4f, 0, -dialogRenderer.getHeight() - height, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -topRight, -dialogRenderer.getHeight(), 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -topLeft, -dialogRenderer.getHeight(), 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -topRight, -dialogRenderer.getHeight(), 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailBottom(Matrix4f matrix4f, VertexConsumer vertexConsumer, float topRight, float topLeft) {
        vertexConsumer.addVertex(matrix4f, -topRight, 0, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -topLeft, 0, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, height, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -topRight, 0, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailLeft(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, -height, 0, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, -width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, -width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailRight(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, height, 0, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, -width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailUpRightCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, height / 2, -4, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -width,  -width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, height / 2, -4, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailDownRightCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, height / 2, 4, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0,  -width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -width, width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, height / 2, 4, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailUpLeftCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, -height / 2, -4, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0,  width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, width, -width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -height / 2, -4, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailDownLeftCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, -height / 2, 4, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, width,  width / 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, width, -width * 2, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -height / 2, 4, 0).setColor(dialogRenderer.getBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }
}
