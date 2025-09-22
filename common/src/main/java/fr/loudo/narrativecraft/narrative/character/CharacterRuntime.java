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

package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import net.minecraft.world.entity.LivingEntity;

public class CharacterRuntime {
    private final CharacterStory characterStory;
    private final CharacterSkinController characterSkinController;
    private LivingEntity entity;
    private String skinName;
    private String oldSkinName;

    public CharacterRuntime(CharacterStory characterStory, String skinName, LivingEntity entity, Scene scene) {
        this.characterStory = characterStory;
        this.entity = entity;
        characterSkinController = new CharacterSkinController(this, skinName, scene);
        characterSkinController.cacheSkins();
    }

    public CharacterStory getCharacterStory() {
        return characterStory;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public CharacterSkinController getCharacterSkinController() {
        return characterSkinController;
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        oldSkinName = this.skinName;
        this.skinName = skinName;
    }

    public String getOldSkinName() {
        return oldSkinName;
    }
}
