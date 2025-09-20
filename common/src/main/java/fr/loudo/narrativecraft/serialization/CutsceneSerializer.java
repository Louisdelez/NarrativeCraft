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
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import java.lang.reflect.Type;

/**
 * Cutscene needs its own serializer to have light file size (Apply for instance of Animation)
 * and prevent duplicates data (Since subscenes and animations have their own separated files.)
 */
public class CutsceneSerializer implements JsonSerializer<Cutscene>, JsonDeserializer<Cutscene> {

    private final String animationsKey = "animations_name";
    private final String subscenesKey = "subscenes_name";

    private final Scene scene;

    public CutsceneSerializer(Scene scene) {
        this.scene = scene;
    }

    @Override
    public JsonElement serialize(Cutscene cutscene, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new Gson().toJsonTree(cutscene).getAsJsonObject();

        JsonArray subscenesArray = new JsonArray();
        for (String name : cutscene.getSubscenesName()) {
            subscenesArray.add(name);
        }
        obj.add(subscenesKey, subscenesArray);

        JsonArray animationsArray = new JsonArray();
        for (String name : cutscene.getAnimationsName()) {
            animationsArray.add(name);
        }
        obj.add(animationsKey, animationsArray);

        return obj;
    }

    @Override
    public Cutscene deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        Cutscene cutscene = new Gson().fromJson(json, Cutscene.class);
        cutscene.setScene(scene);

        if (obj.has(subscenesKey)) {
            for (JsonElement e : obj.getAsJsonArray(subscenesKey)) {
                String subsceneName = e.getAsString();
                Subscene subscene = scene.getSubsceneByName(subsceneName);
                if (subscene == null) continue;
                cutscene.getSubscenes().add(subscene);
            }
        }

        if (obj.has(animationsKey)) {
            for (JsonElement e : obj.getAsJsonArray(animationsKey)) {
                String animationName = e.getAsString();
                Animation animation = scene.getAnimationByName(animationName);
                if (animation == null) continue;
                cutscene.getAnimations().add(animation);
            }
        }

        return cutscene;
    }
}
