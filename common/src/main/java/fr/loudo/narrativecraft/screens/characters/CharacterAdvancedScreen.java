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

import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.narrative.character.MainCharacterAttribute;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class CharacterAdvancedScreen extends Screen {

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen lastScreen;
    private final MainCharacterAttribute mainCharacterAttribute;
    private final CharacterStory characterStory;
    private Checkbox mainCharacterCheck, showNametagCheckBox;

    public CharacterAdvancedScreen(Screen lastScreen, CharacterStory characterStory) {
        super(Component.literal("Character Advanced Screen"));
        this.lastScreen = lastScreen;
        this.characterStory = characterStory;
        this.mainCharacterAttribute = characterStory.getMainCharacterAttribute();
    }

    @Override
    public void onClose() {
        mainCharacterAttribute.setMainCharacter(mainCharacterCheck.selected());
        characterStory.setShowNametag(showNametagCheckBox.selected());
        minecraft.setScreen(lastScreen);
    }

    @Override
    protected void init() {
        GridLayout gridlayout = this.layout.addToHeader(new GridLayout()).spacing(4);
        gridlayout.defaultCellSetting().alignVerticallyMiddle();
        GridLayout.RowHelper rowHelper = gridlayout.createRowHelper(2);
        if (mainCharacterAttribute != null && characterStory.getCharacterType() == CharacterType.MAIN) {
            Component skinShowBtnComponent;
            if (mainCharacterAttribute.isSameSkinAsTheir()) {
                skinShowBtnComponent = Translation.message("screen.character_advanced.player_same_skin_as_their");
            } else if (mainCharacterAttribute.isSameSkinAsPlayer()) {
                skinShowBtnComponent = Translation.message("screen.character_advanced.same_skin_as_player");
            } else {
                skinShowBtnComponent = Translation.message("screen.character_advanced.skin_from_folder");
            }
            Button skinShowBtn = Button.builder(skinShowBtnComponent, button -> {
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
                    .width(200)
                    .build();
            skinShowBtn.active = mainCharacterAttribute.isMainCharacter();
            mainCharacterCheck = new Checkbox(
                    0,
                    0,
                    20,
                    20,
                    Translation.message("screen.character_advanced.main_character"),
                    mainCharacterAttribute.isMainCharacter());
            rowHelper.addChild(skinShowBtn);
            rowHelper.addChild(mainCharacterCheck);
        }
        showNametagCheckBox = new Checkbox(
                0,
                0,
                20,
                20,
                Translation.message("screen.character_advanced.show_nametag"),
                characterStory.showNametag());
        rowHelper.addChild(showNametagCheckBox);
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .width(200)
                .build());
        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }
}
