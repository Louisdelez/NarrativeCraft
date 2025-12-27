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
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import java.io.IOException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public class CharacterStory extends NarrativeEntry {

    private transient EntityType<?> entityType;

    private MainCharacterAttribute mainCharacterAttribute = new MainCharacterAttribute();
    private String birthDate;
    private CharacterType characterType;
    private CharacterModel model;
    private boolean showNametag;
    private int entityTypeId;

    public CharacterStory(
            String name,
            String description,
            String birthDate,
            CharacterModel model,
            CharacterType characterType,
            boolean showNametag,
            MainCharacterAttribute mainCharacterAttribute) {
        super(name, description);
        this.birthDate = birthDate;
        this.model = model;
        this.characterType = characterType;
        this.showNametag = showNametag;
        this.mainCharacterAttribute = mainCharacterAttribute;
        this.entityType = EntityType.PLAYER;
        this.entityTypeId = BuiltInRegistries.ENTITY_TYPE.getId(entityType);
    }

    public CharacterStory(
            String name,
            String description,
            String day,
            String month,
            String year,
            CharacterModel model,
            CharacterType characterType) {
        super(name, description);
        this.birthDate = day + "/" + month + "/" + year;
        this.characterType = characterType;
        this.model = model;
        this.entityType = EntityType.PLAYER;
        this.entityTypeId = BuiltInRegistries.ENTITY_TYPE.getId(entityType);
        showNametag = characterType == CharacterType.MAIN;
    }

    public CharacterStory(String name, String description, CharacterType characterType, CharacterModel model) {
        super(name, description);
        this.birthDate = "01/01/2000";
        this.characterType = characterType;
        this.model = model;
        this.entityType = EntityType.PLAYER;
        this.entityTypeId = BuiltInRegistries.ENTITY_TYPE.getId(entityType);
        this.showNametag = false;
    }

    public void updateEntityType(EntityType<?> entityType, Scene scene) {
        EntityType<?> oldEntityType = this.entityType;
        int oldEntityTypeId = entityTypeId;
        try {
            this.entityType = entityType;
            entityTypeId = BuiltInRegistries.ENTITY_TYPE.getId(entityType);
            if (scene != null) {
                NarrativeCraftFile.updateCharacterData(this, this, scene);
            } else {
                NarrativeCraftFile.updateCharacterData(this, this);
            }
        } catch (IOException e) {
            this.entityType = oldEntityType;
            entityTypeId = oldEntityTypeId;
            //            Util.sendCrashMessage(Minecraft.getInstance().player, e);
            //            Minecraft.getInstance().setScreen(null);
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

    public CharacterModel getModel() {
        return model;
    }

    public void setModel(CharacterModel model) {
        this.model = model;
    }

    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    public int getEntityTypeId() {
        return entityTypeId;
    }

    public boolean showNametag() {
        return showNametag;
    }

    public void setShowNametag(boolean showNametag) {
        this.showNametag = showNametag;
    }

    public EntityType<?> getEntityType() {
        if (entityType == null) {
            entityType = EntityType.PLAYER;
        }
        return entityType;
    }

    public MainCharacterAttribute getMainCharacterAttribute() {
        if (mainCharacterAttribute == null) {
            mainCharacterAttribute = new MainCharacterAttribute();
        }
        return mainCharacterAttribute;
    }

    public void setMainCharacterAttribute(MainCharacterAttribute mainCharacterAttribute) {
        this.mainCharacterAttribute = mainCharacterAttribute;
    }
}
