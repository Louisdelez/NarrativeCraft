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

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.util.Util;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public class CharacterStory extends NarrativeEntry {

    private transient EntityType<?> entityType;

    private String birthDate;
    private CharacterType characterType;
    private PlayerSkin.Model model;
    private int entityTypeId;

    public CharacterStory(
            String name,
            String description,
            String day,
            String month,
            String year,
            PlayerSkin.Model model,
            CharacterType characterType) {
        super(name, description);
        this.birthDate = day + "/" + month + "/" + year;
        this.characterType = characterType;
        this.model = model;
        this.entityType = EntityType.PLAYER;
    }

    public void updateEntityType(EntityType<?> entityType) {
        EntityType<?> oldEntityType = this.entityType;
        int oldEntityTypeId = entityTypeId;
        try {
            this.entityType = entityType;
            entityTypeId = BuiltInRegistries.ENTITY_TYPE.getId(entityType);
            NarrativeCraftFile.updateCharacterData(this, this);
        } catch (IOException e) {
            this.entityType = oldEntityType;
            entityTypeId = oldEntityTypeId;
            Util.sendCrashMessage(Minecraft.getInstance().player, e);
            Minecraft.getInstance().setScreen(null);
        }
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }

    public PlayerSkin.Model getModel() {
        return model;
    }

    public void setModel(PlayerSkin.Model model) {
        this.model = model;
    }

    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    public int getEntityTypeId() {
        return entityTypeId;
    }

    public EntityType<?> getEntityType() {
        if (entityType == null) {
            entityType = EntityType.PLAYER;
        }
        return entityType;
    }
}
