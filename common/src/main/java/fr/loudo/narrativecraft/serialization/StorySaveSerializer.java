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
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.story.StorySave;
import java.lang.reflect.Type;

public class StorySaveSerializer implements JsonSerializer<StorySave>, JsonDeserializer<StorySave> {

    private final String chapterKey = "chapter_index";
    private final String sceneKey = "scene_name";

    @Override
    public JsonElement serialize(StorySave save, Type type, JsonSerializationContext context) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CharacterStoryData.class, new CharacterStoryDataSerializer(save.getScene()))
                .create();
        JsonObject obj = gson.toJsonTree(save).getAsJsonObject();
        obj.addProperty(chapterKey, save.getChapter().getIndex());
        obj.addProperty(sceneKey, save.getScene().getName());
        return obj;
    }

    @Override
    public StorySave deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Chapter chapter = null;
        if (obj.has(chapterKey)) {
            int index = obj.get(chapterKey).getAsInt();
            chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(index);
        }
        Scene scene = null;
        if (obj.has(sceneKey) && chapter != null) {
            String sceneName = obj.get(sceneKey).getAsString();
            scene = chapter.getSceneByName(sceneName);
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CharacterStoryData.class, new CharacterStoryDataSerializer(scene))
                .create();
        StorySave save = gson.fromJson(json, StorySave.class);
        save.setScene(scene);
        save.setChapter(chapter);
        return save;
    }
}
