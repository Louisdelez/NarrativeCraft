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
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

public class CharacterEntityTypeScreen extends OptionsSubScreen {

    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    private EntityTypeList entityTypeList;
    private final List<EntityType<?>> entityTypes;
    private final CharacterStory characterStory;

    public CharacterEntityTypeScreen(Screen lastScreen, CharacterStory characterStory) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal("Change Character Entity Type"));
        this.entityTypes = NarrativeCraftMod.getInstance().getCharacterManager().getAvailableEntityTypes();
        this.characterStory = characterStory;
    }

    @Override
    protected void init() {
        addTitle();
        addContents();
    }

    protected void addTitle() {
        GridLayout gridlayout = new GridLayout();
        GridLayout.RowHelper rowHelper = gridlayout.createRowHelper(1);
        LinearLayout linearlayout = this.layout.addToHeader(new LinearLayout(200, 20, LinearLayout.Orientation.HORIZONTAL), rowHelper.newCellSettings().paddingLeft(8));
        linearlayout.defaultChildLayoutSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
    }

    protected void addContents() {
        this.entityTypeList = this.layout.addToContents(new CharacterEntityTypeScreen.EntityTypeList(this.minecraft));
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        this.entityTypeList.updateSize(this.width, this.height, this.layout.getX(), this.layout.getY());
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
        characterStory.updateEntityType(entityType);
    }

    class EntityTypeList extends ObjectSelectionList<CharacterEntityTypeScreen.EntityTypeList.Entry> implements LayoutElement {
        public EntityTypeList(Minecraft minecraft) {
            super(
                    minecraft,
                    CharacterEntityTypeScreen.this.width,
                    CharacterEntityTypeScreen.this.height - 33 - 53,
                    33,
                    18
            ,18);
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

        @Override
        public void setX(int x0) {
            this.x0 = x0;
        }

        @Override
        public void setY(int y0) {
            this.y0 = y0;
        }

        @Override
        public int getX() {
            return x0;
        }

        @Override
        public int getY() {
            return y0;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> consumer) {

        }

        public class Entry extends ObjectSelectionList.Entry<CharacterEntityTypeScreen.EntityTypeList.Entry> {
            private final EntityType<?> entityType;

            public Entry(EntityType<?> entityType) {
                this.entityType = entityType;
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
                        CharacterEntityTypeScreen.this.font,
                        this.entityType.getDescription().getString().toUpperCase(),
                        CharacterEntityTypeScreen.EntityTypeList.this.width / 2,
                        p_345328_ + p_345185_ / 2 - 4,
                        -1);
            }

            public boolean keyPressed(int p_346403_, int p_345881_, int p_345858_) {
                if (CommonInputs.selected(p_346403_)) {
                    this.select();
                    CharacterEntityTypeScreen.this.onClose();
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
                CharacterEntityTypeScreen.EntityTypeList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.literal(characterStory.getName());
            }
        }
    }
}
