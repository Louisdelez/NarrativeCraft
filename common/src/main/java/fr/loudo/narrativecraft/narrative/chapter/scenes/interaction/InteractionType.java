package fr.loudo.narrativecraft.narrative.chapter.scenes.interaction;

public enum InteractionType {
    ANIMATION(AnimationInteraction.class),
    CHARACTER(CharacterInteraction.class),
    OBJECT(ObjectInteraction.class);

    private final Class<? extends Interaction> interactionClass;

    InteractionType(Class<? extends Interaction> interactionClass) {
        this.interactionClass = interactionClass;
    }

    public Class<? extends Interaction> getInteractionClass() {
        return interactionClass;
    }
}
