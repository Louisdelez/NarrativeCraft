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
import fr.loudo.narrativecraft.controllers.mainScreen.MainScreenController;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.data.MainScreenData;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.Translation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.util.Util;

public class StoryOptionsScreen extends StoryElementScreen {

    private final PlayerSession playerSession;

    public StoryOptionsScreen(PlayerSession playerSession) {
        super(Translation.message("screen.story_options.title"));
        this.playerSession = playerSession;
    }

    @Override
    protected void addTitle() {
        linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.addChild(Button.builder(ImageFontConstants.FOLDER, button -> {
                    openFolder();
                })
                .width(25)
                .build());
    }

    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.dataDirectory.toPath());
    }

    @Override
    protected void addContents() {

        List<StoryElementList.StoryEntryData> entries = new ArrayList<>();

        entries.add(new StoryElementList.StoryEntryData(
                Button.builder(Translation.message("screen.story_options.main_screen"), button -> {
                            NarrativeCraftMod.server.execute(() -> {
                                try {
                                    MainScreenData mainScreenData = NarrativeCraftFile.getMainScreenBackground();
                                    MainScreenController mainScreenController = new MainScreenController(
                                            Environment.DEVELOPMENT, minecraft.player, mainScreenData);
                                    mainScreenController.startSession();
                                } catch (IOException e) {
                                    fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                                }
                            });
                            onClose();
                        })
                        .build()));
        entries.add(new StoryElementList.StoryEntryData(
                Button.builder(Translation.message("screen.story_options.dialog"), button -> {
                            DialogCustomOptionsScreen dialogCustomScreen =
                                    new DialogCustomOptionsScreen(this, playerSession);
                            minecraft.setScreen(dialogCustomScreen);
                        })
                        .build()));

        entries.add(new StoryElementList.StoryEntryData(
                Button.builder(Translation.message("screen.story_options.world_options"), button -> {
                            WorldOptionsScreen screen = new WorldOptionsScreen(this);
                            minecraft.setScreen(screen);
                        })
                        .build()));

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }
}
