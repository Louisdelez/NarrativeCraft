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

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * MC 1.19.x version of WorldOptionsScreen.
 * Uses manual layout (no HeaderAndFooterLayout or LinearLayout.horizontal/vertical in 1.19.x).
 */
public class WorldOptionsScreen extends Screen {

    private final NarrativeWorldOption worldOption =
            NarrativeCraftMod.getInstance().getNarrativeWorldOption();
    private final Screen lastScreen;
    private StoryElementList storyElementList;

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
    protected void init() {
        // Create content list
        int headerHeight = 33;
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
        this.addWidget(this.storyElementList);

        // Done button at bottom
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
        this.renderBackground(poseStack);

        // Render list
        if (this.storyElementList != null) {
            this.storyElementList.render(poseStack, mouseX, mouseY, partialTick);
        }

        // Draw title centered at top
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        super.render(poseStack, mouseX, mouseY, partialTick);
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

    private Component yesOrNo(boolean b) {
        return b ? CommonComponents.GUI_YES : CommonComponents.GUI_NO;
    }
}
