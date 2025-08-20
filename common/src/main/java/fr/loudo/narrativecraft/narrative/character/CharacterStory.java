package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import net.minecraft.client.resources.PlayerSkin;

public class CharacterStory extends NarrativeEntry {

    private String birthDate;
    private CharacterType characterType;
    private PlayerSkin.Model model;

    public CharacterStory(String name, String description, String day, String month, String year, PlayerSkin.Model model, CharacterType characterType) {
        super(name, description);
        this.birthDate = day + "/" + month + "/" + year;
        this.characterType = characterType;
        this.model = model;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }

    public PlayerSkin.Model getModel() {
        return model;
    }

    public void setModel(PlayerSkin.Model model) {
        this.model = model;
    }
}
