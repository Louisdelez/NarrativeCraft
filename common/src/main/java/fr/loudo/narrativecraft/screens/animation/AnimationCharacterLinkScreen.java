package fr.loudo.narrativecraft.screens.animation;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.screens.components.GenericSelectionScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class AnimationCharacterLinkScreen extends GenericSelectionScreen<CharacterStory> {
    private final CharacterType characterType;
    private final Animation animation;

    public AnimationCharacterLinkScreen(Screen lastScreen,
                                        Animation animation,
                                        List<CharacterStory> characterStoryList,
                                        CharacterType characterType,
                                        Consumer<CharacterStory> consumer) {
        super(lastScreen,
                "Link animation to character",
                characterStoryList,
                animation.getCharacter(),
                consumer);
        this.characterType = characterType;
        this.animation = animation;
    }

    public AnimationCharacterLinkScreen(Screen lastScreen,
                                        Animation animation,
                                        Consumer<CharacterStory> consumer) {
        super(lastScreen,
                "Link animation to character",
                NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories(),
                animation.getCharacter(),
                consumer);
        this.characterType = CharacterType.MAIN;
        this.animation = animation;
    }

    @Override
    protected void addCustomTitleButtons(LinearLayout layout) {
        layout.addChild(Button.builder(
                characterType == CharacterType.NPC ? Component.literal("MAIN") : Component.literal("NPC"),
                button -> {
                    Screen screen;
                    if(characterType == CharacterType.MAIN) {
                        screen = new AnimationCharacterLinkScreen(lastScreen, animation, animation.getScene().getNpcs(), CharacterType.NPC, consumer);
                    } else {
                        screen = new AnimationCharacterLinkScreen(lastScreen, animation, NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories(), CharacterType.MAIN, consumer);
                    }
                    minecraft.setScreen(screen);
                })
        .width(40)
        .build());
    }
}
