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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

public class CharacterEntityTypeScreen extends OptionsSubScreen {

    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

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
    protected void init() {
        addTitle();
        addContents();
        addFooter();
        layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
        super.init();
    }

    protected void addTitle() {
        layout.addToHeader(new StringWidget(this.title, this.font));
    }

    protected void addContents() {
        this.entityTypeList = new CharacterEntityTypeScreen.EntityTypeList(this.minecraft);
        this.addRenderableWidget(entityTypeList);
    }

    private void addFooter() {
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> {
                    onClose();
                    minecraft.setScreen(lastScreen);
                })
                .build());
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.entityTypeList.updateSize(
                this.width, this.height, this.layout.getHeaderHeight(), this.height - this.layout.getFooterHeight());
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

    class EntityTypeList extends ObjectSelectionList<CharacterEntityTypeScreen.EntityTypeList.Entry> {
        public EntityTypeList(Minecraft minecraft) {
            super(
                    minecraft,
                    CharacterEntityTypeScreen.this.width,
                    CharacterEntityTypeScreen.this.height,
                    32,
                    CharacterEntityTypeScreen.this.height - 35,
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
