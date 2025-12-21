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

import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public abstract class StoryElementScreen extends Screen {

    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    protected LinearLayout linearlayout;
    protected StoryElementList storyElementList;

    protected StoryElementScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        this.addTitle();
        this.addContents();
        this.addFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected void addTitle() {
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, p_345997_ -> this.onClose())
                .width(200)
                .build());
    }

    protected abstract void addContents();

    protected void initAddButton(Button.OnPress onPress) {
        if (onPress == null) return;
        linearlayout.addChild(
                Button.builder(ImageFontConstants.ADD, onPress).width(25).build());
    }

    protected void initFolderButton() {
        if (!minecraft.isSingleplayer()) return;
        linearlayout.addChild(Button.builder(ImageFontConstants.FOLDER, button -> {
                    openFolder();
                })
                .width(25)
                .build());
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.storyElementList.updateSize(this.width, this.layout);
    }

    protected abstract void openFolder();
}
