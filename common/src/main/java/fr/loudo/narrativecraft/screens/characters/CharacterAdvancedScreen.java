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

import fr.loudo.narrativecraft.narrative.character.MainCharacterAttribute;
import fr.loudo.narrativecraft.util.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class CharacterAdvancedScreen extends Screen {

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen lastScreen;
    private final MainCharacterAttribute mainCharacterAttribute;

    public CharacterAdvancedScreen(Screen lastScreen, MainCharacterAttribute mainCharacterAttribute) {
        super(Component.literal("Character Advanced Screen"));
        this.lastScreen = lastScreen;
        this.mainCharacterAttribute = mainCharacterAttribute;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }

    @Override
    protected void init() {
        LinearLayout linearLayout = layout.addToContents(LinearLayout.vertical().spacing(5));
        Checkbox mainCharacterCheck = Checkbox.builder(
                        Translation.message("screen.character_advanced.main_character"), minecraft.font)
                .onValueChange((checkbox, b) -> {
                    mainCharacterAttribute.setMainCharacter(b);
                })
                .selected(mainCharacterAttribute.isMainCharacter())
                .build();
        Checkbox sameSkinAsPlayerCheck = Checkbox.builder(
                        Translation.message("screen.character_advanced.same_skin_as_player"), minecraft.font)
                .onValueChange((checkbox, b) -> {
                    mainCharacterAttribute.setSameSkinAsPlayer(b);
                })
                .selected(mainCharacterAttribute.isSameSkinAsPlayer())
                .build();
        Checkbox playerSameSkinAsTheir = Checkbox.builder(
                        Translation.message("screen.character_advanced.player_same_skin_as_their"), minecraft.font)
                .onValueChange((checkbox, b) -> {
                    mainCharacterAttribute.setSameSkinAsTheir(b);
                })
                .selected(mainCharacterAttribute.isSameSkinAsTheir())
                .build();
        linearLayout.addChild(mainCharacterCheck);
        linearLayout.addChild(sameSkinAsPlayerCheck);
        linearLayout.addChild(playerSameSkinAsTheir);
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
