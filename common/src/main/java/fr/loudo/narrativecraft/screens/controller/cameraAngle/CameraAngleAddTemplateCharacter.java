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

package fr.loudo.narrativecraft.screens.controller.cameraAngle;

import fr.loudo.narrativecraft.controllers.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.narrative.playback.PlaybackData;
import fr.loudo.narrativecraft.narrative.recording.actions.*;
import fr.loudo.narrativecraft.screens.components.ButtonListScreen;
import fr.loudo.narrativecraft.util.Translation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class CameraAngleAddTemplateCharacter extends ButtonListScreen {

    private final CameraAngleController cameraAngleController;
    private List<Button> buttons = new ArrayList<>();

    public CameraAngleAddTemplateCharacter(Screen lastScreen, CameraAngleController cameraAngleController) {
        super(
                lastScreen,
                Translation.message(
                        "controller.camera_angle.template_character",
                        cameraAngleController.getCameraAngle().getName()));
        this.cameraAngleController = cameraAngleController;
    }

    public CameraAngleAddTemplateCharacter(
            Screen lastScreen, Component title, CameraAngleController cameraAngleController, List<Button> buttons) {
        super(lastScreen, title);
        this.cameraAngleController = cameraAngleController;
        this.buttons = buttons;
    }

    @Override
    protected void addContents() {
        objectListScreen.clear();
        Scene scene = cameraAngleController.getCameraAngle().getScene();
        if (!buttons.isEmpty()) {
            for (Button button : buttons) {
                objectListScreen.addButton(button);
            }
            return;
        }
        List<Button> animationsButton = new ArrayList<>();
        Button animationButton = Button.builder(Translation.message("global.animations"), button -> {
                    for (Animation animation : scene.getAnimations()) {
                        Button button1 = Button.builder(Component.literal(animation.getName()), button2 -> {
                                    spawnEntity(
                                            animation,
                                            animation
                                                            .getActionsData()
                                                            .getFirst()
                                                            .getLocations()
                                                            .size()
                                                    - 1);
                                })
                                .build();
                        animationsButton.add(button1);
                    }
                    minecraft.setScreen(new CameraAngleAddTemplateCharacter(
                            this, Translation.message("global.animations"), cameraAngleController, animationsButton));
                })
                .build();
        objectListScreen.addButton(animationButton);

        List<Button> subscenesButton = new ArrayList<>();
        Button subsceneButton = Button.builder(Translation.message("global.subscenes"), button -> {
                    for (Subscene subscene : scene.getSubscenes()) {
                        Button button1 = Button.builder(Component.literal(subscene.getName()), button2 -> {
                                    for (Animation animation : subscene.getAnimations()) {
                                        spawnEntity(
                                                animation,
                                                animation
                                                                .getActionsData()
                                                                .getFirst()
                                                                .getLocations()
                                                                .size()
                                                        - 1);
                                    }
                                })
                                .build();
                        subscenesButton.add(button1);
                    }
                    minecraft.setScreen(new CameraAngleAddTemplateCharacter(
                            this, Translation.message("global.subscenes"), cameraAngleController, subscenesButton));
                })
                .build();
        objectListScreen.addButton(subsceneButton);

        List<Button> cutscenesButton = new ArrayList<>();
        Button cutsceneButton = Button.builder(Translation.message("global.cutscenes"), button -> {
                    for (Cutscene cutscene : scene.getCutscenes()) {
                        if (cutscene.getKeyframeGroups().isEmpty()) continue;
                        Button button1 = Button.builder(Component.literal(cutscene.getName()), button2 -> {
                                    CutsceneKeyframe lastKeyframe = cutscene.getKeyframeGroups()
                                            .getLast()
                                            .getKeyframes()
                                            .getLast();
                                    int lastLocIndex =
                                            (lastKeyframe.getTick() + 2 + lastKeyframe.getTransitionDelayTick());
                                    for (Subscene subscene : cutscene.getSubscenes()) {
                                        for (Animation animation : subscene.getAnimations()) {
                                            spawnEntity(
                                                    animation,
                                                    Math.min(
                                                            lastLocIndex,
                                                            animation
                                                                            .getActionsData()
                                                                            .getFirst()
                                                                            .getLocations()
                                                                            .size()
                                                                    - 1));
                                        }
                                    }
                                    for (Animation animation : cutscene.getAnimations()) {
                                        spawnEntity(
                                                animation,
                                                Math.min(
                                                        lastLocIndex,
                                                        animation
                                                                        .getActionsData()
                                                                        .getFirst()
                                                                        .getLocations()
                                                                        .size()
                                                                - 1));
                                    }
                                })
                                .build();
                        cutscenesButton.add(button1);
                    }
                    minecraft.setScreen(new CameraAngleAddTemplateCharacter(
                            this, Translation.message("global.cutscenes"), cameraAngleController, cutscenesButton));
                })
                .build();
        objectListScreen.addButton(cutsceneButton);
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, p_345997_ -> this.onClose())
                .width(200)
                .build());
    }

    private void spawnEntity(Animation animation, int index) {
        List<Action> actions = animation.getActionsData().getFirst().getActions().stream()
                .filter(action -> index >= action.getTick()
                        && (action instanceof ItemChangeAction
                                || action instanceof LivingEntityByteAction
                                || action instanceof EntityByteAction
                                || action instanceof PoseAction))
                .toList();
        CharacterStoryData characterStoryData =
                new CharacterStoryData(animation.getCharacter(), animation.getLastLocation(), true);
        characterStoryData.spawn(
                cameraAngleController.getPlayerSession().getPlayer().level(), Environment.DEVELOPMENT);
        PlaybackData playbackData = new PlaybackData(animation.getActionsData().getFirst(), null);
        playbackData.setEntity(characterStoryData.getCharacterRuntime().getEntity());
        for (Action action : actions) {
            action.execute(playbackData);
        }
        characterStoryData.applyItems(minecraft.player.registryAccess());
        characterStoryData.applyBytes((LivingEntity) playbackData.getEntity());
        cameraAngleController.getCharacterStoryDataList().add(characterStoryData);
        cameraAngleController.getPlayerSession().getCharacterRuntimes().add(characterStoryData.getCharacterRuntime());
        minecraft.setScreen(null);
    }
}
