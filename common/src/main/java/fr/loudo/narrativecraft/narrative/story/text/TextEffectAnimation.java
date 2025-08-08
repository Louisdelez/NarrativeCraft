package fr.loudo.narrativecraft.narrative.story.text;

import fr.loudo.narrativecraft.narrative.dialog.DialogAnimationType;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogLetterEffect;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextEffectAnimation {

    private List<DialogLetterEffect> dialogLetterEffectList;
    private final Map<Integer, Vector2f> letterOffsets = new HashMap<>();

    public TextEffectAnimation(String text) {
        this.dialogLetterEffectList = new ArrayList<>();
        ParsedDialog parsedDialog = ParsedDialog.parse(text);
        dialogLetterEffectList = TextEffect.apply(parsedDialog.effects());
    }

    public Map<Integer, Vector2f> getOffsets() {
        long now = System.currentTimeMillis();
        if (!Minecraft.getInstance().isPaused()) {
            for (DialogLetterEffect dialogLetterEffect : dialogLetterEffectList) {
                if (dialogLetterEffect.getAnimation() == DialogAnimationType.SHAKING &&
                        now - dialogLetterEffect.getLastUpdateTime() >= dialogLetterEffect.getTime()) {

                    for (int j = dialogLetterEffect.getStartIndex(); j < dialogLetterEffect.getEndIndex(); j++) {
                        float offsetX = MathUtils.getRandomFloat(-dialogLetterEffect.getForce(), dialogLetterEffect.getForce());
                        float offsetY = MathUtils.getRandomFloat(-dialogLetterEffect.getForce(), dialogLetterEffect.getForce());
                        letterOffsets.put(j, new Vector2f(offsetX, offsetY));
                    }

                    dialogLetterEffect.setLastUpdateTime(now);
                } else if (dialogLetterEffect.getAnimation() == DialogAnimationType.WAVING) {
                    float waveSpacing = 0.2f;
                    double waveSpeed = (double) now / dialogLetterEffect.getTime();

                    for (int j = dialogLetterEffect.getStartIndex(); j < dialogLetterEffect.getEndIndex(); j++) {
                        float offsetY = (float) (Math.sin(waveSpeed + j * waveSpacing) * dialogLetterEffect.getForce());
                        letterOffsets.put(j, new Vector2f(0, offsetY));
                    }
                }
            }
        }
        return letterOffsets;
    }

}
