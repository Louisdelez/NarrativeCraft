package fr.loudo.narrativecraft.screens.interaction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.interaction.Interaction;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class InteractionStitchScreen extends Screen {

    private final Interaction interaction;

    protected InteractionStitchScreen(Interaction interaction) {
        super(Component.literal("Link interaction stitch"));
        this.interaction = interaction;
    }

    @Override
    protected void init() {
        ScreenUtils.LabelBox labelBox = new ScreenUtils.LabelBox(
                Component.literal("Stitch to trigger"),
                minecraft.font,
                100,
                20,
                width / 2 - 100 / 2,
                height / 2 - 20 / 2,
                ScreenUtils.Align.VERTICAL
        );
        labelBox.getEditBox().setValue(interaction.getStitch());
        this.addRenderableWidget(labelBox.getStringWidget());
        this.addRenderableWidget(labelBox.getEditBox());

        Button doneButton = Button.builder(CommonComponents.GUI_DONE, button -> {
            String stitchValue = labelBox.getEditBox().getValue();
            if(!stitchValue.isEmpty()) {
                interaction.setStitch(stitchValue);
                this.onClose();
            } else {
                ScreenUtils.sendToast(Component.literal("Error"), Component.literal("Stitch value cannot be empty."));
            }
        }).pos(width / 2 - 150 / 2, height / 2 + ((20 / 2) * 2) + 10).build();
        this.addRenderableWidget(doneButton);

        Button closeButton = Button.builder(CommonComponents.GUI_BACK, button -> {
            this.onClose();
        }).pos(width / 2 - 150 / 2, height / 2 + (((20 / 2) * 2) * 2) + 15).build();
        this.addRenderableWidget(closeButton);
    }
}
