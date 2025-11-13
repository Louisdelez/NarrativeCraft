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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record TextEffect(DialogAnimationType type, int startIndex, int endIndex, Map<String, String> parameters) {
    public static List<DialogLetterEffect> apply(List<TextEffect> effects) {
        List<DialogLetterEffect> dialogLetterEffectList = new ArrayList<>();
        if (effects.isEmpty()) {
            return dialogLetterEffectList;
        }
        for (TextEffect effect : effects) {
            double time = 0;
            float force = 0;
            try {
                time = Double.parseDouble(effect.parameters().getOrDefault("time", "-1"));
                force = Float.parseFloat(effect.parameters().getOrDefault("force", "-1"));
            } catch (NumberFormatException ignored) {
            }

            switch (effect.type()) {
                case WAVE -> {
                    time = time == -1 ? 0.3 : time;
                    force = force == -1 ? 1f : force;
                }
                case SHAKE -> {
                    time = time == -1 ? 0.05 : time;
                    force = force == -1 ? 0.35f : force;
                }
                case WAIT -> time = time == -1 ? 0 : time;
            }

            dialogLetterEffectList.add(
                    new DialogLetterEffect(effect.type(), time, force, effect.startIndex(), effect.endIndex()));
        }
        return dialogLetterEffectList;
    }
}
