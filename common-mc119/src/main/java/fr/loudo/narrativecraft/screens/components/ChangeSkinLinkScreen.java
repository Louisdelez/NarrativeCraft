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

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.util.Translation;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * MC 1.19.x version of ChangeSkinLinkScreen.
 * Uses Screen directly (OptionsSubScreen doesn't exist in 1.19.x).
 * ObjectSelectionList uses 6-param constructor.
 * Entry.render uses PoseStack instead of GuiGraphics.
 */
public class ChangeSkinLinkScreen extends Screen {
    private SkinList skinList;
    private final CharacterRuntime characterRuntime;
    private final Consumer<String> stringCallback;
    private final Screen lastScreen;

    public ChangeSkinLinkScreen(Screen lastScreen, CharacterRuntime characterRuntime, Consumer<String> stringCallback) {
        super(Translation.message(
                "screen.change_skin_link.title",
                characterRuntime.getCharacterStory().getName()));
        this.lastScreen = lastScreen;
        this.characterRuntime = characterRuntime;
        this.stringCallback = stringCallback;
    }

    @Override
    protected void init() {
        int headerHeight = 33;
        int footerHeight = 53;
        this.skinList = new SkinList(
                this.minecraft,
                characterRuntime.getCharacterSkinController().getSkins(),
                headerHeight,
                footerHeight);
        this.addWidget(this.skinList);

        // Done button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
        this.renderBackground(poseStack);
        this.skinList.render(poseStack, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        SkinList.Entry entry = this.skinList.getSelected();
        if (entry != null) {
            File selectedSkin = entry.skin;
            characterRuntime.getCharacterSkinController().setCurrentSkin(selectedSkin);
            handleSkin(selectedSkin.getName());
        }
        minecraft.setScreen(lastScreen);
    }

    private void handleSkin(String skin) {
        stringCallback.accept(skin);
    }

    class SkinList extends ObjectSelectionList<SkinList.Entry> {
        public SkinList(Minecraft minecraft, List<File> skins, int headerHeight, int footerHeight) {
            // 1.19.x: ObjectSelectionList(minecraft, width, height, y0, y1, itemHeight)
            super(minecraft,
                    ChangeSkinLinkScreen.this.width,
                    ChangeSkinLinkScreen.this.height,
                    headerHeight,
                    ChangeSkinLinkScreen.this.height - footerHeight,
                    18);
            String selectedSkin;
            if (characterRuntime.getCharacterSkinController().getCurrentSkin() != null) {
                selectedSkin = characterRuntime
                        .getCharacterSkinController()
                        .getCurrentSkin()
                        .getName();
            } else {
                selectedSkin = "";
            }
            skins.forEach(file -> {
                Entry entry = new Entry(file);
                this.addEntry(entry);
                if (selectedSkin.equalsIgnoreCase(file.getName())) {
                    this.setSelected(entry);
                }
            });
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final File skin;

            public Entry(File skin) {
                this.skin = skin;
            }

            @Override
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                    int mouseX, int mouseY, boolean hovering, float partialTick) {
                GuiGraphics guiGraphics = new GuiGraphics(ChangeSkinLinkScreen.this.minecraft, poseStack);
                guiGraphics.drawCenteredString(
                        ChangeSkinLinkScreen.this.font,
                        this.skin.getName().split("\\.")[0],
                        SkinList.this.width / 2,
                        top + height / 2 - 9 / 2,
                        -1);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == 257 || keyCode == 335) { // Enter or NumPad Enter
                    this.select();
                    ChangeSkinLinkScreen.this.onClose();
                    return true;
                } else {
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.select();
                return super.mouseClicked(mouseX, mouseY, button);
            }

            private void select() {
                SkinList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.literal(skin.getName());
            }
        }
    }
}
