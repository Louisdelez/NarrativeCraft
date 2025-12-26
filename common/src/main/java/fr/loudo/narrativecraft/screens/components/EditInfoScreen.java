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

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.screens.storyManager.EditScreenAdapter;
import fr.loudo.narrativecraft.util.ScreenUtils;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class EditInfoScreen<T extends NarrativeEntry> extends Screen {
    public final int WIDGET_WIDTH = 190;
    public final int EDIT_BOX_NAME_HEIGHT = 20;
    public final int EDIT_BOX_DESCRIPTION_HEIGHT = 90;
    public final int BUTTON_HEIGHT = 20;
    public final int GAP = 5;

    protected String name, description;
    protected Button actionButton, backButton;
    protected ScreenUtils.LabelBox nameBox;
    protected ScreenUtils.MultilineLabelBox descriptionBox;
    protected StringWidget titleWidget;
    protected Screen lastScreen;
    protected T narrativeEntry;
    protected EditScreenAdapter<T> adapter;

    public Map<String, Object> extraFields = new HashMap<>();

    public List<String> descriptionPlaceholders = List.of(
            "Once upon a time... In a wild... wild world...",
            "Until then is the reason this mod exists!",
            "Free palestine",
            "You should try narrative games",
            "Play Outer Wilds",
            "This screen was boring to code.",
            "I'm sure your creating a wonderful story!",
            "Your gender is valid!",
            "Easter egg message! No just kidding, not rare at all...",
            "Play Until Then",
            "Play Life Is Strange (NOT Double Exposure)",
            "Do these placeholders have a meaning anymore?",
            "Watch Frieren!",
            "Play Signalis",
            "The pain of your absence is sharp and haunting\nAnd i would give anything not to know it; anything but never knowing you at all, which would be worse",
            "The amazing digital circus is the best animation out there",
            "1, 2, 3, 4, 5, 6.... 7\nDid you get it??"
    );

    /**
     * Creates a new {@code EditInfoScreen}.
     * <p>
     * If {@code narrativeEntry} is {@code null}, a new entry will be created.
     * Otherwise, the given entry will be edited.
     * </p>
     *
     * @param lastScreen     the previous screen to return to after editing
     * @param narrativeEntry the narrative entry to edit, or {@code null} to create a new one
     * @param adapter        the adapter used to manage narrative data
     */
    public EditInfoScreen(Screen lastScreen, T narrativeEntry, EditScreenAdapter<T> adapter) {
        super(Component.literal("Edit info"));
        this.name = narrativeEntry != null ? narrativeEntry.getName() : "";
        this.description = narrativeEntry != null ? narrativeEntry.getDescription() : "";
        this.lastScreen = lastScreen;
        this.narrativeEntry = narrativeEntry;
        this.adapter = adapter;
    }

    @Override
    protected void init() {
        extraFields.clear();
        Component title = Component.empty();
        int titleX = this.width / 2 - this.font.width(title) / 2;

        int labelHeight = this.font.lineHeight + 5;
        int centerX = this.width / 2 - WIDGET_WIDTH / 2;
        int centerY = this.height / 2
                - (labelHeight
                                + (EDIT_BOX_NAME_HEIGHT + GAP)
                                + labelHeight
                                + EDIT_BOX_DESCRIPTION_HEIGHT
                                + (BUTTON_HEIGHT * 2))
                        / 2;

        if (adapter != null) {
            adapter.initExtraFields(this, narrativeEntry);
            for (Object object : extraFields.values()) {
                if (object instanceof AbstractWidget widget) {
                    centerY -= widget.getHeight() - GAP;
                }
            }
        }

        titleWidget = ScreenUtils.text(title, this.font, titleX, centerY - labelHeight);
        this.addRenderableWidget(titleWidget);

        Component nameLabel = Translation.message("global.name")
                .append(Component.literal(" *").withStyle(style -> style.withColor(ChatFormatting.RED)));

        nameBox = new ScreenUtils.LabelBox(
                nameLabel, font, WIDGET_WIDTH, EDIT_BOX_NAME_HEIGHT, centerX, centerY, ScreenUtils.Align.VERTICAL);
        nameBox.getEditBox().setValue(name);
        nameBox.getEditBox().setFilter(text -> text.matches(Util.REGEX_NO_SPECIAL_CHARACTERS));
        this.addRenderableWidget(nameBox.getStringWidget());
        this.addRenderableWidget(nameBox.getEditBox());

        centerY += labelHeight + EDIT_BOX_NAME_HEIGHT + GAP;

        descriptionBox = new ScreenUtils.MultilineLabelBox(
                Translation.message("global.description"),
                font,
                WIDGET_WIDTH,
                EDIT_BOX_DESCRIPTION_HEIGHT,
                centerX,
                centerY,
                Component.literal(
                        descriptionPlaceholders.get(new Random().nextInt(0, descriptionPlaceholders.size()))));
        descriptionBox.getMultiLineEditBox().setValue(description);

        this.addRenderableWidget(descriptionBox.getStringWidget());
        this.addRenderableWidget(descriptionBox.getMultiLineEditBox());

        centerY += labelHeight + EDIT_BOX_DESCRIPTION_HEIGHT + GAP;

        adapter.renderExtraFields(this, narrativeEntry, centerX, centerY);
        for (Object object : extraFields.values()) {
            if (object instanceof AbstractWidget widget) {
                centerY += widget.getHeight() + GAP;
            }
        }

        Component buttonActionMessage =
                narrativeEntry == null ? Translation.message("global.add") : Translation.message("global.update");
        actionButton = Button.builder(buttonActionMessage, button -> {
                    if (nameBox.getEditBox().getValue().isEmpty()) {
                        ScreenUtils.sendToast(
                                Translation.message("global.error"), Translation.message("screen.edit_info.name_must"));
                        return;
                    }
                    adapter.buildFromScreen(
                            this,
                            extraFields,
                            this.minecraft,
                            narrativeEntry,
                            nameBox.getEditBox().getValue(),
                            descriptionBox.getMultiLineEditBox().getValue());
                })
                .bounds(centerX, centerY, WIDGET_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(actionButton);

        centerY += BUTTON_HEIGHT + GAP;

        backButton = Button.builder(CommonComponents.GUI_BACK, button -> {
                    this.minecraft.setScreen(lastScreen);
                })
                .bounds(centerX, centerY, WIDGET_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(backButton);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ScreenUtils.LabelBox getNameBox() {
        return nameBox;
    }

    public ScreenUtils.MultilineLabelBox getDescriptionBox() {
        return descriptionBox;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }
}
