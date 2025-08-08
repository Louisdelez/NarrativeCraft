package fr.loudo.narrativecraft.narrative.story.text;

import fr.loudo.narrativecraft.narrative.dialog.DialogAnimationType;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogLetterEffect;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class TextEffectAnimation {

    private DialogLetterEffect dialogLetterEffect;
    private final Map<Integer, Vector2f> letterOffsets = new HashMap<>();
    private long animationTime;

    public TextEffectAnimation(DialogLetterEffect dialogLetterEffect) {
        this.dialogLetterEffect = dialogLetterEffect;
    }

    public Map<Integer, Vector2f> getOffsets() {
        long now = System.currentTimeMillis();
        if (!Minecraft.getInstance().isPaused()) {
            if (dialogLetterEffect.getAnimation() == DialogAnimationType.SHAKING && now - animationTime >= dialogLetterEffect.getTime()) {
                animationTime = now;
                letterOffsets.clear();
                for (int j = dialogLetterEffect.getStartIndex(); j < dialogLetterEffect.getEndIndex(); j++) {
                    float offsetX = MathUtils.getRandomFloat(-dialogLetterEffect.getForce(), dialogLetterEffect.getForce());
                    float offsetY = MathUtils.getRandomFloat(-dialogLetterEffect.getForce(), dialogLetterEffect.getForce());
                    letterOffsets.put(j, new Vector2f(offsetX, offsetY));
                }
            } else if (dialogLetterEffect.getAnimation() == DialogAnimationType.WAVING) {
                letterOffsets.clear();
                float waveSpacing = 0.2f;
                double waveSpeed = (double) now / dialogLetterEffect.getTime();

                for (int j = dialogLetterEffect.getStartIndex(); j < dialogLetterEffect.getEndIndex(); j++) {
                    float offsetY = (float) (Math.sin(waveSpeed + j * waveSpacing) * dialogLetterEffect.getForce());
                    letterOffsets.put(j, new Vector2f(0, offsetY));
                }
            }
        }
        return letterOffsets;
    }
}
