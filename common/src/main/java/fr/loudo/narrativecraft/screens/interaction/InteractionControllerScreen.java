package fr.loudo.narrativecraft.screens.interaction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.interaction.CharacterInteraction;
import fr.loudo.narrativecraft.narrative.chapter.scenes.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.chapter.scenes.interaction.InteractionController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.interaction.InteractionType;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleAddRecord;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleInfoKeyframeScreen;
import fr.loudo.narrativecraft.screens.components.AddCharacterListScreen;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class InteractionControllerScreen extends Screen {

    private final int BUTTON_HEIGHT = 20;
    private final int BUTTON_WIDTH = 30;
    private final InteractionController interactionController;

    public InteractionControllerScreen(InteractionController interactionController) {
        super(Component.literal("Camera Angle Controller Screen"));
        this.interactionController = interactionController;
    }

    @Override
    protected void init() {
        int spacing = 5;
        int totalWidth = BUTTON_WIDTH * 5 + spacing * 3;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height - 50;
        if(interactionController.getInteraction().getType() == InteractionType.CHARACTER) {
            Button addCharacter = Button.builder(ImageFontConstants.CHARACTER_ADD, button -> {
                AddCharacterListScreen addCharacterListScreen = new AddCharacterListScreen(interactionController.getInteraction().getScene(), characterStoryData -> {
                    CharacterInteraction characterInteraction = (CharacterInteraction) interactionController.getInteraction();
                    if(characterInteraction.getCharacterData() != null) {
                        NarrativeCraftMod.server.execute(() -> characterInteraction.getCharacterData().getCharacterStory().kill());
                    }
                    characterStoryData.spawn(Utils.getServerLevel());
                    characterInteraction.setCharacterData(characterStoryData);
                });
                minecraft.setScreen(addCharacterListScreen);
            }).bounds(startX + (BUTTON_WIDTH + spacing) * 1, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
            addCharacter.setTooltip(Tooltip.create(Translation.message("screen.camera_angle_controller.tooltip.character")));
            this.addRenderableWidget(addCharacter);
        } else if(interactionController.getInteraction().getType() == InteractionType.ANIMATION) {
//            Button recordMenu = Button.builder(ImageFontConstants.SETTINGS, button -> {
//                CameraAngleAddRecord cameraAngleAddRecord = new CameraAngleAddRecord(interactionController.getCameraAngleGroup());
//                minecraft.setScreen(cameraAngleAddRecord);
//            }).bounds(startX + (BUTTON_WIDTH + spacing) * 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
//            recordMenu.setTooltip(Tooltip.create(Translation.message("screen.camera_angle_controller.tooltip.template")));
//            this.addRenderableWidget(recordMenu);
        }

        Button saveButton = Button.builder(ImageFontConstants.SAVE, button -> {
            NarrativeCraftMod.server.execute(() -> interactionController.stopSession(true));
            this.onClose();
        }).bounds(startX + (BUTTON_WIDTH + spacing) * 3, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(saveButton);

    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
