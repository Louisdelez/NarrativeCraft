package fr.loudo.narrativecraft.narrative.recording.actions;

import com.zigythebird.playeranimcore.animation.Animation;
import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.manager.ActionType;
import fr.loudo.narrativecraft.util.FakePlayer;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import java.util.UUID;

public class EmoteAction extends Action {

    private final UUID emoteId;

    public EmoteAction(int tick, UUID emoteId) {
        super(tick, ActionType.EMOTE);
        this.emoteId = emoteId;
    }

    @Override
    public void execute(PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof FakePlayer fakePlayer) {
            Animation emote = UniversalEmoteSerializer.getEmote(emoteId);
            ServerEmoteAPI.playEmote(fakePlayer.getUUID(), emote, false);
        }
    }

    @Override
    public void rewind(PlaybackData playbackData) {}

}
