package fr.loudo.narrativecraft.screens.storyManager.character;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CharactersScreen extends StoryElementScreen {
    public CharactersScreen() {
        super(Component.literal("Characters"), null);
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<CharacterStory> screen = new EditInfoScreen<>(this, null, new EditScreenCharacterAdapter());
            this.minecraft.setScreen(screen);
        });
        initFolderButton();
    }

    @Override
    protected void addContents() {
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();

        List<StoryElementList.StoryEntryData> entries = characterManager.getCharacterStories().stream()
                .map(character -> {
                    Button button = Button.builder(Component.literal(character.getName()), button1 -> {}).build();
                    button.active = false;

                    return new StoryElementList.StoryEntryData(button, () -> {
                        minecraft.setScreen(new EditInfoScreen<>(this, character, new EditScreenCharacterAdapter()));
                    }, () -> {
                        characterManager.removeCharacter(character);
                        NarrativeCraftFile.deleteCharacterFolder(character);
                        minecraft.setScreen(new CharactersScreen());
                    });
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    @Override
    protected void openFolder() {
        net.minecraft.Util.getPlatform().openPath(NarrativeCraftFile.characterDirectory.toPath());
    }
}
