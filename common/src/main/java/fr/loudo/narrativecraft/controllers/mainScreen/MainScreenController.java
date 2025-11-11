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

package fr.loudo.narrativecraft.controllers.mainScreen;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionRegistry;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.controllers.keyframe.AbstractKeyframesBase;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.data.MainScreenData;
import fr.loudo.narrativecraft.narrative.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.keyframes.keyframeTrigger.KeyframeTrigger;
import fr.loudo.narrativecraft.narrative.keyframes.mainScreen.MainScreenKeyframe;
import fr.loudo.narrativecraft.narrative.recording.Location;
import fr.loudo.narrativecraft.screens.controller.mainScreen.MainScreenControllerScreen;
import fr.loudo.narrativecraft.screens.controller.mainScreen.MainScreenKeyframeOptionsScreen;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

public class MainScreenController extends AbstractKeyframesBase<MainScreenKeyframe> {

    private final MainScreenData mainScreenData;

    public MainScreenController(Environment environment, Player player, MainScreenData mainScreenData) {
        super(environment, player);
        this.mainScreenData = mainScreenData;
        if (mainScreenData.getKeyframe() != null) {
            keyframes.add(mainScreenData.getKeyframe());
        }
        if (mainScreenData.getKeyframeTrigger() != null) {
            keyframeTriggers.add(mainScreenData.getKeyframeTrigger());
        }
        characterStoryDataList.addAll(mainScreenData.getCharacterStoryDataList());
        hudMessage = Translation.message("controller.main_screen.hud").getString();
    }

    public void addKeyframe() {
        MainScreenKeyframe keyframe = new MainScreenKeyframe(0, getKeyframeLocationFromPlayer());
        keyframe.showKeyframe(playerSession.getPlayer());
        keyframes.add(keyframe);
    }

    @Override
    public void addKeyframeTrigger(int tick, String commands) {
        if (keyframeTriggers.isEmpty()) {
            super.addKeyframeTrigger(tick, commands);
        }
    }

    @Override
    public Screen keyframeOptionScreen(Keyframe keyframe, boolean hide) {
        return new MainScreenKeyframeOptionsScreen((MainScreenKeyframe) keyframe, this, playerSession, hide);
    }

    @Override
    public void startSession() {
        stopCurrentSession();
        playerSession.setController(this);
        for (CharacterStoryData characterStoryData : characterStoryDataList) {
            characterStoryData.spawn(playerSession.getPlayer().level(), environment);
            playerSession.getCharacterRuntimes().add(characterStoryData.getCharacterRuntime());
        }
        if (environment == Environment.PRODUCTION) {
            if (!keyframes.isEmpty()) {
                playerSession.setCurrentCamera(keyframes.getFirst().getKeyframeLocation());
            }
            if (!keyframeTriggers.isEmpty()) {
                List<String> tags = keyframeTriggers.getFirst().getCommandsToList();
                for (String tag : tags) {
                    InkAction inkAction = InkActionRegistry.findByCommand(tag);
                    if (inkAction == null) continue;
                    InkActionResult result = inkAction.validateAndExecute(tag, playerSession);
                    if (result.isOk()) {
                        playerSession.addInkAction(inkAction);
                    }
                }
            }
            return;
        }
        if (!keyframes.isEmpty()) {
            keyframes.getFirst().showKeyframe(playerSession.getPlayer());
        }
        if (!keyframeTriggers.isEmpty()) {
            keyframeTriggers.getFirst().showKeyframe(playerSession.getPlayer());
        }
        Location location = null;
        if (!keyframes.isEmpty()) {
            location = keyframes.getFirst().getKeyframeLocation().asLocation();
        } else if (!keyframeTriggers.isEmpty()) {
            location = keyframeTriggers.getFirst().getKeyframeLocation().asLocation();
        } else if (!characterStoryDataList.isEmpty()) {
            location = characterStoryDataList.getFirst().getLocation();
        }
        if (location != null) {
            playerSession.getPlayer().teleportTo(location.x(), location.y(), location.z());
        }
    }

    @Override
    public void stopSession(boolean save) {
        playerSession.setController(null);
        for (CharacterStoryData characterStoryData : characterStoryDataList) {
            characterStoryData.kill();
        }
        playerSession
                .getCharacterRuntimes()
                .removeAll(characterStoryDataList.stream()
                        .map(CharacterStoryData::getCharacterRuntime)
                        .toList());
        if (environment == Environment.PRODUCTION) {
            for (InkAction inkAction : playerSession.getInkActions()) {
                inkAction.stop();
            }
            playerSession.getInkActions().clear();
            playerSession.setCurrentCamera(null);
            return;
        }
        if (!keyframes.isEmpty()) {
            keyframes.getFirst().hideKeyframe(playerSession.getPlayer());
        }
        if (!keyframeTriggers.isEmpty()) {
            keyframeTriggers.getFirst().hideKeyframe(playerSession.getPlayer());
        }
        playerSession.getCharacterRuntimes().clear();
        if (save) {
            MainScreenKeyframe oldKeyframe = mainScreenData.getKeyframe();
            KeyframeTrigger oldKeyframeTrigger = mainScreenData.getKeyframeTrigger();
            MainScreenKeyframe keyframe = !keyframes.isEmpty() ? keyframes.getFirst() : null;
            KeyframeTrigger keyframeTrigger = !keyframeTriggers.isEmpty() ? keyframeTriggers.getFirst() : null;
            List<CharacterStoryData> oldCharacterStoryData = mainScreenData.getCharacterStoryDataList();
            mainScreenData.getCharacterStoryDataList().clear();
            try {
                mainScreenData.setKeyframe(keyframe);
                mainScreenData.setKeyframeTrigger(keyframeTrigger);
                mainScreenData.getCharacterStoryDataList().addAll(characterStoryDataList);
                NarrativeCraftFile.updateMainScreenBackground(mainScreenData, playerSession.getScene());
                for (CharacterStoryData characterStoryData : mainScreenData.getCharacterStoryDataList()) {
                    characterStoryData.setSkinName(
                            characterStoryData.getCharacterRuntime().getSkinName());
                }
                playerSession.getPlayer().sendSystemMessage(Translation.message("controller.saved"));
            } catch (IOException e) {
                mainScreenData.setKeyframe(oldKeyframe);
                mainScreenData.setKeyframeTrigger(oldKeyframeTrigger);
                mainScreenData.getCharacterStoryDataList().addAll(oldCharacterStoryData);
                for (CharacterStoryData characterStoryData : mainScreenData.getCharacterStoryDataList()) {
                    characterStoryData.setSkinName(
                            characterStoryData.getCharacterRuntime().getOldSkinName());
                }
                Util.sendCrashMessage(playerSession.getPlayer(), e);
            }
        } else {
            for (CharacterStoryData characterStoryData : mainScreenData.getCharacterStoryDataList()) {
                characterStoryData.setSkinName(
                        characterStoryData.getCharacterRuntime().getOldSkinName());
            }
        }
    }

    public MainScreenKeyframe getKeyframe() {
        return keyframes.isEmpty() ? null : keyframes.getFirst();
    }

    public KeyframeTrigger getKeyframeTrigger() {
        return keyframeTriggers.isEmpty() ? null : keyframeTriggers.getFirst();
    }

    @Override
    public Screen getControllerScreen() {
        return new MainScreenControllerScreen(this);
    }

    public MainScreenData getMainScreenData() {
        return mainScreenData;
    }
}
