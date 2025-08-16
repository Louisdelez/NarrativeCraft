package fr.loudo.narrativecraft.screens.cameraAngles;

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.screens.components.AddCharacterListScreen;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CameraAngleControllerScreen extends Screen {

    private final int BUTTON_HEIGHT = 20;
    private final int BUTTON_WIDTH = 30;
    private final CameraAngleController cameraAngleController;

    public CameraAngleControllerScreen(CameraAngleController cameraAngleController) {
        super(Component.literal("Camera Angle Controller Screen"));
        this.cameraAngleController = cameraAngleController;
    }

    @Override
    protected void init() {
        int spacing = 5;
        int totalWidth = BUTTON_WIDTH * 5 + spacing * 3;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height - 50;

        Button addKeyframe = Button.builder(ImageFontConstants.ADD_KEYFRAME, button -> {
            CameraAngleInfoKeyframeScreen screen = new CameraAngleInfoKeyframeScreen(cameraAngleController);
            this.minecraft.setScreen(screen);
        }).bounds(startX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addKeyframe.setTooltip(Tooltip.create(Translation.message("screen.camera_angle_controller.tooltip.keyframe_group")));
        this.addRenderableWidget(addKeyframe);

        Button addCharacter = Button.builder(ImageFontConstants.CHARACTER_ADD, button -> {
            AddCharacterListScreen addCharacterListScreen = new AddCharacterListScreen(cameraAngleController.getCameraAngleGroup().getScene(), characterStoryData -> {
                if(characterStoryData.getCharacterStory().getEntity() instanceof FakePlayer fakePlayer) {
                    LocalPlayer localPlayer = Minecraft.getInstance().player;
                    CameraAngleGroup cameraAngleGroup = cameraAngleController.getCameraAngleGroup();
                    if(cameraAngleGroup.getCharacterStoryData(characterStoryData.getCharacterStory().getName()) != null) {
                        minecraft.player.displayClientMessage(Component.literal("§c" + Translation.message("screen.camera_angle_character.add.fail").getString()), false);
                        return;
                    }
                    for(EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                        ItemStack itemStack = localPlayer.getItemBySlot(equipmentSlot);
                        fakePlayer.getServer().getPlayerList().broadcastAll(new ClientboundSetEquipmentPacket(
                                fakePlayer.getId(),
                                List.of(new Pair<>(equipmentSlot, itemStack))
                        ));
                        fakePlayer.setItemSlot(equipmentSlot, itemStack);
                    }
                    cameraAngleGroup.addCharacter(characterStoryData, Playback.PlaybackType.DEVELOPMENT);
                }
            });
            minecraft.setScreen(addCharacterListScreen);
        }).bounds(startX + (BUTTON_WIDTH + spacing) * 1, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addCharacter.setTooltip(Tooltip.create(Translation.message("screen.camera_angle_controller.tooltip.character")));
        this.addRenderableWidget(addCharacter);

        Button recordMenu = Button.builder(ImageFontConstants.SETTINGS, button -> {
            CameraAngleAddRecord cameraAngleAddRecord = new CameraAngleAddRecord(cameraAngleController.getCameraAngleGroup());
            minecraft.setScreen(cameraAngleAddRecord);
        }).bounds(startX + (BUTTON_WIDTH + spacing) * 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        recordMenu.setTooltip(Tooltip.create(Translation.message("screen.camera_angle_controller.tooltip.template")));
        this.addRenderableWidget(recordMenu);

        Button saveButton = Button.builder(ImageFontConstants.SAVE, button -> {
            NarrativeCraftMod.server.execute(() -> cameraAngleController.stopSession(true));
            this.onClose();
        }).bounds(startX + (BUTTON_WIDTH + spacing) * 3, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(saveButton);

        Button closeButton = Button.builder(Component.literal("✖"), button -> {
            NarrativeCraftMod.server.execute(() -> cameraAngleController.stopSession(false));
            this.onClose();
        }).bounds(startX + (BUTTON_WIDTH + spacing) * 4, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(closeButton);

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
