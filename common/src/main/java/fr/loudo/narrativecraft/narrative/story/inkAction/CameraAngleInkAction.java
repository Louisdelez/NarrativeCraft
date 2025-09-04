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

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.CameraAngle;
import fr.loudo.narrativecraft.narrative.keyframes.cameraAngle.CameraAngleKeyframe;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Translation;
import java.util.List;

public class CameraAngleInkAction extends InkAction {

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
        CameraAngle cameraAngle = scene.getCameraAngleByName(parentName);
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
        playerSession.setCurrentCamera(keyframe.getKeyframeLocation());
        return InkActionResult.ok();
    }
}
