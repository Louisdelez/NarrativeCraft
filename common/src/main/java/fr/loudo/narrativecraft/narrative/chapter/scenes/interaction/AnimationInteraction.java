package fr.loudo.narrativecraft.narrative.chapter.scenes.interaction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;

public class AnimationInteraction extends Interaction {

    private Animation animation;

    public AnimationInteraction(String name, Scene scene) {
        super(name, "", scene);
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    @Override
    public InteractionType getType() {
        return InteractionType.ANIMATION;
    }
}
