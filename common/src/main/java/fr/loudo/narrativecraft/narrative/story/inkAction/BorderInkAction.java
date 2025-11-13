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

package fr.loudo.narrativecraft.narrative.story.inkAction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Easing;
import fr.loudo.narrativecraft.util.Translation;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class BorderInkAction extends InkAction {

    private float up, right, down, left;
    private float upInterpolated, rightInterpolated, downInterpolated, leftInterpolated;
    private int color;
    private double opacity;
    private Easing easing;
    private String fadeAction;

    public BorderInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    public void tick() {
        if (tick == totalTick && fadeAction.equals("out")) {
            isRunning = false;
        }
        if (tick < totalTick) {
            tick++;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        if (!isRunning) return;
        Minecraft minecraft = Minecraft.getInstance();
        int widthScreen = minecraft.getWindow().getGuiScaledWidth();
        int heightScreen = minecraft.getWindow().getGuiScaledHeight();
        int guiScale = minecraft.options.guiScale().get();
        if (minecraft.options.guiScale().get() == 0) {
            guiScale = 3;
        }

        float up = this.up;
        float right = this.right;
        float down = this.down;
        float left = this.left;

        if (fadeAction.equals("out") && tick == totalTick) {
            up = 0;
            right = 0;
            down = 0;
            left = 0;
        }

        if (tick < totalTick) {
            double t = Mth.clamp((tick + partialTick) / totalTick, 0.0, 1.0);
            t = easing.interpolate(t);
            if (fadeAction.equals("in")) {
                up = (float) Mth.lerp(t, 0, this.up);
                right = (float) Mth.lerp(t, 0, this.right);
                down = (float) Mth.lerp(t, 0, this.down);
                left = (float) Mth.lerp(t, 0, this.left);
            } else if (fadeAction.equals("out")) {
                up = (float) Mth.lerp(t, this.up, 0);
                right = (float) Mth.lerp(t, this.right, 0);
                down = (float) Mth.lerp(t, this.down, 0);
                left = (float) Mth.lerp(t, this.left, 0);
            }
            upInterpolated = up;
            rightInterpolated = right;
            downInterpolated = down;
            leftInterpolated = left;
        }

        PoseStack poseStack = guiGraphics.pose();
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.gui());
        poseStack.pushPose();

        // UP
        vertexConsumer.vertex(matrix4f, 0, 0, 0).color(color).endVertex();
        vertexConsumer.vertex(matrix4f, 0, up / guiScale, 0).color(color).endVertex();
        vertexConsumer
                .vertex(matrix4f, widthScreen, up / guiScale, 0)
                .color(color)
                .endVertex();
        vertexConsumer.vertex(matrix4f, widthScreen, 0, 0).color(color).endVertex();

        // RIGHT
        vertexConsumer
                .vertex(matrix4f, widthScreen - right / guiScale, 0, 0)
                .color(color)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, widthScreen - right / guiScale, heightScreen, 0)
                .color(color)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, widthScreen, heightScreen, 0)
                .color(color)
                .endVertex();
        vertexConsumer.vertex(matrix4f, widthScreen, 0, 0).color(color).endVertex();

        // DOWN
        vertexConsumer
                .vertex(matrix4f, 0, heightScreen - down / guiScale, 0)
                .color(color)
                .endVertex();
        vertexConsumer.vertex(matrix4f, 0, heightScreen, 0).color(color).endVertex();
        vertexConsumer
                .vertex(matrix4f, widthScreen, heightScreen, 0)
                .color(color)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, widthScreen, heightScreen - down / guiScale, 0)
                .color(color)
                .endVertex();

        // LEFT
        vertexConsumer.vertex(matrix4f, 0, 0, 0).color(color).endVertex();
        vertexConsumer.vertex(matrix4f, 0, heightScreen, 0).color(color).endVertex();
        vertexConsumer
                .vertex(matrix4f, left / guiScale, heightScreen, 0)
                .color(color)
                .endVertex();
        vertexConsumer.vertex(matrix4f, left / guiScale, 0, 0).color(color).endVertex();

        poseStack.popPose();
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        fadeAction = "";
        easing = Easing.SMOOTH;
        if (arguments.size() == 1) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Up value missing"));
        }
        if (arguments.get(1).equals("clear")) {
            return InkActionResult.ok();
        }
        if (arguments.get(1).equals("out")) {
            fadeAction = "out";
            if (arguments.size() == 2) {
                return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Fade time value"));
            }
            try {
                totalTick = (int) (Double.parseDouble(arguments.get(2)) * 20.0);
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(2)));
            }
            easing = Easing.SMOOTH;
            if (arguments.size() > 3) {
                try {
                    easing = Easing.valueOf(arguments.get(3).toUpperCase());
                } catch (IllegalArgumentException e) {
                    return InkActionResult.error(
                            Translation.message(WRONG_EASING_VALUE, Arrays.toString(Easing.values())));
                }
            }
            return InkActionResult.ok();
        }
        if (arguments.size() == 2) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Right value missing"));
        }
        if (arguments.size() == 3) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Down value missing"));
        }
        if (arguments.size() == 4) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Left value missing"));
        }
        try {
            up = Integer.parseInt(arguments.get(1)) * 2;
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(1)));
        }
        try {
            right = Integer.parseInt(arguments.get(2)) * 2;
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(2)));
        }
        try {
            down = Integer.parseInt(arguments.get(3)) * 2;
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
        }
        try {
            left = Integer.parseInt(arguments.get(4)) * 2;
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(4)));
        }
        if (arguments.size() > 5) {
            try {
                color = Integer.parseInt(arguments.get(5), 16);
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_COLOR, arguments.get(5)));
            }
        }
        opacity = 1.0;
        if (arguments.size() > 6) {
            try {
                opacity = Double.parseDouble(arguments.get(6));
                if (opacity > 1) {
                    return InkActionResult.error(
                            Translation.message(WRONG_ARGUMENT_TEXT, "The opacity value is greater than 1"));
                } else if (opacity < 0) {
                    return InkActionResult.error(
                            Translation.message(WRONG_ARGUMENT_TEXT, "The opacity value is less than 0"));
                }
            } catch (NumberFormatException e) {
                return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(6)));
            }
        }
        color = FastColor.ABGR32.color((int) (opacity * 255), color);
        if (arguments.size() < 7) return InkActionResult.ok();
        fadeAction = arguments.get(7);
        if (!fadeAction.equals("in")) {
            return InkActionResult.error(Translation.message(WRONG_ARGUMENT_TEXT, "Action must be \"in\""));
        }
        if (arguments.size() == 8)
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Fade time value"));
        try {
            totalTick = (int) (Double.parseDouble(arguments.get(8)) * 20.0);
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(8)));
        }
        if (arguments.size() == 9) return InkActionResult.ok();
        try {
            easing = Easing.valueOf(arguments.get(9).toUpperCase());
        } catch (IllegalArgumentException e) {
            return InkActionResult.error(Translation.message(WRONG_EASING_VALUE, Arrays.toString(Easing.values())));
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        if (fadeAction.equals("out")) {
            for (InkAction inkAction : playerSession.getClientSideInkActions()) {
                if (inkAction instanceof BorderInkAction borderInkAction) {
                    borderInkAction.isRunning = false;
                    this.up = borderInkAction.upInterpolated;
                    this.right = borderInkAction.rightInterpolated;
                    this.down = borderInkAction.downInterpolated;
                    this.left = borderInkAction.leftInterpolated;
                    this.opacity = borderInkAction.opacity;
                    this.color = borderInkAction.color;
                    this.easing = borderInkAction.easing;
                }
            }
        }
        if (up == 0 && right == 0 && down == 0 && left == 0) {
            playerSession.getInkActions().removeIf(inkAction -> inkAction instanceof BorderInkAction);
            isRunning = false;
        }
        return InkActionResult.ok();
    }

    @Override
    public boolean needScene() {
        return false;
    }
}
