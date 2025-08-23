package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.narrative.playback.Playback;

public class CharacterRuntime {
    private final CharacterStory characterStory;
    private final Playback playback;

    public CharacterRuntime(CharacterStory characterStory, Playback playback) {
        this.characterStory = characterStory;
        this.playback = playback;
    }

    public CharacterStory getCharacterStory() {
        return characterStory;
    }

    public Playback getAnimation() {
        return playback;
    }
}
