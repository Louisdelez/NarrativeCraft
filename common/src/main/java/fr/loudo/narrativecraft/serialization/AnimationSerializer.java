package fr.loudo.narrativecraft.serialization;

import com.google.gson.*;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recording.actions.Action;

import java.lang.reflect.Type;

public class AnimationSerializer implements JsonSerializer<Animation>, JsonDeserializer<Animation> {

    private final String characterKey = "character_name";
    private final Scene scene;

    public AnimationSerializer(Scene scene) {
        this.scene = scene;
    }

    @Override
    public JsonElement serialize(Animation animation, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new Gson().toJsonTree(animation).getAsJsonObject();
        CharacterStory characterStory = animation.getCharacter();
        if(characterStory == null) return obj;
        obj.addProperty(characterKey, characterStory.getName());
        return obj;
    }

    @Override
    public Animation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Animation animation = new GsonBuilder()
                .registerTypeAdapter(Action.class, new ActionSerializer())
                .create()
                .fromJson(json, Animation.class);
        animation.setScene(scene);
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        JsonObject jsonObject = json.getAsJsonObject();
        if(jsonObject.has(characterKey)) {
            String characterName = jsonObject.get(characterKey).getAsString();
            CharacterStory characterStory = characterManager.getCharacterByName(characterName);
            if(characterStory == null) return animation;
            animation.setCharacter(characterStory);
        }
        return animation;
    }
}
