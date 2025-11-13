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

package fr.loudo.narrativecraft.narrative.chapter.scene.data.interaction;

import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.AreaTrigger;
import java.util.ArrayList;
import java.util.List;

public class Interaction extends SceneData {

    private List<CharacterInteraction> characterInteractions = new ArrayList<>();
    private List<EntityInteraction> entityInteractions = new ArrayList<>();
    private List<AreaTrigger> areaTriggers = new ArrayList<>();

    public Interaction(String name, String description, Scene scene) {
        super(name, description, scene);
    }

    public List<CharacterInteraction> getCharacterInteractions() {
        if (characterInteractions == null) {
            characterInteractions = new ArrayList<>();
        }
        return characterInteractions;
    }

    public void setCharacterInteractions(List<CharacterInteraction> characterInteractions) {
        this.characterInteractions = characterInteractions;
    }

    public List<EntityInteraction> getEntityInteractions() {
        if (entityInteractions == null) {
            entityInteractions = new ArrayList<>();
        }
        return entityInteractions;
    }

    public void setEntityInteractions(List<EntityInteraction> entityInteractions) {
        this.entityInteractions = entityInteractions;
    }

    public List<AreaTrigger> getAreaTriggers() {
        if (areaTriggers == null) {
            areaTriggers = new ArrayList<>();
        }
        return areaTriggers;
    }

    public void setAreaTriggers(List<AreaTrigger> areaTriggers) {
        this.areaTriggers = areaTriggers;
    }

    public boolean areaTriggerExists(String name) {
        for (AreaTrigger areaTrigger : areaTriggers) {
            if (areaTrigger.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
