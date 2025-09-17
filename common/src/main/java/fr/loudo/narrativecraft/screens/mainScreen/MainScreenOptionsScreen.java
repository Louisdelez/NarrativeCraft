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
    private int textSpeed;
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
                                String.format(Locale.US, "%.2f", clientOption.textSpeed / 1000.0)),
                        (400.0 - clientOption.textSpeed) / 400.0) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Translation.message(
                                "screen.main_screen.options.dialog_speed",
                                String.format("%.2f", (400 - (this.value * 400)) / 1000.0)));
                    }

                    @Override
                    protected void applyValue() {
                        textSpeed = (int) (400 - (this.value * 400));
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
