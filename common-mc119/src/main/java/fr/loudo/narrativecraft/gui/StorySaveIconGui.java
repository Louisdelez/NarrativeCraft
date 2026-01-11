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

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * MC 1.19.x version of StorySaveIconGui.
 * Uses PoseStack and font.draw() instead of GuiGraphics.
 */
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

    public void render(PoseStack poseStack, float partialTick) {
        if (!saving) return;
        double t = Mth.clamp((currentTick + partialTick) / totalTick, 0.0, 1.0);
        double opacity = 1.0;
        switch (state) {
            case IN -> opacity = Mth.lerp(t, 0.0, 1.0);
            case OUT -> opacity = Mth.lerp(t, 1.0, 0.0);
        }
        String logo = ImageFontConstants.SAVE.getString();
        int logoWidth = minecraft.font.width(logo);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        int x = width - logoWidth - 30;
        int y = height - 30;
        // 1.19.x: Use font.draw() with PoseStack instead of guiGraphics.drawString()
        minecraft.font.draw(poseStack, logo, x, y,
                NarrativeCraftMod.getColorCompat().color((int) (opacity * 255.0), 0xFFFFFF));
        if (debugMode) {
            Component message = Component.literal("Fake save (debug)");
            minecraft.font.draw(poseStack, message,
                    x - minecraft.font.width(message) / 2f - 5,
                    y - minecraft.font.lineHeight - 10,
                    NarrativeCraftMod.getColorCompat().color((int) (opacity * 255.0), 0xFFFFFF));
        }
    }

    private enum State {
        IN,
        STAY,
        OUT
    }
}
