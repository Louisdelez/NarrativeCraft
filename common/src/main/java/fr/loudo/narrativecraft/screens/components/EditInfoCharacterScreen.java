package fr.loudo.narrativecraft.screens.components;

import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.screens.storyManager.character.CharactersScreen;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class EditInfoCharacterScreen extends Screen {
    public final int WIDGET_WIDTH = 190;
    public final int EDIT_BOX_NAME_HEIGHT = 20;
    public final int EDIT_BOX_DESCRIPTION_HEIGHT = 90;
    public final int BUTTON_HEIGHT = 20;
    public final int GAP = 5;

    protected CharacterStory characterStory;
    protected String name;
    protected Button actionButton, backButton;
    protected ScreenUtils.LabelBox nameBox;
    protected StringWidget titleWidget;
    protected Screen lastScreen;

    public EditInfoCharacterScreen(Screen lastScreen, CharacterStory characterStory) {
        super(Component.literal("Edit info"));
        this.name = characterStory != null ? characterStory.getName() : "";
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        Component title = Component.empty();
        int titleX = this.width / 2 - this.font.width(title) / 2;

        int labelHeight = this.font.lineHeight + 5;
        int centerX = this.width / 2 - WIDGET_WIDTH / 2;
        int centerY = this.height / 2 - ((labelHeight + (EDIT_BOX_NAME_HEIGHT + GAP)) + (BUTTON_HEIGHT * 2)) / 2;

        titleWidget = ScreenUtils.text(title, this.font, titleX, centerY - labelHeight);
        this.addRenderableWidget(titleWidget);

        Component nameLabel = Translation.message("global.name")
                .append(Component.literal(" *").withStyle(style -> style.withColor(ChatFormatting.RED)));

        nameBox = new ScreenUtils.LabelBox(
                nameLabel,
                font,
                WIDGET_WIDTH,
                EDIT_BOX_NAME_HEIGHT,
                centerX,
                centerY,
                ScreenUtils.Align.VERTICAL
        );
        nameBox.getEditBox().setValue(name);
        nameBox.getEditBox().setFilter(text -> !text.matches(".*[\\\\/:*?\"<>|].*"));
        this.addRenderableWidget(nameBox.getStringWidget());
        this.addRenderableWidget(nameBox.getEditBox());

        centerY += labelHeight + EDIT_BOX_NAME_HEIGHT + GAP;

        Component buttonActionMessage = characterStory == null ? Translation.message("global.add") : Translation.message("global.update");
        actionButton = Button.builder(buttonActionMessage, button -> {
            if(nameBox.getEditBox().getValue().isEmpty()) {
                ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.edit_info.name_must"));
            } else {
                minecraft.setScreen(new CharactersScreen());
            }
        }).bounds(centerX, centerY, WIDGET_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(actionButton);

        centerY += BUTTON_HEIGHT + GAP;

        backButton = Button.builder(CommonComponents.GUI_BACK, button -> {
            this.minecraft.setScreen(lastScreen);
        }).bounds(centerX, centerY, WIDGET_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(backButton);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }
}
