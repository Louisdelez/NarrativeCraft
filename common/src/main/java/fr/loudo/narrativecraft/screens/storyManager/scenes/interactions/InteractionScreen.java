package fr.loudo.narrativecraft.screens.storyManager.scenes.interactions;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.interaction.InteractionController;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.MainScreenController;
import fr.loudo.narrativecraft.screens.characters.CharacterEntityTypeScreen;
import fr.loudo.narrativecraft.screens.components.EditCharacterInfoScreen;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.List;

public class InteractionScreen extends StoryElementScreen {

    private final Scene scene;

    public InteractionScreen(Scene scene) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.interaction_manager.title", Component.literal(scene.getName()).withColor(StoryElementScreen.SCENE_NAME_COLOR)));
        this.scene = scene;
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, (p_345997_) -> this.onClose()).width(200).build());
    }

    @Override
    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.getSceneFolder(scene).toPath());
    }

    @Override
    public void onClose() {
        ScenesMenuScreen screen = new ScenesMenuScreen(scene);
        this.minecraft.setScreen(screen);
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = scene.getInteractionList().stream()
                .map(interaction -> {
                    Button button = Button.builder(Component.literal(interaction.getName()), b -> {
                        InteractionController interactionController = new InteractionController(interaction, Utils.getServerPlayerByUUID(this.minecraft.player.getUUID()), Playback.PlaybackType.DEVELOPMENT);
                        interactionController.startSession();
                        minecraft.setScreen(null);
                    }).build();
                    return new StoryElementList.StoryEntryData(button, interaction);
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    public Scene getScene() {
        return scene;
    }
}
