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

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

/**
 * MC 1.19.x version of CharacterEntityTypeScreen.
 * Uses Screen directly (OptionsSubScreen doesn't exist in 1.19.x).
 * ObjectSelectionList uses 6-param constructor.
 * Entry.render uses PoseStack instead of GuiGraphics.
 */
public class CharacterEntityTypeScreen extends Screen {

    private EntityTypeList entityTypeList;
    private final List<EntityType<?>> entityTypes;
    private final CharacterStory characterStory;
    private final Scene scene;
    private final Screen lastScreen;

    public CharacterEntityTypeScreen(Screen lastScreen, CharacterStory characterStory, Scene scene) {
        super(Translation.message("screen.character.change_entity_type"));
        this.lastScreen = lastScreen;
        this.entityTypes = NarrativeCraftMod.getInstance().getCharacterManager().getAvailableEntityTypes();
        this.characterStory = characterStory;
        this.scene = scene;
    }

    @Override
    protected void init() {
        int headerHeight = 33;
        int footerHeight = 53;
        this.entityTypeList = new EntityTypeList(this.minecraft, headerHeight, footerHeight);
        this.addWidget(this.entityTypeList);

        // Done button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
        this.renderBackground(poseStack);
        this.entityTypeList.render(poseStack, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
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
        public EntityTypeList(Minecraft minecraft, int headerHeight, int footerHeight) {
            // 1.19.x: ObjectSelectionList(minecraft, width, height, y0, y1, itemHeight)
            super(
                    minecraft,
                    CharacterEntityTypeScreen.this.width,
                    CharacterEntityTypeScreen.this.height,
                    headerHeight,
                    CharacterEntityTypeScreen.this.height - footerHeight,
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
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                    int mouseX, int mouseY, boolean hovering, float partialTick) {
                GuiGraphics guiGraphics = new GuiGraphics(CharacterEntityTypeScreen.this.minecraft, poseStack);
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
