package fr.loudo.narrativecraft.narrative.chapter.scenes.interaction;

import com.google.gson.*;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;

import java.lang.reflect.Type;

public class InteractionSerializer implements JsonSerializer<Interaction>, JsonDeserializer<Interaction> {
    @Override
    public Interaction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement interactionTypeElem = jsonObject.get("interactionType");
        if(interactionTypeElem == null) return null;
        InteractionType interactionType = InteractionType.valueOf(interactionTypeElem.getAsString());
        Interaction interaction = context.deserialize(json, interactionType.getInteractionClass());
        if(interaction instanceof AnimationInteraction animationInteraction) {
            JsonElement animElement = jsonObject.get("animation_name");
            if(animElement != null) {
                Scene scene = interaction.getScene();
                Animation animation = scene.getAnimationByName(animElement.getAsString());
                animationInteraction.setAnimation(animation);
            }
        }
        return interaction;
    }

    @Override
    public JsonElement serialize(Interaction interaction, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = context.serialize(interaction, interaction.getClass()).getAsJsonObject();
        jsonObject.addProperty("interactionType", interaction.getType().name());

        if (interaction instanceof AnimationInteraction animationInteraction) {
            Animation animation = animationInteraction.getAnimation();
            if (animation != null) {
                jsonObject.addProperty("animation_name", animation.getName());
            }
        }

        return jsonObject;
    }
}
