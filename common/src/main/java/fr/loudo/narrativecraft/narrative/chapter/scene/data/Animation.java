package fr.loudo.narrativecraft.narrative.chapter.scene.data;

import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;

public class Animation extends SceneData {

    private transient CharacterStory characterStory;
    //private List<ActionsData> actionsData = new ArrayList<>();
    private String skinName = "main.png";

    public Animation(String name, String description, Scene scene) {
        super(name, description, scene);
    }

    public CharacterStory getCharacterStory() {
        return characterStory;
    }

    public void setCharacterStory(CharacterStory characterStory) {
        this.characterStory = characterStory;
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }
}
