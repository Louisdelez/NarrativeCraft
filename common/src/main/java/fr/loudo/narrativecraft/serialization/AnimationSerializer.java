package fr.loudo.narrativecraft.serialization;

import com.google.gson.*;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;

import java.lang.reflect.Type;

public class AnimationSerializer implements JsonSerializer<Animation>, JsonDeserializer<Animation> {

    private final String characterKey = "character_name";

    @Override
    public JsonElement serialize(Animation animation, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = context.serialize(animation).getAsJsonObject();
        CharacterStory characterStory = animation.getCharacterStory();
        if(characterStory == null) return jsonObject;
        jsonObject.addProperty(characterKey, characterStory.getName());
        return jsonObject;
    }

    @Override
    public Animation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Animation animation = context.deserialize(json, Animation.class);
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        JsonObject jsonObject = json.getAsJsonObject();
        if(jsonObject.has(characterKey)) {
            String characterName = jsonObject.get(characterKey).getAsString();
            CharacterStory characterStory = characterManager.getCharacterByName(characterName);
            if(characterStory == null) return animation;
            animation.setCharacterStory(characterStory);
        }
        return animation;
    }
}
