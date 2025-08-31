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

package fr.loudo.narrativecraft.util;

import fr.loudo.narrativecraft.narrative.keyframes.KeyframeLocation;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframeGroup;
import java.util.List;
import net.minecraft.util.Mth;

public class CatmullRoll {

    public static KeyframeLocation interpolate(
            double partialTick, CutsceneKeyframeGroup cutsceneKeyframeGroup, int totalTick) {
        List<CutsceneKeyframe> keyframes = cutsceneKeyframeGroup.getKeyframes();
        if (keyframes.size() < 2) return keyframes.getFirst().getKeyframeLocation();

        int startIndex = 0;
        for (CutsceneKeyframe keyframe : keyframes) {
            if (keyframe.getId() == keyframes.getFirst().getId()) {
                break;
            }
            startIndex++;
        }

        double elapsedTick = (totalTick + partialTick) - keyframes.getFirst().getTick();
        long accumulatedTick = 0;
        for (int i = startIndex; i < keyframes.size() - 1; i++) {
            CutsceneKeyframe k1 = keyframes.get(i);
            CutsceneKeyframe k2 = keyframes.get(i + 1);

            int segmentDuration = (int) (k2.getPathTick() / k2.getSpeed());
            if (elapsedTick < accumulatedTick + segmentDuration) {
                double t = (elapsedTick - accumulatedTick) / segmentDuration;

                CutsceneKeyframe p0 = keyframes.get(Math.max(i - 1, 0));
                CutsceneKeyframe p1 = k1;
                CutsceneKeyframe p2 = k2;
                CutsceneKeyframe p3 = keyframes.get(Math.min(i + 2, keyframes.size() - 1));

                return interpolateArcLength(p0, p1, p2, p3, t);
            }
            accumulatedTick += segmentDuration;
        }
        return keyframes.getLast().getKeyframeLocation();
    }

    private static KeyframeLocation catmullRom(
            KeyframeLocation p0, KeyframeLocation p1, KeyframeLocation p2, KeyframeLocation p3, double t) {

        double x = MathHelper.catmullRom(
                (float) p0.getX(), (float) p1.getX(), (float) p2.getX(), (float) p3.getX(), (float) t);

        double y = MathHelper.catmullRom(
                (float) p0.getY(), (float) p1.getY(), (float) p2.getY(), (float) p3.getY(), (float) t);

        double z = MathHelper.catmullRom(
                (float) p0.getZ(), (float) p1.getZ(), (float) p2.getZ(), (float) p3.getZ(), (float) t);

        float pitch = MathHelper.catmullRom(p0.getPitch(), p1.getPitch(), p2.getPitch(), p3.getPitch(), (float) t);

        float yaw = interpolateYawCatmullRom(p0.getYaw(), p1.getYaw(), p2.getYaw(), p3.getYaw(), (float) t);

        float fov = MathHelper.catmullRom(p0.getFov(), p1.getFov(), p2.getFov(), p3.getFov(), (float) t);

        return new KeyframeLocation(x, y, z, pitch, yaw, 0, fov);
    }

    private static float interpolateYawCatmullRom(float a0, float a1, float a2, float a3, double t) {
        a0 = MathHelper.unwrap360(a0, a1);
        a2 = MathHelper.unwrap360(a2, a1);
        a3 = MathHelper.unwrap360(a3, a2);

        float angle = MathHelper.catmullRom(a0, a1, a2, a3, (float) t);

        return MathHelper.wrap360(angle);
    }

    private static KeyframeLocation interpolateArcLength(
            CutsceneKeyframe p0, CutsceneKeyframe p1, CutsceneKeyframe p2, CutsceneKeyframe p3, double progress) {
        int samples = 60;
        double[] tValues = new double[samples + 1];
        double[] arcLengths = new double[samples + 1];

        KeyframeLocation prev = catmullRom(
                p0.getKeyframeLocation(),
                p1.getKeyframeLocation(),
                p2.getKeyframeLocation(),
                p3.getKeyframeLocation(),
                0.0);
        tValues[0] = 0.0;
        arcLengths[0] = 0.0;

        double totalLength = 0.0;
        for (int i = 1; i <= samples; i++) {
            double t = (double) i / samples;
            KeyframeLocation curr = catmullRom(
                    p0.getKeyframeLocation(),
                    p1.getKeyframeLocation(),
                    p2.getKeyframeLocation(),
                    p3.getKeyframeLocation(),
                    t);
            totalLength += distance(prev, curr);
            tValues[i] = t;
            arcLengths[i] = totalLength;
            prev = curr;
        }

        double targetLength = progress * totalLength;

        int low = 0, high = samples;
        while (low < high) {
            int mid = (low + high) / 2;
            if (arcLengths[mid] < targetLength) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        int idx = Math.max(1, low);
        double lengthBefore = arcLengths[idx - 1];
        double lengthAfter = arcLengths[idx];
        double segmentLength = lengthAfter - lengthBefore;
        double factor = (segmentLength == 0) ? 0 : (targetLength - lengthBefore) / segmentLength;

        double tFinal = Mth.lerp(factor, tValues[idx - 1], tValues[idx]);

        return catmullRom(
                p0.getKeyframeLocation(),
                p1.getKeyframeLocation(),
                p2.getKeyframeLocation(),
                p3.getKeyframeLocation(),
                tFinal);
    }

    private static double distance(KeyframeLocation a, KeyframeLocation b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
