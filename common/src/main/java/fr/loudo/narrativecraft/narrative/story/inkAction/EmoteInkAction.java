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

package fr.loudo.narrativecraft.narrative.story.inkAction;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.UUIDMap;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkActionUtil;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.util.Translation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import java.util.List;
import java.util.UUID;

public class EmoteInkAction extends InkAction {

    private String action;
    private boolean forced;
    private KeyframeAnimation emote;
    private CharacterStory characterStory;

    public EmoteInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (!Services.PLATFORM.isModLoaded("emotecraft")) return InkActionResult.ignored();
        forced = InkActionUtil.getOptionalArgument(command, "force");
        if (arguments.size() < 2) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Action, play or stop"));
        }
        action = arguments.get(1);
        if (arguments.size() < 3) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Emote name"));
        }
        if (!action.equals("play") && !action.equals("stop")) {
            return InkActionResult.error(Translation.message(WRONG_ARGUMENT_TEXT, "Only play or stop"));
        }
        String emoteName = arguments.get(2);
        emote = getEmote(emoteName, UniversalEmoteSerializer.serverEmotes);
        if (emote == null) {
            return InkActionResult.error(
                    Translation.message(WRONG_ARGUMENT_TEXT, String.format("Emote %s does not exists.", emoteName)));
        }
        if (arguments.size() < 4) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Character name"));
        }
        String characterName = arguments.get(3);
        characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacterByName(characterName);
        if (characterStory == null) {
            characterStory = scene.getNpcByName(characterName);
        }
        if (characterStory == null) {
            return InkActionResult.error(Translation.message("character.no_exists", characterName));
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        isRunning = false;
        if (!Services.PLATFORM.isModLoaded("emotecraft")) return InkActionResult.ignored();
        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler == null) return InkActionResult.ignored();
        List<CharacterRuntime> characterRuntimes = storyHandler.getCharacterRuntimeFromCharacter(characterStory);
        if (characterRuntimes.isEmpty()) return InkActionResult.ignored();
        for (CharacterRuntime characterRuntime : characterRuntimes) {
            if (characterRuntime == null || characterRuntime.getEntity() == null) continue;
            ServerEmoteAPI.playEmote(characterRuntime.getEntity().getUUID(), emote, forced);
        }
        return InkActionResult.ok();
    }

    private KeyframeAnimation getEmote(String id, UUIDMap<KeyframeAnimation> emotes) {
        try {
            UUID emoteID = UUID.fromString(id);
            for (KeyframeAnimation keyframeAnimation : emotes) {
                if (keyframeAnimation.get().equals(emoteID)) {
                    return keyframeAnimation;
                }
            }
        } catch (IllegalArgumentException ignore) {
        } // Not a UUID

        for (KeyframeAnimation animation : emotes) {
            String emoteName = animation.extraData.get("name").toString();
            emoteName = emoteName.replace("\"", "");
            if (emoteName.equalsIgnoreCase(id)) {
                return animation;
            }
        }
        return null;
    }

    @Override
    public boolean needScene() {
        return false;
    }
}
