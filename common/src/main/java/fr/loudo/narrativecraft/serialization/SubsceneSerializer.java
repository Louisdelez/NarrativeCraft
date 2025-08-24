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
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import java.lang.reflect.Type;

/**
 * Serializer for subscene to only references animations name instead of their data
 * (Animations are location to animations folder of the scene)
 */
public class SubsceneSerializer implements JsonSerializer<Subscene>, JsonDeserializer<Subscene> {
    private final String animationsKey = "animations_name";
    private final Scene scene;

    public SubsceneSerializer(Scene scene) {
        this.scene = scene;
    }

    @Override
    public JsonElement serialize(Subscene subscene, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new Gson().toJsonTree(subscene).getAsJsonObject();

        JsonArray animationsArray = new JsonArray();
        for (String name : subscene.getAnimationsName()) {
            animationsArray.add(name);
        }
        obj.add(animationsKey, animationsArray);

        return obj;
    }

    @Override
    public Subscene deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        Subscene subscene = new Gson().fromJson(json, Subscene.class);
        subscene.setScene(scene);

        if (obj.has(animationsKey)) {
            for (JsonElement e : obj.getAsJsonArray(animationsKey)) {
                String animationName = e.getAsString();
                Animation animation = scene.getAnimationByName(animationName);
                if (animation != null) {
                    subscene.getAnimations().add(animation);
                }
            }
        }

        return subscene;
    }
}
