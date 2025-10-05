/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.screens.options;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class WorldOptionsScreen extends StoryElementScreen {

    private final NarrativeWorldOption worldOption =
            NarrativeCraftMod.getInstance().getNarrativeWorldOption();
    private final Screen lastScreen;

    public WorldOptionsScreen(Screen lastScreen) {
        super(Translation.message("screen.world_options.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    public void onClose() {
        NarrativeCraftFile.updateWorldOptions(worldOption);
        minecraft.setScreen(lastScreen);
    }

    @Override
    protected void addTitle() {
        layout.addToHeader(new StringWidget(this.title, this.font));
    }

    @Override
    protected void addContents() {

        List<StoryElementList.StoryEntryData> entries = List.of(
                createToggleButton(
                        () -> worldOption.finishedStory,
                        val -> worldOption.finishedStory = val,
                        "screen.world_options.finished_story"),
                createToggleButton(
                        () -> worldOption.showMainScreen,
                        val -> worldOption.showMainScreen = val,
                        "screen.world_options.show_main_screen"),
                createToggleButton(
                        () -> worldOption.showCreditsScreen,
                        val -> worldOption.showCreditsScreen = val,
                        "screen.world_options.show_credits_screen"));

        this.storyElementList = new StoryElementList(this.minecraft, this, entries, true);
    }

    private StoryElementList.StoryEntryData createToggleButton(
            BooleanSupplier getter, Consumer<Boolean> setter, String translationKey) {
        Button button = Button.builder(Translation.message(translationKey, yesOrNo(getter.getAsBoolean())), b -> {
                    boolean newValue = !getter.getAsBoolean();
                    setter.accept(newValue);
                    b.setMessage(Translation.message(translationKey, yesOrNo(newValue)));
                })
                .build();

        return new StoryElementList.StoryEntryData(button);
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (p_345997_) -> this.onClose())
                .width(200)
                .build());
    }

    @Override
    protected void openFolder() {}

    private Component yesOrNo(boolean b) {
        return b ? CommonComponents.GUI_YES : CommonComponents.GUI_NO;
    }
}
