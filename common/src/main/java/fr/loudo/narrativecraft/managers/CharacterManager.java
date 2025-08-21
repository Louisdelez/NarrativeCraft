package fr.loudo.narrativecraft.managers;

import fr.loudo.narrativecraft.narrative.character.CharacterStory;

import java.util.ArrayList;
import java.util.List;

public class CharacterManager {

    private final List<CharacterStory> characterStories = new ArrayList<>();

    public void addCharacter(CharacterStory characterStory) {
        if(characterStories.contains(characterStory)) return;
        characterStories.add(characterStory);
    }

    public void removeCharacter(CharacterStory characterStory) {
        characterStories.remove(characterStory);
    }

    public CharacterStory getCharacterByName(String name) {
        for(CharacterStory characterStory : characterStories) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return characterStory;
            }
        }
        return null;
    }

    public boolean characterExists(String name) {
        for(CharacterStory characterStory : characterStories) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public List<CharacterStory> getCharacterStories() {
        return characterStories;
    }

}
