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

package fr.loudo.narrativecraft.narrative.interaction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.compat.api.RenderChannel;
import fr.loudo.narrativecraft.util.Easing;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class InteractionEyeRenderer {

    private static final double APPEAR_TIME = 0.15;
    private static final float BASE_SCALE = 0.025F;
    private static final float SIZE = 10F;
    private static final int MAX_OPACITY = 255;
    private static int currentTick;
    private static Vec3 position;
    private static boolean isWatching;
    private static int lastEntityId;

    public static void tick() {
        currentTick += isWatching ? 1 : -1;
        int maxTick = (int) (APPEAR_TIME * 20);
        if (currentTick > maxTick) currentTick = maxTick;
        if (currentTick < 0) currentTick = 0;
    }

    public static void render(PoseStack poseStack, float partialTick, int entityId) {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.level.getEntity(entityId);
        Vec3 camPos = mc.gameRenderer.getMainCamera().position();

        isWatching = entity != null;

        if (entityId != -1 && lastEntityId != entityId) currentTick = 0;
        if (isWatching) {
            position = entity.position().add(0, entity.getBbHeight() / 2, 0);
            lastEntityId = entityId;
        }
        if (position == null || (currentTick == 0 && !isWatching)) return;

        poseStack.pushPose();
        poseStack.translate(position.x - camPos.x, position.y - camPos.y, position.z - camPos.z);
        poseStack.mulPose(mc.getEntityRenderDispatcher().camera.rotation());

        int totalTick = (int) (APPEAR_TIME * 20);
        double t = (currentTick + (isWatching ? partialTick : (1.0 - partialTick))) / totalTick;
        t = Easing.SMOOTH.interpolate(Mth.clamp(t, 0.0, 1.0));

        float scale = (float) (BASE_SCALE * t);
        int opacity = (int) (MAX_OPACITY * t);

        poseStack.scale(scale, -scale, scale);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(NarrativeCraftMod.getRenderType(RenderChannel.DIALOG_BACKGROUND));
        Matrix4f mat = poseStack.last().pose();

        vc.addVertex(mat, -SIZE, SIZE, -1).setColor(0, 0, 0, opacity).setLight(LightTexture.FULL_BRIGHT);
        vc.addVertex(mat, SIZE, SIZE, -1).setColor(0, 0, 0, opacity).setLight(LightTexture.FULL_BRIGHT);
        vc.addVertex(mat, SIZE, -SIZE, -1).setColor(0, 0, 0, opacity).setLight(LightTexture.FULL_BRIGHT);
        vc.addVertex(mat, -SIZE, -SIZE, -1).setColor(0, 0, 0, opacity).setLight(LightTexture.FULL_BRIGHT);

        mc.font.drawInBatch(
                ImageFontConstants.EYE_OPEN,
                -mc.font.width(ImageFontConstants.EYE_OPEN) / 2f,
                -mc.font.lineHeight / 2f,
                NarrativeCraftMod.getColorCompat().color(opacity, 0xFFFFFF),
                false,
                mat,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LightTexture.FULL_BRIGHT);

        poseStack.popPose();
        bufferSource.endBatch();
    }
}
