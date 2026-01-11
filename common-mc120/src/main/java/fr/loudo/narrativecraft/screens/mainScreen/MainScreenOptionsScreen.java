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

package fr.loudo.narrativecraft.screens.mainScreen;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.options.NarrativeClientOption;
import fr.loudo.narrativecraft.screens.credits.CreditScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * MC 1.20.x version of MainScreenOptionsScreen.
 * Uses Screen directly instead of OptionsSubScreen (API differs significantly).
 * Checkbox API differs: no builder() method in 1.20.x.
 */
public class MainScreenOptionsScreen extends Screen {

    private final NarrativeClientOption option = NarrativeCraftMod.getInstance().getNarrativeClientOptions();
    private final PlayerSession playerSession;
    private final Screen lastScreen;
    private float textSpeed;
    private Checkbox autoSkipCheck;

    public MainScreenOptionsScreen(PlayerSession playerSession, Screen lastScreen) {
        super(Component.literal("Options"));
        this.playerSession = playerSession;
        this.lastScreen = lastScreen;
        textSpeed = option.textSpeed;
    }

    @Override
    public void onClose() {
        option.textSpeed = textSpeed;
        if (playerSession.getDialogRenderer() != null) {
            playerSession.getDialogRenderer().getDialogScrollText().setTextSpeed(textSpeed);
        }
        // 1.20.x: selected() method exists on Checkbox
        option.autoSkip = autoSkipCheck.selected();
        NarrativeCraftFile.updateUserOptions(option);
        minecraft.setScreen(lastScreen);
    }

    private double textSpeedToSlider(double textSpeed) {
        double visualMin = 0.1;
        double visualMax = 10.0;
        double internalMin = 0.05;
        double internalMax = 4.0;

        return visualMin + (internalMax - textSpeed) * (visualMax - visualMin) / (internalMax - internalMin);
    }

    @Override
    protected void init() {
        NarrativeClientOption clientOption = NarrativeCraftMod.getInstance().getNarrativeClientOptions();

        int centerX = this.width / 2;
        int startY = this.height / 4;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 24;

        // Slider for dialog speed
        AbstractSliderButton sliderButton = new AbstractSliderButton(
                centerX - buttonWidth / 2,
                startY,
                buttonWidth,
                buttonHeight,
                Translation.message(
                        "screen.main_screen.options.dialog_speed",
                        String.format(Locale.US, "%.2f", textSpeedToSlider(clientOption.textSpeed))),
                textSpeedToSlider(clientOption.textSpeed) / 10.0) {
            @Override
            protected void updateMessage() {
                double visualSpeed = 0.1 + this.value * (10.0 - 0.1);
                this.setMessage(Translation.message(
                        "screen.main_screen.options.dialog_speed", String.format("%.2f", visualSpeed)));
            }

            @Override
            protected void applyValue() {
                double visualSpeed = 0.1 + this.value * (10.0 - 0.1);
                double internalSpeed = 0.05 + (10.0 - visualSpeed) / 9.9 * (4.0 - 0.05);
                textSpeed = (float) internalSpeed;
            }
        };
        this.addRenderableWidget(sliderButton);

        // 1.20.x Checkbox API: Checkbox(x, y, message, selected) - no width/height params
        // Width is computed from the message, height is fixed
        autoSkipCheck = Checkbox.builder(
                Translation.message("screen.main_screen.options.auto_skip"),
                this.font)
                .pos(centerX - buttonWidth / 2, startY + spacing)
                .selected(clientOption.autoSkip)
                .build();
        this.addRenderableWidget(autoSkipCheck);

        // Minecraft options button
        this.addRenderableWidget(Button.builder(
                Translation.message("screen.main_screen.minecraft_options"),
                button -> {
                    OptionsScreen screen = new OptionsScreen(this, minecraft.options);
                    minecraft.setScreen(screen);
                })
                .bounds(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight)
                .build());

        // Credits button (if no story running)
        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler == null || !storyHandler.isRunning()) {
            this.addRenderableWidget(Button.builder(
                    Component.literal("Credits"),
                    button -> {
                        CreditScreen creditScreen = new CreditScreen(playerSession, true, false);
                        minecraft.setScreen(creditScreen);
                    })
                    .bounds(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight)
                    .build());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1.20.x: renderBackground takes additional parameters
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
