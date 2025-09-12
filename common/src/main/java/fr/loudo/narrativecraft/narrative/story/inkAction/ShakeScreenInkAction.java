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
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class ShakeScreenInkAction extends InkAction {

    private static final float PIXEL = 0.025f;

    private float noiseShakeSpeed;
    private float noiseShakeStrength;
    private float shakeDecayRate;

    private SimplexNoise noise;
    private float noiseI = 0.0f;
    private float shakeStrength = 0.0f;

    private float lastOffsetX = 0.0f;
    private float lastOffsetY = 0.0f;
    private float currentOffsetX = 0.0f;
    private float currentOffsetY = 0.0f;

    public ShakeScreenInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    public void stop() {
        super.stop();
        shakeStrength = 0.0f;
        currentOffsetX = currentOffsetY = lastOffsetX = lastOffsetY = 0;
        noiseI = 0.0f;
    }

    @Override
    public void tick() {
        if (!isRunning) return;

        noiseI += (1.0f / 20.0f) * noiseShakeSpeed;

        shakeStrength = Mth.lerp(shakeDecayRate * (1.0f / 20.0f), shakeStrength, 0.0f);

        lastOffsetX = currentOffsetX;
        lastOffsetY = currentOffsetY;

        currentOffsetX = (float) noise.getValue(1, noiseI) * shakeStrength;
        currentOffsetY = (float) noise.getValue(100, noiseI) * shakeStrength;

        if (Math.abs(shakeStrength) <= 0) {
            isRunning = false;
            currentOffsetX = currentOffsetY = lastOffsetX = lastOffsetY = 0;
        }
    }

    @Override
    public void render(PoseStack poseStack, float partialTick) {
        float interpolatedX = Mth.lerp(partialTick, lastOffsetX, currentOffsetX);
        float interpolatedY = Mth.lerp(partialTick, lastOffsetY, currentOffsetY);

        poseStack.translate(interpolatedX, interpolatedY, 0);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() == 1) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Shake strength"));
        }

        if (arguments.size() == 2) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Shake decay rate"));
        }

        if (arguments.size() == 3) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Shake speed"));
        }

        try {
            noiseShakeStrength = Float.parseFloat(arguments.get(1));
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(1)));
        }

        try {
            shakeDecayRate = Float.parseFloat(arguments.get(2));
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(2)));
        }

        try {
            noiseShakeSpeed = Float.parseFloat(arguments.get(3));
        } catch (NumberFormatException e) {
            return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
        }

        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        playerSession.getInkActions().removeIf(inkAction -> inkAction instanceof ShakeScreenInkAction);
        if (noShaking()) {
            isRunning = false;
            return InkActionResult.ok();
        }
        noise = new SimplexNoise(RandomSource.create());
        shakeStrength = noiseShakeStrength * PIXEL;
        noiseI = 0.0f;
        lastOffsetX = lastOffsetY = currentOffsetX = currentOffsetY = 0.0f;
        return InkActionResult.ok();
    }

    private boolean noShaking() {
        return noiseShakeStrength > 0 && shakeDecayRate > 0 && noiseShakeSpeed > 0;
    }
}
