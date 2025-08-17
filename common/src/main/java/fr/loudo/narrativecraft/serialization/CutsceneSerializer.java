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
        JsonObject obj = context.serialize(cutscene).getAsJsonObject();

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
    public Cutscene deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Cutscene cutscene = context.deserialize(json, Cutscene.class);

        JsonObject obj = json.getAsJsonObject();

        if (obj.has(subscenesKey)) {
            for (JsonElement e : obj.getAsJsonArray(subscenesKey)) {
                String subsceneName = e.getAsString();
                Subscene subscene = scene.getSubsceneByName(subsceneName);
                if(subscene == null) continue;
                cutscene.getSubscenes().add(subscene);
            }
        }

        if (obj.has(animationsKey)) {
            for (JsonElement e : obj.getAsJsonArray(animationsKey)) {
                String animationName = e.getAsString();
                Animation animation = scene.getAnimationByName(animationName);
                if(animation == null) continue;
                cutscene.getAnimations().add(animation);
            }
        }

        return cutscene;
    }

}
