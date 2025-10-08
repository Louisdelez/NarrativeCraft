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

package fr.loudo.narrativecraft.screens.storyManager;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

/**
 * Dynamically create a new instance for a class that extends NarrativeEntry
 * After finishing putting info on EditScreen class
 * @param <T>
 */
public interface EditScreenAdapter<T extends NarrativeEntry> {

    Minecraft minecraft = Minecraft.getInstance();

    /**
     * Init extra fields to EditInfoScreen.
     * If you place horizontally, add only widget that has the biggest height.
     */
    void initExtraFields(EditInfoScreen<T> screen, T entry);

    /**
     * Render extra fields previously initiated to EditInfoScreen
     */
    void renderExtraFields(EditInfoScreen<T> screen, T entry, int x, int y);

    /**
     * Return the initial data after clicking done, and can retrieve extra features set from screen.
     * If it returns an existing one, then its to edit it.
     */
    void buildFromScreen(
            Screen screen,
            Map<String, Object> extraFields,
            Minecraft minecraft,
            @Nullable T existing,
            String name,
            String description);
}
