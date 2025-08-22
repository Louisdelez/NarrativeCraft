package fr.loudo.narrativecraft.serialization;

import com.google.gson.*;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
        JsonObject obj = new JsonObject();
        obj.addProperty("name", subscene.getName());
        obj.addProperty("description", subscene.getDescription());

        JsonArray animationsArray = new JsonArray();
        for (String name : subscene.getAnimationsName()) {
            animationsArray.add(name);
        }
        obj.add(animationsKey, animationsArray);

        return obj;
    }

    @Override
    public Subscene deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        Subscene subscene = new Subscene(
                obj.get("name").getAsString(),
                obj.get("description").getAsString(),
                scene
        );

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
