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

package fr.loudo.narrativecraft.screens.animation;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.screens.components.GenericSelectionScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AnimationCharacterLinkScreen extends GenericSelectionScreen<CharacterStory> {
    private final CharacterType characterType;
    private final Animation animation;

    public AnimationCharacterLinkScreen(
            Screen lastScreen,
            Animation animation,
            List<CharacterStory> characterStoryList,
            CharacterType characterType,
            Consumer<CharacterStory> consumer) {
        super(
                lastScreen,
                Translation.message("screen.story_manager.link_animation_character")
                        .getString(),
                characterStoryList,
                animation.getCharacter(),
                consumer);
        this.characterType = characterType;
        this.animation = animation;
    }

    public AnimationCharacterLinkScreen(Screen lastScreen, Animation animation, Consumer<CharacterStory> consumer) {
        super(
                lastScreen,
                Translation.message("screen.story_manager.link_animation_character")
                        .getString(),
                NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories(),
                animation.getCharacter(),
                consumer);
        this.characterType = CharacterType.MAIN;
        this.animation = animation;
    }

    @Override
    protected void addCustomTitleButtons(LinearLayout layout) {
        layout.addChild(Button.builder(
                        characterType == CharacterType.NPC ? Component.literal("<- MAIN") : Component.literal("NPC ->"),
                        button -> {
                            Screen screen;
                            if (characterType == CharacterType.MAIN) {
                                screen = new AnimationCharacterLinkScreen(
                                        lastScreen,
                                        animation,
                                        animation.getScene().getNpcs(),
                                        CharacterType.NPC,
                                        consumer);
                            } else {
                                screen = new AnimationCharacterLinkScreen(
                                        lastScreen,
                                        animation,
                                        NarrativeCraftMod.getInstance()
                                                .getCharacterManager()
                                                .getCharacterStories(),
                                        CharacterType.MAIN,
                                        consumer);
                            }
                            minecraft.setScreen(screen);
                        })
                .width(40)
                .build());
    }
}
