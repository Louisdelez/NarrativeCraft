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

package fr.loudo.narrativecraft.screens.components;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrashScreen extends AbstractTextBoxScreen {

    private final String message;
    private final PlayerSession playerSession;

    public CrashScreen(PlayerSession playerSession, String message) {
        super(Translation.message("screen.crash.title"));
        this.message = message;
        this.playerSession = playerSession;
    }

    @Override
    public void onClose() {
        NarrativeWorldOption worldOption = NarrativeCraftMod.getInstance().getNarrativeWorldOption();
        if (worldOption.showMainScreen) {
            MainScreen mainScreen = new MainScreen(playerSession, false, false);
            minecraft.setScreen(mainScreen);
        } else {
            minecraft.setScreen(null);
        }
    }

    @Override
    protected List<String> renderContent() {
        List<String> lines = new ArrayList<>();

        lines.add(Translation.message("screen.crash.title").getString());

        String translatedMessage = Translation.message("screen.crash.message").getString();
        lines.addAll(Arrays.asList(translatedMessage.split("\n")));

        lines.add(message);

        return lines;
    }
}
