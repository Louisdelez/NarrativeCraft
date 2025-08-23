package fr.loudo.narrativecraft.serialization;

import com.google.gson.*;
import fr.loudo.narrativecraft.narrative.recording.actions.Action;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;

import java.lang.reflect.Type;

public class ActionSerializer implements JsonDeserializer<Action> {

    @Override
    public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement actionTypElem = jsonObject.get("actionType");
        if(actionTypElem == null) return null;
        ActionType actionType = ActionType.valueOf(actionTypElem.getAsString());
        return context.deserialize(json, actionType.getActionClass());
    }
}
