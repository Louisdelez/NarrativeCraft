package fr.loudo.narrativecraft.screens.storyManager;

import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public abstract class StoryElementScreen extends Screen {

    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    protected LinearLayout linearlayout;
    protected StoryElementList storyElementList;

    protected StoryElementScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        this.addTitle();
        this.addContents();
        this.addFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected void addTitle() {
        linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, p_345997_ -> this.onClose()).width(200).build());
    }

    protected abstract void addContents();

    protected void initAddButton(Button.OnPress onPress) {
        if(onPress == null) return;
        linearlayout.addChild(Button.builder(ImageFontConstants.ADD, onPress).width(25).build());
    }

    protected void initFolderButton() {
        linearlayout.addChild(Button.builder(ImageFontConstants.FOLDER, button -> {
            openFolder();
        }).width(25).build());
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.storyElementList.updateSize(this.width, this.layout);
    }

    protected abstract void openFolder();

}
