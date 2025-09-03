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

package fr.loudo.narrativecraft.controllers.cameraAngle;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.controllers.keyframe.AbstractKeyframesBase;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.CameraAngle;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.keyframes.cameraAngle.CameraAngleKeyframe;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.screens.controller.cameraAngle.CameraAngleControllerScreen;
import fr.loudo.narrativecraft.screens.controller.cameraAngle.CameraAngleOptionsScreen;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class CameraAngleController extends AbstractKeyframesBase<CameraAngleKeyframe> {

    private final CameraAngle cameraAngle;
    private final List<CharacterStoryData> characterStoryDataList = new ArrayList<>();

    public CameraAngleController(Environment environment, Player player, CameraAngle cameraAngle) {
        super(environment, player);
        this.cameraAngle = cameraAngle;
        hudMessage = Translation.message("controller.camera_angle.hud").getString();
        characterStoryDataList.addAll(cameraAngle.getCharacterStoryDataList());
        keyframes.addAll(cameraAngle.getCameraAngles());
    }

    public CameraAngleKeyframe createKeyframe(String name) {
        CameraAngleKeyframe keyframe =
                new CameraAngleKeyframe(keyframesCounter.incrementAndGet(), getKeyframeLocationFromPlayer(), name);
        keyframe.showKeyframe(playerSession.getPlayer());
        keyframes.add(keyframe);
        return keyframe;
    }

    public CameraAngleKeyframe getKeyframeByName(String name) {
        for (CameraAngleKeyframe keyframe : keyframes) {
            if (keyframe.getName().equalsIgnoreCase(name)) {
                return keyframe;
            }
        }
        return null;
    }

    @Override
    public Screen keyframeOptionScreen(Keyframe keyframe, boolean hide) {
        return new CameraAngleOptionsScreen((CameraAngleKeyframe) keyframe, this, playerSession, hide);
    }

    @Override
    public void startSession() {
        stopCurrentSession();
        playerSession.setController(this);
        if (environment != Environment.DEVELOPMENT) return;
        Location location = null;
        if (!characterStoryDataList.isEmpty()) {
            location = characterStoryDataList.getFirst().getLocation();
        } else if (!keyframes.isEmpty()) {
            keyframesCounter.set(keyframes.getLast().getId());
            location = keyframes.getFirst().getKeyframeLocation().asLocation();
        }
        if (location != null) {
            playerSession.getPlayer().teleportTo(location.x(), location.y(), location.z());
        }
        for (CameraAngleKeyframe cameraAngleKeyframe : keyframes) {
            cameraAngleKeyframe.showKeyframe(playerSession.getPlayer());
        }
        for (CharacterStoryData characterStoryData : characterStoryDataList) {
            characterStoryData.spawn(playerSession.getPlayer().level(), environment);
        }
    }

    @Override
    public void stopSession(boolean save) {
        for (CameraAngleKeyframe cameraAngleKeyframe : keyframes) {
            cameraAngleKeyframe.hideKeyframe(playerSession.getPlayer());
        }
        for (CharacterStoryData characterStoryData : characterStoryDataList) {
            characterStoryData.kill();
        }
        playerSession.setController(null);
        if (environment != Environment.DEVELOPMENT) return;
        if (save) {
            List<CameraAngleKeyframe> oldCameraAngles = cameraAngle.getCameraAngles();
            List<CharacterStoryData> oldCharacterStoryData = cameraAngle.getCharacterStoryDataList();
            cameraAngle.getCameraAngles().clear();
            cameraAngle.getCharacterStoryDataList().clear();
            try {
                cameraAngle.getCameraAngles().addAll(keyframes);
                cameraAngle.getCharacterStoryDataList().addAll(characterStoryDataList);
                NarrativeCraftFile.updateCameraAngles(cameraAngle.getScene());
                playerSession.getPlayer().sendSystemMessage(Translation.message("controller.saved"));
            } catch (IOException e) {
                cameraAngle.getCameraAngles().addAll(oldCameraAngles);
                cameraAngle.getCharacterStoryDataList().addAll(oldCharacterStoryData);
                playerSession.getPlayer().sendSystemMessage(Translation.message("crash.global-message"));
                NarrativeCraftMod.LOGGER.error("Impossible to save the camera angle: ", e);
            }
        }
    }

    public CharacterStoryData getCharacterStoryDataFromEntity(Entity entity) {
        for (CharacterStoryData characterStoryData : characterStoryDataList) {
            if (Util.isSameEntity(
                    entity, characterStoryData.getCharacterRuntime().getEntity())) {
                return characterStoryData;
            }
        }
        return null;
    }

    @Override
    public Screen getControllerScreen() {
        return new CameraAngleControllerScreen(this);
    }

    public CameraAngle getCameraAngle() {
        return cameraAngle;
    }

    public List<CharacterStoryData> getCharacterStoryDataList() {
        return characterStoryDataList;
    }

    public List<CharacterStory> getCharacterStories() {
        return characterStoryDataList.stream()
                .map(CharacterStoryData::getCharacterStory)
                .toList();
    }
}
