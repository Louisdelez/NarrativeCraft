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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Vector2f;

public class TextEffectAnimation {

    private final List<DialogLetterEffect> dialogLetterEffectList;
    private final Map<Integer, Vector2f> letterOffsets = new HashMap<>();
    private final Map<Integer, Vector2f> oldLetterOffsets = new HashMap<>();
    private int tickCounter;

    public TextEffectAnimation(ParsedDialog parsedDialog) {
        dialogLetterEffectList = TextEffect.apply(parsedDialog.effects());
    }

    public void tick() {
        tickCounter++;

        oldLetterOffsets.clear();
        oldLetterOffsets.putAll(letterOffsets);

        for (DialogLetterEffect dialogLetterEffect : dialogLetterEffectList.stream()
                .filter(dialogLetterEffect -> dialogLetterEffect.getAnimation() != DialogAnimationType.WAIT)
                .toList()) {
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
                    letterOffsets.put(j, new Vector2f(offsetX, offsetY));
                    oldLetterOffsets.put(j, new Vector2f(offsetX, offsetY));
                }

            } else if (dialogLetterEffect.getAnimation() == DialogAnimationType.WAVE) {
                float waveSpacing = 0.2f;
                double waveSpeed = (double) tickCounter / dialogLetterEffect.getTotalTick();

                for (int j = dialogLetterEffect.getStartIndex(); j < dialogLetterEffect.getEndIndex(); j++) {
                    float offsetY = (float) (Math.sin(waveSpeed + j * waveSpacing) * dialogLetterEffect.getForce());
                    letterOffsets.put(j, new Vector2f(0, offsetY));
                }
            }
        }
    }

    public boolean canTick(int currentCharIndex) {
        for (DialogLetterEffect dialogLetterEffect : dialogLetterEffectList.stream()
                .filter(dialogLetterEffect -> dialogLetterEffect.getAnimation() == DialogAnimationType.WAIT)
                .toList()) {
            if (currentCharIndex < dialogLetterEffect.getStartIndex() - 1) continue;
            dialogLetterEffect.tick();
            return dialogLetterEffect.getCooldownTick() == 0;
        }
        return true;
    }

    public Map<Integer, Vector2f> getOffsets(float partialTick) {
        Map<Integer, Vector2f> interpolated = new HashMap<>();

        for (int j : letterOffsets.keySet()) {
            Vector2f oldOffset = oldLetterOffsets.getOrDefault(j, new Vector2f(0, 0));
            Vector2f newOffset = letterOffsets.get(j);

            float x = Mth.lerp(partialTick, oldOffset.x, newOffset.x);
            float y = Mth.lerp(partialTick, oldOffset.y, newOffset.y);

            interpolated.put(j, new Vector2f(x, y));
        }

        return interpolated;
    }
}
