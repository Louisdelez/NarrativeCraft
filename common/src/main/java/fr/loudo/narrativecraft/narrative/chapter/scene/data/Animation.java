package fr.loudo.narrativecraft.narrative.chapter.scene.data;

import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.narrative.recording.actions.ActionsData;

import java.util.List;

public class Animation extends SceneData {

    private transient CharacterStory character;
    private List<ActionsData> actionsData;

    public Animation(String name, Scene scene) {
        super(name, "", scene);
    }

    public Location getFirstLocation() {
        return actionsData.getFirst().getLocations().getFirst();
    }

    public CharacterStory getCharacter() {
        return character;
    }

    public void setCharacter(CharacterStory character) {
        this.character = character;
    }

    public List<ActionsData> getActionsData() {
        return actionsData;
    }

    public void setActionsData(List<ActionsData> actionsData) {
        this.actionsData = actionsData;
    }
}