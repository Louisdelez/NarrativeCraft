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
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class MainScreenOptionsScreen extends OptionsSubScreen {

    private final NarrativeClientOption option = NarrativeCraftMod.getInstance().getNarrativeClientOptions();
    private final PlayerSession playerSession;
    private float textSpeed;
    private Checkbox autoSkipCheck;

    public MainScreenOptionsScreen(PlayerSession playerSession, Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal(""));
        this.playerSession = playerSession;
        textSpeed = option.textSpeed;
    }

    @Override
    public void onClose() {
        super.onClose();
        option.textSpeed = textSpeed;
        option.autoSkip = autoSkipCheck.selected();
        NarrativeCraftFile.updateUserOptions(option);
    }

    private double textSpeedToSlider(double textSpeed) {
        double visualMin = 0.1;
        double visualMax = 10.0;
        double internalMin = 0.05;
        double internalMax = 4.0;

        return visualMin + (internalMax - textSpeed) * (visualMax - visualMin) / (internalMax - internalMin);
    }

    @Override
    protected void addContents() {
        NarrativeClientOption clientOption = NarrativeCraftMod.getInstance().getNarrativeClientOptions();
        LinearLayout linearlayout =
                this.layout.addToContents(LinearLayout.vertical()).spacing(8);
        AbstractSliderButton abstractSliderButton =
                new AbstractSliderButton(
                        50,
                        20,
                        200,
                        20,
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

        linearlayout.addChild(abstractSliderButton);
        autoSkipCheck = Checkbox.builder(Translation.message("screen.main_screen.options.auto_skip"), minecraft.font)
                .selected(clientOption.autoSkip)
                .build();
        linearlayout.addChild(autoSkipCheck);

        linearlayout.addChild(Button.builder(Translation.message("screen.main_screen.minecraft_options"), button -> {
                    OptionsScreen screen = new OptionsScreen(this, minecraft.options);
                    minecraft.setScreen(screen);
                })
                .width(200)
                .build());

        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler == null || !storyHandler.isRunning()) {
            linearlayout.addChild(Button.builder(Component.literal("Credits"), button -> {
                        CreditScreen creditScreen = new CreditScreen(playerSession, true, false);
                        minecraft.setScreen(creditScreen);
                    })
                    .width(200)
                    .build());
        }
    }

    @Override
    protected void addOptions() {}
}
