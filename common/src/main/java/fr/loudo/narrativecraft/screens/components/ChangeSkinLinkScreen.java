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

import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.util.Translation;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class ChangeSkinLinkScreen extends OptionsSubScreen {
    private SkinList skinList;
    private final CharacterRuntime characterRuntime;
    private final Consumer<String> stringCallback;

    public ChangeSkinLinkScreen(CharacterRuntime characterRuntime, Consumer<String> stringCallback) {
        super(
                null,
                Minecraft.getInstance().options,
                Translation.message(
                        "screen.change_skin_link.title",
                        characterRuntime.getCharacterStory().getName()));
        this.characterRuntime = characterRuntime;
        this.stringCallback = stringCallback;
    }

    public ChangeSkinLinkScreen(Screen lastScreen, CharacterRuntime characterRuntime, Consumer<String> stringCallback) {
        super(
                lastScreen,
                Minecraft.getInstance().options,
                Translation.message(
                        "screen.change_skin_link.title",
                        characterRuntime.getCharacterStory().getName()));
        this.characterRuntime = characterRuntime;
        this.stringCallback = stringCallback;
    }

    protected void addContents() {
        this.skinList = this.layout.addToContents(new SkinList(
                this.minecraft, characterRuntime.getCharacterSkinController().getSkins()));
    }

    protected void addOptions() {}

    protected void repositionElements() {
        super.repositionElements();
        this.skinList.updateSize(this.width, this.layout);
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
        public SkinList(Minecraft minecraft, List<File> skins) {
            super(minecraft, ChangeSkinLinkScreen.this.width, ChangeSkinLinkScreen.this.height - 33 - 53, 33, 18);
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

            public void render(
                    GuiGraphics p_345300_,
                    int p_345469_,
                    int p_345328_,
                    int p_345700_,
                    int p_345311_,
                    int p_345185_,
                    int p_344805_,
                    int p_345963_,
                    boolean p_345912_,
                    float p_346091_) {
                p_345300_.drawCenteredString(
                        ChangeSkinLinkScreen.this.font,
                        this.skin.getName().split("\\.")[0],
                        SkinList.this.width / 2,
                        p_345328_ + p_345185_ / 2 - 4,
                        -1);
            }

            public boolean keyPressed(int p_346403_, int p_345881_, int p_345858_) {
                if (CommonInputs.selected(p_346403_)) {
                    this.select();
                    ChangeSkinLinkScreen.this.onClose();
                    return true;
                } else {
                    return super.keyPressed(p_346403_, p_345881_, p_345858_);
                }
            }

            public boolean mouseClicked(double p_344965_, double p_345385_, int p_345080_) {
                this.select();
                return super.mouseClicked(p_344965_, p_345385_, p_345080_);
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
