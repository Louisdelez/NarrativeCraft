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

package fr.loudo.narrativecraft.narrative.story.text;

import fr.loudo.narrativecraft.narrative.dialog.DialogAnimationType;
import fr.loudo.narrativecraft.narrative.dialog.DialogLetterEffect;
import fr.loudo.narrativecraft.util.NarrativeProfiler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Vector2f;

public class TextEffectAnimation {

    // T095: Reusable Vector2f for zero offset to avoid allocation
    private static final Vector2f ZERO_OFFSET = new Vector2f(0, 0);

    private final List<DialogLetterEffect> dialogLetterEffectList;
    private final Map<Integer, Vector2f> letterOffsets = new HashMap<>();
    private final Map<Integer, Vector2f> oldLetterOffsets = new HashMap<>();
    // T095: Reusable map for interpolation results to avoid HashMap allocation per getOffsets() call
    private final Map<Integer, Vector2f> interpolatedCache = new HashMap<>();
    private int tickCounter;

    public TextEffectAnimation(ParsedDialog parsedDialog) {
        dialogLetterEffectList = TextEffect.apply(parsedDialog.effects());
    }

    public void tick() {
        NarrativeProfiler.start(NarrativeProfiler.TEXT_EFFECTS);
        tickCounter++;

        oldLetterOffsets.clear();
        oldLetterOffsets.putAll(letterOffsets);

        // T098: Replace stream().filter().toList() with direct loop
        // Before: dialogLetterEffectList.stream().filter(...).toList()
        // After: Direct iteration with inline filter - zero allocations
        for (DialogLetterEffect dialogLetterEffect : dialogLetterEffectList) {
            if (dialogLetterEffect.getAnimation() == DialogAnimationType.WAIT) {
                continue; // T098: Inline filter instead of stream filter
            }
            dialogLetterEffect.tick();
            if (dialogLetterEffect.getAnimation() == DialogAnimationType.SHAKE) {

                for (int j = dialogLetterEffect.getStartIndex(); j < dialogLetterEffect.getEndIndex(); j++) {
                    RandomSource randomSource = RandomSource.create();
                    float offsetX = 0;
                    float offsetY = 0;
                    if (dialogLetterEffect.canApplyEffect()) {
                        offsetX = Mth.randomBetween(
                                randomSource, -dialogLetterEffect.getForce(), dialogLetterEffect.getForce());
                        offsetY = Mth.randomBetween(
                                randomSource, -dialogLetterEffect.getForce(), dialogLetterEffect.getForce());
                    } else if (letterOffsets.containsKey(j)) {
                        offsetX = letterOffsets.get(j).x;
                        offsetY = letterOffsets.get(j).y;
                    }
                    // T095: Reuse Vector2f or update in place to reduce allocations
                    Vector2f offset = letterOffsets.computeIfAbsent(j, k -> new Vector2f());
                    offset.set(offsetX, offsetY);
                    Vector2f oldOffset = oldLetterOffsets.computeIfAbsent(j, k -> new Vector2f());
                    oldOffset.set(offsetX, offsetY);
                }

            } else if (dialogLetterEffect.getAnimation() == DialogAnimationType.WAVE) {
                float waveSpacing = 0.2f;
                double waveSpeed = (double) tickCounter / dialogLetterEffect.getTotalTick();

                for (int j = dialogLetterEffect.getStartIndex(); j < dialogLetterEffect.getEndIndex(); j++) {
                    float offsetY = (float) (Math.sin(waveSpeed + j * waveSpacing) * dialogLetterEffect.getForce());
                    // T095: Reuse Vector2f instead of allocating new one each tick
                    Vector2f offset = letterOffsets.computeIfAbsent(j, k -> new Vector2f());
                    offset.set(0, offsetY);
                }
            }
        }
        NarrativeProfiler.stop(NarrativeProfiler.TEXT_EFFECTS);
    }

    public boolean canTick(int currentCharIndex) {
        // T098: Replace stream().filter().toList() with direct loop
        // Before: dialogLetterEffectList.stream().filter(...).toList()
        // After: Direct iteration with inline filter - zero allocations
        for (DialogLetterEffect dialogLetterEffect : dialogLetterEffectList) {
            if (dialogLetterEffect.getAnimation() != DialogAnimationType.WAIT) {
                continue; // T098: Inline filter instead of stream filter
            }
            if (currentCharIndex < dialogLetterEffect.getStartIndex() - 1) continue;
            dialogLetterEffect.tick();
            return dialogLetterEffect.getCooldownTick() == 0;
        }
        return true;
    }

    public Map<Integer, Vector2f> getOffsets(float partialTick) {
        // T095: Reuse cached map instead of allocating new HashMap each call
        // Before: Map<Integer, Vector2f> interpolated = new HashMap<>();
        // After: Clear and reuse interpolatedCache
        interpolatedCache.clear();

        for (int j : letterOffsets.keySet()) {
            // T095: Use static ZERO_OFFSET instead of new Vector2f(0, 0)
            Vector2f oldOffset = oldLetterOffsets.getOrDefault(j, ZERO_OFFSET);
            Vector2f newOffset = letterOffsets.get(j);

            float x = Mth.lerp(partialTick, oldOffset.x, newOffset.x);
            float y = Mth.lerp(partialTick, oldOffset.y, newOffset.y);

            // T095: Reuse Vector2f from cache or create once
            Vector2f interpolatedOffset = interpolatedCache.computeIfAbsent(j, k -> new Vector2f());
            interpolatedOffset.set(x, y);
        }

        return interpolatedCache;
    }
}
