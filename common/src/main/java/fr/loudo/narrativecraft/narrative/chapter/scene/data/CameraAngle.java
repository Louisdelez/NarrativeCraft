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

package fr.loudo.narrativecraft.narrative.chapter.scene.data;

import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.SceneData;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.keyframes.cameraAngle.CameraAngleKeyframe;
import java.util.ArrayList;
import java.util.List;

public class CameraAngle extends SceneData {

    private List<CameraAngleKeyframe> cameraAngleKeyframes = new ArrayList<>();
    private List<CharacterStoryData> characterStoryDataList = new ArrayList<>();

    public CameraAngle(String name, String description, Scene scene) {
        super(name, description, scene);
    }

    public CameraAngleKeyframe getCameraAngleKeyframeByName(String name) {
        for (CameraAngleKeyframe cameraAngleKeyframe : cameraAngleKeyframes) {
            if (cameraAngleKeyframe.getName().equalsIgnoreCase(name)) {
                return cameraAngleKeyframe;
            }
        }
        return null;
    }

    public List<CameraAngleKeyframe> getCameraAngleKeyframes() {
        if (cameraAngleKeyframes == null) {
            cameraAngleKeyframes = new ArrayList<>();
        }
        return cameraAngleKeyframes;
    }

    public List<CharacterStoryData> getCharacterStoryDataList() {
        if (characterStoryDataList == null) {
            characterStoryDataList = new ArrayList<>();
        }
        return characterStoryDataList;
    }
}
