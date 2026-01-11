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

package fr.loudo.narrativecraft.screens.characters;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

/**
 * MC 1.20.x version of CharacterEntityTypeScreen.
 * Uses 1.20.x OptionsSubScreen path and primitive event parameters.
 */
public class CharacterEntityTypeScreen extends OptionsSubScreen {

    private EntityTypeList entityTypeList;
    private final List<EntityType<?>> entityTypes;
    private final CharacterStory characterStory;
    private final Scene scene;

    public CharacterEntityTypeScreen(Screen lastScreen, CharacterStory characterStory, Scene scene) {
        super(lastScreen, Minecraft.getInstance().options, Translation.message("screen.character.change_entity_type"));
        this.entityTypes = NarrativeCraftMod.getInstance().getCharacterManager().getAvailableEntityTypes();
        this.characterStory = characterStory;
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout =
                this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
    }

    protected void addContents() {
        this.entityTypeList = this.layout.addToContents(new CharacterEntityTypeScreen.EntityTypeList(this.minecraft));
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        this.entityTypeList.updateSize(this.width, this.layout);
    }

    @Override
    public void onClose() {
        EntityTypeList.Entry entry = entityTypeList.getSelected();
        if (entry == null) {
            minecraft.setScreen(null);
            return;
        }
        EntityType<?> entityType = entry.entityType;
        minecraft.setScreen(lastScreen);
        characterStory.updateEntityType(entityType, scene);
    }

    // 1.20.x: addOptions() doesn't exist in OptionsSubScreen, removed override

    class EntityTypeList extends ObjectSelectionList<CharacterEntityTypeScreen.EntityTypeList.Entry> {
        public EntityTypeList(Minecraft minecraft) {
            super(
                    minecraft,
                    CharacterEntityTypeScreen.this.width,
                    CharacterEntityTypeScreen.this.height - 33 - 53,
                    33,
                    18);
            int selectedEntityTypeId = characterStory.getEntityTypeId();
            entityTypes.forEach(entityType -> {
                CharacterEntityTypeScreen.EntityTypeList.Entry entry =
                        new CharacterEntityTypeScreen.EntityTypeList.Entry(entityType);
                this.addEntry(entry);
                if (selectedEntityTypeId == BuiltInRegistries.ENTITY_TYPE.getId(entityType)) {
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

        public class Entry extends ObjectSelectionList.Entry<CharacterEntityTypeScreen.EntityTypeList.Entry> {
            private final EntityType<?> entityType;

            public Entry(EntityType<?> entityType) {
                this.entityType = entityType;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                    int mouseX, int mouseY, boolean hovering, float partialTick) {
                guiGraphics.drawCenteredString(
                        CharacterEntityTypeScreen.this.font,
                        this.entityType.getDescription().getString().toUpperCase(),
                        CharacterEntityTypeScreen.EntityTypeList.this.width / 2,
                        top + height / 2 - 9 / 2,
                        -1);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == 257 || keyCode == 335) { // Enter or NumPad Enter
                    this.select();
                    CharacterEntityTypeScreen.this.onClose();
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
                CharacterEntityTypeScreen.EntityTypeList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.literal(characterStory.getName());
            }
        }
    }
}
