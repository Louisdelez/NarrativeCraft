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
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.narrative.character.MainCharacterAttribute;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * MC 1.19.x version of CharacterAdvancedScreen.
 * Key differences from 1.20.x+:
 * - Uses new Checkbox(x, y, width, height, message, selected) constructor instead of Checkbox.builder()
 * - No HeaderAndFooterLayout or LinearLayout.vertical() - uses manual layout
 * - No onValueChange callback - must manually track checkbox state changes
 */
public class CharacterAdvancedScreen extends Screen {

    private final Screen lastScreen;
    private final MainCharacterAttribute mainCharacterAttribute;
    private final CharacterStory characterStory;
    private Checkbox mainCharacterCheck;
    private Checkbox showNametagCheckBox;
    private Button skinShowBtn;

    public CharacterAdvancedScreen(Screen lastScreen, CharacterStory characterStory) {
        super(Component.literal("Character Advanced Screen"));
        this.lastScreen = lastScreen;
        this.characterStory = characterStory;
        this.mainCharacterAttribute = characterStory.getMainCharacterAttribute();
    }

    @Override
    public void onClose() {
        // 1.19.x: Manually save checkbox states on close
        if (mainCharacterAttribute != null && mainCharacterCheck != null) {
            mainCharacterAttribute.setMainCharacter(mainCharacterCheck.selected());
        }
        if (showNametagCheckBox != null) {
            characterStory.setShowNametag(showNametagCheckBox.selected());
        }
        minecraft.setScreen(lastScreen);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 50;
        int spacing = 25;
        int currentY = startY;

        if (mainCharacterAttribute != null && characterStory.getCharacterType() == CharacterType.MAIN) {
            Component skinShowBtnComponent;
            if (mainCharacterAttribute.isSameSkinAsTheir()) {
                skinShowBtnComponent = Translation.message("screen.character_advanced.player_same_skin_as_their");
            } else if (mainCharacterAttribute.isSameSkinAsPlayer()) {
                skinShowBtnComponent = Translation.message("screen.character_advanced.same_skin_as_player");
            } else {
                skinShowBtnComponent = Translation.message("screen.character_advanced.skin_from_folder");
            }
            skinShowBtn = Button.builder(skinShowBtnComponent, button -> {
                        Component newMode = button.getMessage();
                        if (newMode.equals(Translation.message("screen.character_advanced.skin_from_folder"))) {
                            button.setMessage(Translation.message("screen.character_advanced.same_skin_as_player"));
                            mainCharacterAttribute.setSameSkinAsPlayer(true);
                            mainCharacterAttribute.setSameSkinAsTheir(false);
                        } else if (newMode.equals(
                                Translation.message("screen.character_advanced.same_skin_as_player"))) {
                            button.setMessage(
                                    Translation.message("screen.character_advanced.player_same_skin_as_their"));
                            mainCharacterAttribute.setSameSkinAsPlayer(false);
                            mainCharacterAttribute.setSameSkinAsTheir(true);
                        } else {
                            button.setMessage(Translation.message("screen.character_advanced.skin_from_folder"));
                            mainCharacterAttribute.setSameSkinAsPlayer(false);
                            mainCharacterAttribute.setSameSkinAsTheir(false);
                        }
                    })
                    .bounds(centerX - 100, currentY, 200, 20)
                    .build();
            skinShowBtn.active = mainCharacterAttribute.isMainCharacter();
            this.addRenderableWidget(skinShowBtn);
            currentY += spacing;

            // 1.19.x: Use Checkbox constructor instead of Checkbox.builder()
            // Checkbox(x, y, width, height, message, selected)
            mainCharacterCheck = new Checkbox(
                    centerX - 100, currentY, 200, 20,
                    Translation.message("screen.character_advanced.main_character"),
                    mainCharacterAttribute.isMainCharacter());
            this.addRenderableWidget(mainCharacterCheck);
            currentY += spacing;
        }

        // 1.19.x: Use Checkbox constructor instead of Checkbox.builder()
        showNametagCheckBox = new Checkbox(
                centerX - 100, currentY, 200, 20,
                Translation.message("screen.character_advanced.show_nametag"),
                characterStory.showNametag());
        this.addRenderableWidget(showNametagCheckBox);

        // Done button at bottom
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(centerX - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, poseStack);
        this.renderBackground(poseStack);

        // Draw title centered at top
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void tick() {
        super.tick();
        // 1.19.x: Manually update skinShowBtn active state based on checkbox
        if (skinShowBtn != null && mainCharacterCheck != null && mainCharacterAttribute != null) {
            skinShowBtn.active = mainCharacterCheck.selected();
            mainCharacterAttribute.setMainCharacter(mainCharacterCheck.selected());
        }
        if (showNametagCheckBox != null) {
            characterStory.setShowNametag(showNametagCheckBox.selected());
        }
    }
}
