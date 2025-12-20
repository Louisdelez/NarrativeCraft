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

package fr.loudo.narrativecraft.narrative.dialog;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.client.NarrativeCraftModClient;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.dialog.geometric.DialogTail;
import fr.loudo.narrativecraft.util.Easing;
import fr.loudo.narrativecraft.util.Position2D;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class DialogRenderer3D extends DialogRenderer {

    private final DialogTail dialogTail;
    private final String characterName;
    private DialogEntityBobbing dialogEntityBobbing;
    private Position2D dialogOffset;
    private CharacterRuntime characterRuntime;
    private Vec3 dialogPosition;

    public DialogRenderer3D(
            String text,
            String characterName,
            CharacterRuntime characterRuntime,
            Position2D dialogOffset,
            float width,
            float paddingX,
            float paddingY,
            float scale,
            float letterSpacing,
            float gap,
            int backgroundColor,
            int textColor) {
        super(text, width, paddingX, paddingY, scale, letterSpacing, gap, backgroundColor, textColor);
        this.characterName = characterName;
        this.characterRuntime = characterRuntime;
        this.dialogOffset = dialogOffset;
        dialogTail = new DialogTail(this, 5, 10, 0);
    }

    public DialogRenderer3D(
            String text, String characterName, DialogData dialogData, CharacterRuntime characterRuntime) {
        super(text, dialogData);
        this.characterName = characterName;
        this.characterRuntime = characterRuntime;
        this.dialogOffset = dialogData.getOffset();
        dialogTail = new DialogTail(this, 5, 10, 0);
    }

    @Override
    public void tick() {
        if (dialogPosition == null) return;
        dialogEntityBobbing.tick();
        super.tick();
    }

    private void updateDialogPosition(float partialTick) {
        Entity serverEntity = characterRuntime.getEntity();
        if (serverEntity != null) {
            Entity entity =
                    minecraft.level.getEntity(characterRuntime.getEntity().getId());
            if (entity != null) {
                if (dialogPosition != null) {
                    dialogPosition = Mth.lerp(
                            partialTick, dialogPosition, entity.position().add(0, entity.getEyeHeight(), 0));
                } else {
                    dialogPosition = entity.position().add(0, entity.getEyeHeight(), 0);
                }
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, float partialTick) {
        updateDialogPosition(partialTick);
        if (dialogPosition == null) return;

        Vec3 position = dialogPosition;
        position = translateToRelativeApplyOffset(position);
        float originalScale = scale;
        if (currentTick < totalTick) {
            double t = t(partialTick);
            t = Easing.SMOOTH.interpolate(t);
            if (dialogStarting || dialogStopping) {
                double opacity;
                if (dialogStarting) {
                    originalScale = (float) Mth.lerp(t, 0.0, scale);
                    opacity = Mth.lerp(t, 0.0, 1.0);
                    position = getDialogInterpolatedAppearPosition(t);
                } else {
                    originalScale = (float) Mth.lerp(t, scale, 0);
                    opacity = Mth.lerp(t, 1.0, 0.0);
                    position = getDialogInterpolatedDisappearPosition(t);
                }
                backgroundColor = ARGB.color((int) (opacity * 255.0), backgroundColor);
            } else {
                originalScale = (float) Mth.lerp(t, oldScale, scale);
            }
        }
        if (currentTick == totalTick) {
            if (dialogStopping && !dialogStarting) dialogStopping = false;
            if (dialogStarting && !dialogStopping) dialogStarting = false;
        }
        poseStack.translate(position.x, position.y, position.z);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().camera.rotation());
        poseStack.scale(originalScale * 0.025F, -originalScale * 0.025F, originalScale * 0.025F);

        renderDialogBackground(poseStack, partialTick);

        Side side = dialogOffsetSide();
        poseStack.pushPose();

        if (side == Side.DOWN) {
            poseStack.translate(0, dialogOffset.x == 0 ? height : height / 2.0F + paddingY / 2.0F, 0);
        }

        dialogTail.render(
                poseStack,
                partialTick,
                minecraft.renderBuffers().bufferSource(),
                minecraft.gameRenderer.getMainCamera());

        poseStack.popPose();

        if (dialogOffset.y == 0) {
            switch (side) {
                case RIGHT, LEFT -> poseStack.translate(0, 0, 0);
            }
        }

        poseStack.pushPose();
        if (side == Side.RIGHT) {
            if (dialogOffset.y == 0) {
                poseStack.translate(totalWidth, totalHeight / 2.0F, 0);
            } else if (dialogOffset.y > 0) {
                poseStack.translate(totalWidth, 0, 0);
            } else if (dialogOffset.y < 0) {
                poseStack.translate(totalWidth, totalHeight, 0);
            }
        } else if (side == Side.LEFT) {
            if (dialogOffset.y == 0) {
                poseStack.translate(-totalWidth, totalHeight / 2.0F, 0);
            } else if (dialogOffset.y > 0) {
                poseStack.translate(-totalWidth, 0, 0);
            } else if (dialogOffset.y < 0) {
                poseStack.translate(-totalWidth, totalHeight, 0);
            }
        } else if (side == Side.DOWN) {
            poseStack.translate(0, totalHeight, 0);
        }

        if (!dialogStopping) {
            dialogEntityBobbing.partialTick(partialTick);
            dialogScrollTextDialog.render(poseStack, minecraft.renderBuffers().bufferSource(), partialTick);
            if (dialogScrollTextDialog.isFinished()) {
                if (!dialogAutoSkipping) {
                    dialogAutoSkipping = true;
                    currentTick = 0;
                }
                dialogArrowSkip.start();
            }
            if (!noSkip) {
                dialogArrowSkip.render(poseStack, minecraft.renderBuffers().bufferSource(), partialTick);
            }
        }

        poseStack.popPose();
        minecraft
                .renderBuffers()
                .bufferSource()
                .endBatch(NarrativeCraftModClient.getInstance().dialogBackgroundRenderType());
    }

    public void updateBobbing(float value1, float value2) {
        dialogEntityBobbing.setNoiseShakeSpeed(value1);
        dialogEntityBobbing.setNoiseShakeStrength(value2);
    }

    private Vec3 getDialogInterpolatedAppearPosition(double t) {
        double x, y, z;
        Vec3 dialogPos = translateToRelative(dialogPosition);
        Vec3 dialogPositionOffsetApplied = translateToRelativeApplyOffset(dialogPosition);
        x = Mth.lerp(t, dialogPos.x, dialogPositionOffsetApplied.x);
        y = Mth.lerp(t, dialogPos.y, dialogPositionOffsetApplied.y);
        z = Mth.lerp(t, dialogPos.z, dialogPositionOffsetApplied.z);
        return new Vec3(x, y, z);
    }

    private Vec3 getDialogInterpolatedDisappearPosition(double t) {
        double x, y, z;
        Vec3 dialogPos = translateToRelative(dialogPosition);
        Vec3 dialogPositionOffsetApplied = translateToRelativeApplyOffset(dialogPosition);
        x = Mth.lerp(t, dialogPositionOffsetApplied.x, dialogPos.x);
        y = Mth.lerp(t, dialogPositionOffsetApplied.y, dialogPos.y);
        z = Mth.lerp(t, dialogPositionOffsetApplied.z, dialogPos.z);
        return new Vec3(x, y, z);
    }

    private void renderDialogBackground(PoseStack poseStack, float partialTick) {
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer =
                bufferSource.getBuffer(NarrativeCraftModClient.getInstance().dialogBackgroundRenderType());
        Matrix4f matrix4f = poseStack.last().pose();

        Side side = dialogOffsetSide();
        double diffY = translateToRelativeApplyOffset(dialogPosition).y - translateToRelative(dialogPosition).y;

        float originalWidth = totalWidth;
        float originalHeight = totalHeight;
        if (isAnimating() && !dialogStarting && !dialogStopping) {
            originalWidth = getInterpolatedWidth(partialTick);
            originalHeight = getInterpolatedHeight(partialTick);
        } else {
            oldTotalWidth = totalWidth;
            oldWidth = width;
            oldHeight = height;
            oldTotalHeight = totalHeight;
            oldScale = scale;
        }

        switch (side) {
            case UP -> {
                vertexConsumer
                        .addVertex(matrix4f, -originalWidth, 0, 0)
                        .setColor(backgroundColor)
                        .setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer
                        .addVertex(matrix4f, originalWidth, 0, 0)
                        .setColor(backgroundColor)
                        .setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer
                        .addVertex(matrix4f, originalWidth, -originalHeight, 0)
                        .setColor(backgroundColor)
                        .setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer
                        .addVertex(matrix4f, -originalWidth, -originalHeight, 0)
                        .setColor(backgroundColor)
                        .setLight(LightTexture.FULL_BRIGHT);
            }
            case RIGHT -> {
                if (diffY < 0) {
                    vertexConsumer
                            .addVertex(matrix4f, 0, 0, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, 0, originalHeight, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, originalWidth * 2, originalHeight, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, originalWidth * 2, 0, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                } else if (diffY > 0) {
                    vertexConsumer
                            .addVertex(matrix4f, 0, -originalHeight, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, 0, 0, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, originalWidth * 2, 0, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, originalWidth * 2, -originalHeight, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                } else {
                    vertexConsumer
                            .addVertex(matrix4f, 0, -originalHeight / 2, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, 0, originalHeight / 2, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, originalWidth * 2, originalHeight / 2, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, originalWidth * 2, -originalHeight / 2, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                }
            }
            case LEFT -> {
                if (diffY < 0) {
                    vertexConsumer
                            .addVertex(matrix4f, -originalWidth * 2, 0, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, -originalWidth * 2, originalHeight, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, 0, originalHeight, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, 0, 0, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                } else if (diffY > 0) {
                    vertexConsumer
                            .addVertex(matrix4f, 0, 0, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, 0, -originalHeight, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, -originalWidth * 2, -originalHeight, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, -originalWidth * 2, 0, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                } else {
                    vertexConsumer
                            .addVertex(matrix4f, 0, originalHeight / 2, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, 0, -originalHeight / 2, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, -originalWidth * 2, -originalHeight / 2, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer
                            .addVertex(matrix4f, -originalWidth * 2, originalHeight / 2, 0)
                            .setColor(backgroundColor)
                            .setLight(LightTexture.FULL_BRIGHT);
                }
            }
            case DOWN -> {
                vertexConsumer
                        .addVertex(matrix4f, -originalWidth, originalHeight, 0)
                        .setColor(backgroundColor)
                        .setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer
                        .addVertex(matrix4f, originalWidth, originalHeight, 0)
                        .setColor(backgroundColor)
                        .setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer
                        .addVertex(matrix4f, originalWidth, 0, 0)
                        .setColor(backgroundColor)
                        .setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer
                        .addVertex(matrix4f, -originalWidth, 0, 0)
                        .setColor(backgroundColor)
                        .setLight(LightTexture.FULL_BRIGHT);
            }
        }
    }

    private Side dialogOffsetSide() {
        Vec3 dialogPos = translateToRelative(dialogPosition);
        Vec3 dialogPosOffset = translateToRelativeApplyOffset(dialogPosition);

        double offsetX = 0;
        double offsetY = dialogPosOffset.y - dialogPos.y;

        Direction direction = minecraft.player.getDirection();

        if (direction == Direction.EAST) {
            offsetX = dialogPosOffset.z - dialogPos.z;
        } else if (direction == Direction.WEST) {
            offsetX = dialogPos.z - dialogPosOffset.z;
        } else if (direction == Direction.NORTH) {
            offsetX = dialogPosOffset.x - dialogPos.x;
        } else if (direction == Direction.SOUTH) {
            offsetX = dialogPos.x - dialogPosOffset.x;
        }

        if (offsetY >= 0 && offsetX >= 0 && offsetX <= 0) {
            return Side.UP;
        } else if (offsetY <= 0 && offsetX >= 0 && offsetX <= 0) {
            return Side.DOWN;
        } else if (offsetX <= 0) {
            return Side.LEFT;
        } else if (offsetX >= 0) {
            return Side.RIGHT;
        }
        return Side.UP;
    }

    public Vec3 translateToRelative(Vec3 worldPos) {
        Vec3 camPos = minecraft.gameRenderer.getMainCamera().position();
        return new Vec3(worldPos.x - camPos.x, worldPos.y - camPos.y, worldPos.z - camPos.z);
    }

    public Vec3 translateToRelativeApplyOffset(Vec3 position) {
        position = translateToRelative(position);
        double offsetX = 0;
        double offsetZ = 0;
        switch (minecraft.player.getDirection()) {
            case EAST -> offsetZ = dialogOffset.x;
            case WEST -> offsetZ = -dialogOffset.x;
            case SOUTH -> offsetX = -dialogOffset.x;
            case NORTH -> offsetX = dialogOffset.x;
        }
        return new Vec3(position.x + offsetX, position.y + dialogOffset.y, position.z + offsetZ);
    }

    public enum Side {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public CharacterRuntime getCharacterRuntime() {
        return characterRuntime;
    }

    public void setCharacterRuntime(CharacterRuntime characterRuntime) {
        this.characterRuntime = characterRuntime;
    }

    public Position2D getDialogOffset() {
        return dialogOffset;
    }

    public void setDialogOffset(Position2D dialogOffset) {
        this.dialogOffset = dialogOffset;
    }

    public Vec3 getDialogPosition() {
        return dialogPosition;
    }

    public Vec3 getDialogPositionWithOffset() {
        return dialogPosition.add(dialogOffset.x, dialogPosition.y, dialogPosition.z);
    }

    public String getCharacterName() {
        return characterName;
    }

    public DialogEntityBobbing getDialogEntityBobbing() {
        return dialogEntityBobbing;
    }

    public void setDialogEntityBobbing(DialogEntityBobbing dialogEntityBobbing) {
        this.dialogEntityBobbing = dialogEntityBobbing;
    }
}
