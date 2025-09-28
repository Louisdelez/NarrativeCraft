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

package fr.loudo.narrativecraft.gui;

import fr.loudo.narrativecraft.util.ImageFontConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class StorySaveIconGui {

    private final Minecraft minecraft = Minecraft.getInstance();

    private int currentTick, totalTickIn, totalTickStay, totalTickOut, totalTick;
    private State state = State.IN;
    private boolean saving;
    private boolean debugMode;

    public StorySaveIconGui(double in, double stay, double out) {
        totalTickIn = (int) (in * 20.0);
        totalTickStay = (int) (stay * 20.0);
        totalTickOut = (int) (out * 20.0);
        totalTick = totalTickIn;
    }

    public void tick() {
        if (!saving) return;
        if (currentTick < totalTick) {
            currentTick++;
        }
        if (currentTick == totalTick) {
            currentTick = 0;
            switch (state) {
                case IN -> {
                    totalTick = totalTickStay;
                    state = State.STAY;
                }
                case STAY -> {
                    totalTick = totalTickOut;
                    state = State.OUT;
                }
                case OUT -> saving = false;
            }
        }
    }

    public void showSave(boolean debugMode) {
        this.debugMode = debugMode;
        currentTick = 0;
        saving = true;
        state = State.IN;
    }

    public void render(GuiGraphics guiGraphics, float partialTick) {
        if (!saving) return;
        double t = Mth.clamp((currentTick + partialTick) / totalTick, 0.0, 1.0);
        double opacity = 1.0;
        switch (state) {
            case IN -> opacity = Mth.lerp(t, 0.0, 1.0);
            case OUT -> opacity = Mth.lerp(t, 1.0, 0.0);
        }
        String logo = ImageFontConstants.SAVE.getString();
        int logoWidth = minecraft.font.width(logo);
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        int x = width - logoWidth - 30;
        int y = height - 30;
        guiGraphics.drawString(
                minecraft.font, logo, x, y, FastColor.ARGB32.color((int) (opacity * 255.0), 0xFFFFFF), false);
        if (debugMode) {
            Component message = Component.literal("Fake save (debug)");
            guiGraphics.drawString(
                    minecraft.font,
                    message,
                    x - minecraft.font.width(message) / 2 - 5,
                    y - minecraft.font.lineHeight - 10,
                    FastColor.ARGB32.color((int) (opacity * 255.0), 0xFFFFFF),
                    false);
        }
    }

    enum State {
        IN,
        STAY,
        OUT
    }
}
