package fr.loudo.narrativecraft.narrative.story.text;

import fr.loudo.narrativecraft.narrative.dialog.DialogAnimationType;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogLetterEffect;

import java.util.List;
import java.util.Map;

public record TextEffect(DialogAnimationType type, int startIndex, int endIndex, Map<String, String> parameters) {
    public static DialogLetterEffect apply(List<TextEffect> effects) {
        if (effects.isEmpty()) {
            return new DialogLetterEffect(
                    DialogAnimationType.NONE
            );
        }
        for (TextEffect effect : effects) {
            double time = Double.parseDouble(effect.parameters().getOrDefault("time", "-1"));
            float force = Float.parseFloat(effect.parameters().getOrDefault("force", "-1"));

            switch (effect.type()) {
                case WAVING -> {
                    time = time == -1 ? 0.3 : time;
                    force = force == -1 ? 1f : force;
                }
                case SHAKING -> {
                    time = time == -1 ? 0.05 : time;
                    force = force == -1 ? 0.35f : force;
                }
            }

            return new DialogLetterEffect(
                    effect.type(),
                    (long) (time * 1000L),
                    force,
                    effect.startIndex(),
                    effect.endIndex()
            );
        }
        return null;
    }
}