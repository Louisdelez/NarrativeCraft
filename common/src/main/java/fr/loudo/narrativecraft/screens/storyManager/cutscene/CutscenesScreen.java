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

package fr.loudo.narrativecraft.screens.storyManager.cutscene;

import fr.loudo.narrativecraft.controllers.cutscene.CutsceneController;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.PickElementScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scene.ScenesMenuScreen;
import fr.loudo.narrativecraft.util.ImageFontConstants;
import fr.loudo.narrativecraft.util.Translation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class CutscenesScreen extends StoryElementScreen {

    private final Scene scene;

    private Button settingsButton;

    public CutscenesScreen(Scene scene) {
        super(Translation.message("screen.story_manager.cutscene_list", scene.getName()));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<Cutscene> screen = new EditInfoScreen<>(this, null, new EditScreenCutsceneAdapter(scene));
            this.minecraft.setScreen(screen);
        });
        initFolderButton();
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, p_345997_ -> this.onClose())
                .width(200)
                .build());
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new ScenesMenuScreen(scene));
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = scene.getCutscenes().stream()
                .map(cutscene -> {
                    Button button = Button.builder(Component.literal(cutscene.getName()), button1 -> {
                                new CutsceneController(Environment.DEVELOPMENT, minecraft.player, cutscene)
                                        .startSession();
                                minecraft.setScreen(null);
                            })
                            .build();
                    settingsButton = createSettingsButton(cutscene);
                    settingsButton.setTooltip(Tooltip.create(
                            hasShiftDown()
                                    ? Translation.message("screen.story_manager.animation_cutscene_link")
                                    : Translation.message("screen.story_manager.subscene_cutscene_link")));
                    return new StoryElementList.StoryEntryData(
                            button,
                            List.of(settingsButton),
                            () -> {
                                minecraft.setScreen(
                                        new EditInfoScreen<>(this, cutscene, new EditScreenCutsceneAdapter(scene)));
                            },
                            () -> {
                                minecraft.setScreen(new CutscenesScreen(scene));
                                try {
                                    scene.removeCutscene(cutscene);
                                    NarrativeCraftFile.updateCutsceneFile(scene);
                                    minecraft.setScreen(new CutscenesScreen(scene));
                                } catch (Exception e) {
                                    scene.addCutscene(cutscene);
                                    fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                                }
                            });
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    private Button createSettingsButton(Cutscene cutscene) {
        List<Subscene> subscenesAvailable = scene.getSubscenes().stream()
                .filter(sub -> cutscene.getSubscenes().stream()
                        .noneMatch(s -> s.getName().equals(sub.getName())))
                .toList();

        List<Animation> animationsAvailable = scene.getAnimations().stream()
                .filter(anim -> cutscene.getAnimations().stream()
                        .noneMatch(a -> a.getName().equals(anim.getName())))
                .toList();
        return Button.builder(ImageFontConstants.SETTINGS, button -> {
                    PickElementScreen screen;
                    if (Screen.hasShiftDown()) {
                        screen = new PickElementScreen(
                                this,
                                Translation.message(
                                        "screen.pick.cutscene.title",
                                        Translation.message("global.animations"),
                                        Component.literal(cutscene.getName())),
                                Translation.message("global.animations"),
                                animationsAvailable,
                                cutscene.getAnimations(),
                                entries -> {
                                    List<Animation> selectedAnimations = new ArrayList<>();
                                    for (PickElementScreen.TransferableStorySelectionList.Entry entry : entries) {
                                        Animation animation = (Animation) entry.getNarrativeEntry();
                                        selectedAnimations.add(animation);
                                    }
                                    List<Animation> oldAnimations = cutscene.getAnimations();
                                    try {
                                        cutscene.getAnimations().clear();
                                        cutscene.getAnimations().addAll(selectedAnimations);
                                        NarrativeCraftFile.updateCutsceneFile(scene);
                                        this.minecraft.setScreen(new CutscenesScreen(scene));
                                    } catch (Exception e) {
                                        cutscene.getAnimations().addAll(oldAnimations);
                                        fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                                        minecraft.setScreen(null);
                                    }
                                });
                    } else {
                        screen = new PickElementScreen(
                                this,
                                Translation.message(
                                        "screen.pick.cutscene.title",
                                        Translation.message("global.subscenes"),
                                        Component.literal(cutscene.getName())),
                                Translation.message("global.subscenes"),
                                subscenesAvailable,
                                cutscene.getSubscenes(),
                                entries -> {
                                    List<Subscene> selectedSubscene = new ArrayList<>();
                                    for (PickElementScreen.TransferableStorySelectionList.Entry entry : entries) {
                                        Subscene subscene = (Subscene) entry.getNarrativeEntry();
                                        selectedSubscene.add(subscene);
                                    }
                                    List<Subscene> oldSubscenes = cutscene.getSubscenes();
                                    try {
                                        cutscene.getSubscenes().clear();
                                        cutscene.getSubscenes().addAll(selectedSubscene);
                                        NarrativeCraftFile.updateCutsceneFile(scene);
                                        this.minecraft.setScreen(new CutscenesScreen(scene));
                                    } catch (Exception e) {
                                        cutscene.getSubscenes().addAll(oldSubscenes);
                                        fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                                        minecraft.setScreen(null);
                                    }
                                });
                    }
                    this.minecraft.setScreen(screen);
                })
                .width(20)
                .build();
    }

    @Override
    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.getDataFolder(scene).toPath());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        settingsButton.setTooltip(Tooltip.create(
                hasShiftDown()
                        ? Translation.message("screen.story_manager.animation_cutscene_link")
                        : Translation.message("screen.story_manager.subscene_cutscene_link")));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
