package fr.loudo.narrativecraft.narrative.chapter.scenes.interaction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;

public class CharacterInteraction extends Interaction {

    private CharacterStoryData characterData;

    public CharacterInteraction(String name, Scene scene) {
        super(name, "", scene);
    }

    public CharacterStoryData getCharacterData() {
        return characterData;
    }

    public void setCharacterData(CharacterStoryData characterData) {
        this.characterData = characterData;
    }

    @Override
    public InteractionType getType() {
        return InteractionType.CHARACTER;
    }
}
