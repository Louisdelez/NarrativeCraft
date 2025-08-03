package fr.loudo.narrativecraft.screens.options;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class WorldOptionsScreen extends StoryElementScreen {

    public WorldOptionsScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Translation.message("screen.world_options.title"));
    }

    @Override
    public void onClose() {
        NarrativeWorldOption worldOption = NarrativeCraftMod.getInstance().getNarrativeWorldOption();
        NarrativeCraftFile.updateWorldOptions(worldOption);
        super.onClose();
    }

    @Override
    protected void addTitle() {
        linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
    }

    @Override
    protected void addContents() {
        NarrativeWorldOption worldOption = NarrativeCraftMod.getInstance().getNarrativeWorldOption();

        List<StoryElementList.StoryEntryData> entries = List.of(
                createToggleButton(
                        () -> worldOption.finishedStory,
                        val -> worldOption.finishedStory = val,
                        "screen.world_options.finished_story"
                ),
                createToggleButton(
                        () -> worldOption.showMainScreen,
                        val -> worldOption.showMainScreen = val,
                        "screen.world_options.show_main_screen"
                ),
                createToggleButton(
                        () -> worldOption.showCreditsScreen,
                        val -> worldOption.showCreditsScreen = val,
                        "screen.world_options.show_credits_screen"
                )
        );

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    private StoryElementList.StoryEntryData createToggleButton(BooleanSupplier getter, Consumer<Boolean> setter, String translationKey) {
        Button button = Button.builder(Translation.message(translationKey, yesOrNo(getter.getAsBoolean())), b -> {
            boolean newValue = !getter.getAsBoolean();
            setter.accept(newValue);
            b.setMessage(Translation.message(translationKey, yesOrNo(newValue)));
        }).build();

        return new StoryElementList.StoryEntryData(button);
    }


    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (p_345997_) -> this.onClose()).width(200).build());
    }

    @Override
    protected void openFolder() {}

    private Component yesOrNo(boolean b) {
        return b ? CommonComponents.GUI_YES : CommonComponents.GUI_NO;
    }
}
