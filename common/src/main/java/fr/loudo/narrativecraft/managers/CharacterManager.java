package fr.loudo.narrativecraft.managers;

import fr.loudo.narrativecraft.narrative.character.CharacterStory;

import java.util.ArrayList;
import java.util.List;

public class CharacterManager {

    private final List<CharacterStory> characterStories = new ArrayList<>();

    public void addCharacter(CharacterStory characterStory) {

    }

    public void removeCharacter(CharacterStory characterStory) {

    }

    public CharacterStory getCharacterByName(String name) {
        return null;
    }

    public List<CharacterStory> getCharacterStories() {
        return characterStories;
    }
}
