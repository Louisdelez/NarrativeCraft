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
import fr.loudo.narrativecraft.compat.api.IGuiRenderCompat;
import fr.loudo.narrativecraft.compat.api.NcId;
import fr.loudo.narrativecraft.compat.api.VersionAdapterLoader;
import fr.loudo.narrativecraft.util.Util;
import net.minecraft.client.gui.GuiGraphics;

public class NarrativeCraftLogoRenderer {

    public static final NcId LOGO =
            NcId.of(NarrativeCraftMod.MOD_ID, "textures/logo.png");

    private final NcId resourceLocation;
    private int[] logoRes;
    private int imageHeight;

    public NarrativeCraftLogoRenderer(NcId resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public void init() {
        logoRes = Util.getImageResolution(LOGO);
        if (logoRes != null) {
            imageHeight = Util.getDynamicHeight(logoRes, 256);
        }
    }

    public boolean logoExists() {
        return Util.resourceExists(resourceLocation);
    }

    public void render(GuiGraphics guiGraphics, int x, int y) {
        IGuiRenderCompat guiCompat = VersionAdapterLoader.getAdapter().getGuiRenderCompat();
        guiCompat.blitTexture(
                guiGraphics,
                LOGO.toString(),
                x,
                y,
                0,
                0,
                256,
                imageHeight,
                256,
                imageHeight);
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int[] getLogoRes() {
        return logoRes;
    }
}
