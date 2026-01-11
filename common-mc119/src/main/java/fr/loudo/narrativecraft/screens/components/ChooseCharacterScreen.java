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

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
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
 * MC 1.19.x version of ChooseCharacterScreen.
 * Uses manual positioning (no HeaderAndFooterLayout/LinearLayout in 1.19.x).
 */
public class ChooseCharacterScreen extends Screen {
    private final List<CharacterStory> itemList;
    private final Consumer<CharacterStory> consumer;
    private final CharacterStory currentSelection;
    private final String screenTitle;
    private final Screen lastScreen;
    private final CharacterType characterType;
    private final Scene scene;

    private SelectionList selectionList;

    public ChooseCharacterScreen(
            Screen lastScreen,
            String screenTitle,
            List<CharacterStory> itemList,
            CharacterType characterType,
            CharacterStory currentSelection,
            Scene scene,
            Consumer<CharacterStory> consumer) {
        super(Component.literal(screenTitle));
        this.lastScreen = lastScreen;
        this.screenTitle = screenTitle;
        this.itemList = itemList;
        this.currentSelection = currentSelection;
        this.consumer = consumer;
        this.characterType = characterType;
        this.scene = scene;
    }

    public ChooseCharacterScreen(
            Screen lastScreen,
            String screenTitle,
            CharacterStory currentSelection,
            Scene scene,
            Consumer<CharacterStory> consumer) {
        this(
                lastScreen,
                screenTitle,
                NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories(),
                CharacterType.MAIN,
                currentSelection,
                scene,
                consumer);
    }

    @Override
    protected void init() {
        int headerHeight = 33;
        int footerHeight = 53;
        this.selectionList = new SelectionList(this.minecraft, this, headerHeight, footerHeight);
        this.addWidget(this.selectionList);

        // Add NPC/MAIN toggle button if scene is available
        if (scene != null) {
            this.addRenderableWidget(Button.builder(
                            characterType == CharacterType.NPC ? Component.literal("<- MAIN") : Component.literal("NPC ->"),
                            button -> {
                                Screen screen = new ChooseCharacterScreen(
                                        lastScreen,
                                        screenTitle,
                                        characterType == CharacterType.MAIN
                                                ? scene.getNpcs()
                                                : NarrativeCraftMod.getInstance()
                                                        .getCharacterManager()
                                                        .getCharacterStories(),
                                        characterType == CharacterType.MAIN ? CharacterType.NPC : CharacterType.MAIN,
                                        null,
                                        scene,
                                        consumer);
                                minecraft.setScreen(screen);
                            })
                    .bounds(this.width / 2 + 105, 6, 40, 20)
                    .build());
        }

        // Done button at the bottom
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
        this.renderBackground(poseStack);
        this.selectionList.render(poseStack, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        SelectionList.Entry entry = this.selectionList.getSelected();
        if (entry == null) {
            consumer.accept(null);
        } else {
            minecraft.setScreen(lastScreen);
            consumer.accept(entry.getItem());
        }
    }

    class SelectionList extends ObjectSelectionList<SelectionList.Entry> {
        private final ChooseCharacterScreen parentScreen;

        public SelectionList(Minecraft minecraft, ChooseCharacterScreen parentScreen, int headerHeight, int footerHeight) {
            // 1.19.x: ObjectSelectionList(minecraft, width, height, y0, y1, itemHeight)
            super(minecraft,
                    parentScreen.width,
                    parentScreen.height,
                    headerHeight,
                    parentScreen.height - footerHeight,
                    18);
            this.parentScreen = parentScreen;

            String selectedName = "";
            if (parentScreen.currentSelection != null) {
                selectedName = parentScreen.currentSelection.getName();
            }

            for (CharacterStory item : parentScreen.itemList) {
                Entry entry = new Entry(item);
                this.addEntry(entry);

                String itemName = item.getName();
                if (selectedName.equalsIgnoreCase(itemName)) {
                    this.setSelected(entry);
                }
            }

            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final CharacterStory item;

            public Entry(CharacterStory item) {
                this.item = item;
            }

            public CharacterStory getItem() {
                return item;
            }

            @Override
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                    int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                GuiGraphics guiGraphics = new GuiGraphics(parentScreen.minecraft, poseStack);
                String displayName = this.item.getName();
                guiGraphics.drawCenteredString(
                        parentScreen.font,
                        displayName,
                        SelectionList.this.width / 2,
                        top + height / 2 - 9 / 2,
                        -1);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
                    this.select();
                    parentScreen.onClose();
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
                SelectionList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                String displayName = this.item.getName();
                return Component.literal(displayName);
            }
        }
    }
}
