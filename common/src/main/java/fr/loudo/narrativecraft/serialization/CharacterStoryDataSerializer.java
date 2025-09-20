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

package fr.loudo.narrativecraft.serialization;

import com.google.gson.*;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import java.lang.reflect.Type;

public class CharacterStoryDataSerializer
        implements JsonSerializer<CharacterStoryData>, JsonDeserializer<CharacterStoryData> {

    private final String characterKey = "character_name";
    private final Scene scene;

    public CharacterStoryDataSerializer(Scene scene) {
        this.scene = scene;
    }

    @Override
    public JsonElement serialize(
            CharacterStoryData characterStoryData, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject obj = new Gson().toJsonTree(characterStoryData).getAsJsonObject();
        CharacterStory characterStory = characterStoryData.getCharacterStory();
        if (characterStory == null) return obj;
        obj.addProperty(characterKey, characterStory.getName());
        return obj;
    }

    @Override
    public CharacterStoryData deserialize(
            JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        CharacterStoryData characterStoryData = new GsonBuilder().create().fromJson(json, CharacterStoryData.class);
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has(characterKey)) {
            String characterName = jsonObject.get(characterKey).getAsString();
            CharacterStory characterStory = characterManager.getCharacterByName(characterName);
            if (characterStory == null && scene != null) {
                characterStory = scene.getNpcByName(characterName);
                if (characterStory == null) return characterStoryData;
            }
            characterStoryData.setCharacterRuntime(
                    new CharacterRuntime(characterStory, characterStoryData.getSkinName(), null));
        }
        return characterStoryData;
    }
}
