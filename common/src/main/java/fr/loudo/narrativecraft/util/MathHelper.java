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

public class MathHelper {
    public static float wrapDegrees360(float value) {
        return (value + 360) % 360;
    }

    public static long tickToMills(int tick) {
        return (long) ((tick / 20.0) * 1000);
    }

    public static double millisToSeconds(long millis) {
        return millis / 1000.0;
    }

    public static long secondsToMillis(float seconds) {
        return (long) (seconds * 1000L);
    }

    public static int secondsToTick(float seconds) {
        return (int) (seconds * 20);
    }

    public static double tickToSeconds(int tick) {
        return tick / 20.0;
    }

    public static float normalRotation(double t, float startAngle, float endAngle) {
        float diff = endAngle - startAngle;
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }
        return startAngle + (float) (diff * t);
    }

    public static float normalYaw(double t, float startAngle, float endAngle) {
        float diff = endAngle - startAngle;

        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }

        float interpolated = startAngle + (float) (diff * t);

        if (interpolated > 180) {
            interpolated -= 360;
        } else if (interpolated < -180) {
            interpolated += 360;
        }

        return interpolated;
    }

    public static float catmullRom(float p0, float p1, float p2, float p3, float t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return (float) (0.5
                * ((2 * p1)
                        + (-p0 + p2) * t
                        + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2
                        + (-p0 + 3 * p1 - 3 * p2 + p3) * t3));
    }

    public static float unwrap360(float angle, float reference) {
        float diff = angle - reference;
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;
        return reference + diff;
    }

    public static float wrap360(float angle) {
        while (angle <= -180) angle += 360;
        while (angle > 180) angle -= 360;
        return angle;
    }

    private float normalRotation(float startAngle, float endAngle, double t) {
        float diff = endAngle - startAngle;
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }
        return startAngle + (float) (diff * t);
    }
}
