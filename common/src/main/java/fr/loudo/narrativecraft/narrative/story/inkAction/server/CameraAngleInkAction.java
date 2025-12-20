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

package fr.loudo.narrativecraft.narrative.story.inkAction.server;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.controllers.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.CameraAngle;
import fr.loudo.narrativecraft.narrative.character.CharacterRuntime;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.keyframes.cameraAngle.CameraAngleKeyframe;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class CameraAngleInkAction extends InkAction {

    private CameraAngle cameraAngle;
    private CameraAngleKeyframe keyframe;

    public CameraAngleInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() < 2) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Camera angle parent name missing"));
        }
        if (arguments.size() < 3) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Camera angle child name missing"));
        }
        String parentName = arguments.get(1);
        if (scene == null) return InkActionResult.ignored();
        cameraAngle = scene.getCameraAngleByName(parentName);
        if (cameraAngle == null) {
            return InkActionResult.error(Translation.message("camera_angle.no_exists", parentName, scene.getName()));
        }
        String childName = arguments.get(2);
        keyframe = cameraAngle.getCameraAngleKeyframeByName(childName);
        if (keyframe == null) {
            return InkActionResult.error(Translation.message("camera_angle.keyframe_no_exists", childName, parentName));
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        playerSession.clearKilledCharacters();
        if (playerSession.getController() instanceof CameraAngleController cameraAngleController) {
            playerSession.getInkActions().removeIf(inkAction -> inkAction instanceof CameraAngleInkAction);
            if (!cameraAngleController.getCameraAngle().getName().equalsIgnoreCase(cameraAngle.getName())) {
                clear(playerSession);
            }
        } else if (!(playerSession.getController() instanceof CameraAngleController)) {
            clear(playerSession);
        }

        playerSession.setCurrentCamera(keyframe.getKeyframeLocation());
        Minecraft.getInstance().options.hideGui = true;
        return InkActionResult.ok();
    }

    private void clear(PlayerSession playerSession) {
        // Remove characters that exists in the story
        List<CharacterRuntime> toRemove = new ArrayList<>();
        for (CharacterStoryData characterStoryData : cameraAngle.getCharacterStoryDataList()) {
            for (CharacterRuntime characterRuntime : playerSession.getCharacterRuntimes()) {
                if (characterStoryData
                                .getCharacterStory()
                                .getName()
                                .equalsIgnoreCase(
                                        characterRuntime.getCharacterStory().getName())
                        && !characterStoryData.isTemplate()) {
                    if (characterRuntime.getEntity() == null) continue;
                    NarrativeCraftMod.server.execute(
                            () -> characterRuntime.getEntity().remove(Entity.RemovalReason.KILLED));
                    toRemove.add(characterRuntime);
                }
            }
        }
        playerSession.getCharacterRuntimes().removeAll(toRemove);
        CameraAngleController controller =
                new CameraAngleController(Environment.PRODUCTION, playerSession.getPlayer(), cameraAngle);
        controller.startSession();
    }

    @Override
    public boolean needScene() {
        return true;
    }
}
