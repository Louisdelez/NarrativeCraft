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

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class DialogEntityBobbing {

    private final Minecraft minecraft = Minecraft.getInstance();

    private final int entityId;

    private final float shakeDecayRate;
    private float noiseShakeSpeed;
    private float noiseShakeStrength;

    private final SimplexNoise noise;
    private float noiseI = 0.0f;

    private float lastOffsetX = 0.0f;
    private float lastOffsetY = 0.0f;
    private float currentOffsetX = 0.0f;
    private float currentOffsetY = 0.0f;

    private float lastXRot;
    private float lastYRot;

    public DialogEntityBobbing(DialogRenderer3D dialogRenderer3D, float noiseShakeSpeed, float noiseShakeStrength) {
        Entity entity = dialogRenderer3D.getCharacterRuntime().getEntity();
        if (entity != null) {
            lastXRot = entity.getXRot();
            lastYRot = entity.getYRot();
        }
        entityId = dialogRenderer3D.getCharacterRuntime().getEntity().getId();
        noise = new SimplexNoise(RandomSource.create());
        this.noiseShakeSpeed = noiseShakeSpeed;
        this.shakeDecayRate = 0;
        this.noiseShakeStrength = noiseShakeStrength;
    }

    public void tick() {
        noiseI += (1.0f / 20.0f) * noiseShakeSpeed;

        float noiseShakeStrength = Mth.lerp(shakeDecayRate * (1.0f / 20.0f), this.noiseShakeStrength, 0.0f);

        lastOffsetX = currentOffsetX;
        lastOffsetY = currentOffsetY;

        currentOffsetX = (float) noise.getValue(1, noiseI) * noiseShakeStrength;
        currentOffsetY = (float) noise.getValue(100, noiseI) * noiseShakeStrength;

        if (Math.abs(noiseShakeStrength) <= 0) {
            currentOffsetX = currentOffsetY = lastOffsetX = lastOffsetY = 0;
        }
    }

    public void partialTick(float partialTick) {
        Entity entity = minecraft.level.getEntity(entityId);
        if (entity == null) return;

        float interpolatedX = Mth.lerp(partialTick, lastOffsetX, currentOffsetX);
        float interpolatedY = Mth.lerp(partialTick, lastOffsetY, currentOffsetY);

        entity.setYRot(lastYRot + interpolatedY);
        entity.setYHeadRot(lastYRot + interpolatedY);
        entity.setXRot(lastXRot + interpolatedX);
    }

    public float getNoiseShakeSpeed() {
        return noiseShakeSpeed;
    }

    public void setNoiseShakeSpeed(float noiseShakeSpeed) {
        this.noiseShakeSpeed = noiseShakeSpeed;
    }

    public float getNoiseShakeStrength() {
        return noiseShakeStrength;
    }

    public void setNoiseShakeStrength(float noiseShakeStrength) {
        this.noiseShakeStrength = noiseShakeStrength;
    }
}
