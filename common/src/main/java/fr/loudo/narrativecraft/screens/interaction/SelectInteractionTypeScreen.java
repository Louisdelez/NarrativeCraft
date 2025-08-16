package fr.loudo.narrativecraft.screens.interaction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.interaction.CharacterInteraction;
import fr.loudo.narrativecraft.narrative.chapter.scenes.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.chapter.scenes.interaction.InteractionType;
import fr.loudo.narrativecraft.narrative.chapter.scenes.interaction.ObjectInteraction;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SelectInteractionTypeScreen extends Screen {

    private final String name;
    private final Scene scene;
    private final Consumer<Interaction> interactionConsumer;

    public SelectInteractionTypeScreen(String name, Scene scene, Consumer<Interaction> interactionConsumer) {
        super(Component.literal("Select interaction type screen"));
        this.name = name;
        this.scene = scene;
        this.interactionConsumer = interactionConsumer;
    }

    @Override
    protected void init() {
        Component message = Component.literal("Select the interaction type.");
        int middleX = this.width / 2 - this.font.width(message) / 2;
        int middleY = this.height / 2 - 10 - 5;
        StringWidget stringWidget = new StringWidget(
                message,
                this.font
        );
        stringWidget.setX(middleX);
        stringWidget.setY(middleY);
        this.addRenderableWidget(stringWidget);

        middleY = this.height / 2 + 10 + 5;
        middleX = this.width / 2 - 130 / 2;
        for(InteractionType interactionType : InteractionType.values()) {
            Button button = Button.builder(Component.literal(interactionType.name()), button1 -> {
                Interaction interaction = null;
                switch (interactionType) {
                    case CHARACTER -> {
                        interaction = new CharacterInteraction(name, scene);
                    }
                    case OBJECT -> {
                        interaction = new ObjectInteraction(name, scene);
                    }
                }
                interactionConsumer.accept(interaction);
            }).bounds(middleX, middleY, 130, 20).build();
            this.addRenderableWidget(button);
            middleY += button.getHeight() + 2;
        }
    }
}
