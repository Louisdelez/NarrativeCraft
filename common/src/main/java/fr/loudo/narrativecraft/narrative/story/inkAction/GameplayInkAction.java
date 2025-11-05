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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class GameplayInkAction extends InkAction {

    public GameplayInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        playerSession.getInkActions().removeIf(inkAction -> inkAction instanceof GameplayInkAction);
        CharacterStory mainCharacter =
                NarrativeCraftMod.getInstance().getCharacterManager().getMainCharacter();
        if (mainCharacter == null) {
            return InkActionResult.error("A main character assigned is required for this tag to work!");
        }
        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler == null) return InkActionResult.ignored();
        if (playerSession.getDialogRenderer() != null) {
            playerSession.getDialogRenderer().setRunDialogStopped(() -> {
                playerSession.setDialogRenderer(null);
                execute(playerSession);
                playerSession.getInkActions().add(this);
            });
            playerSession.getDialogRenderer().stop();
            return InkActionResult.ok();
        }
        if (storyHandler.characterInStory(mainCharacter)) {
            CharacterRuntime characterRuntime = storyHandler.getCharacterRuntimeFromCharacter(mainCharacter);
            if (characterRuntime == null) {
                return InkActionResult.error("Main character was found in the story, but his entity was not...");
            }
            Vec3 position = characterRuntime.getEntity().position();
            Entity entity = characterRuntime.getEntity();
            playerSession
                    .getPlayer()
                    .connection
                    .teleport(position.x, position.y, position.z, entity.getYRot(), entity.getXRot());
            storyHandler.killCharacter(mainCharacter);
        }
        if (playerSession.getController() != null) {
            playerSession.getController().stopSession(false);
        }
        playerSession.setCurrentCamera(null);
        playerSession.getInkActions().removeIf(inkAction -> inkAction instanceof CameraAngleInkAction);
        return InkActionResult.ok();
    }

    @Override
    public boolean needScene() {
        return false;
    }
}
