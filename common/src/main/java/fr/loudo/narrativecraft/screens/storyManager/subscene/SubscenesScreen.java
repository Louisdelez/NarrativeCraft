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

package fr.loudo.narrativecraft.screens.storyManager.subscene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Animation;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class SubscenesScreen extends StoryElementScreen {

    private final Scene scene;

    public SubscenesScreen(Scene scene) {
        super(Translation.message("screen.story_manager.subscene_list", scene.getName()));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        super.addTitle();
        initAddButton(button -> {
            EditInfoScreen<Subscene> screen = new EditInfoScreen<>(this, null, new EditScreenSubsceneAdapter(scene));
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
        List<StoryElementList.StoryEntryData> entries = scene.getSubscenes().stream()
                .map(subscene -> {
                    Button button = Button.builder(Component.literal(subscene.getName()), button1 -> {})
                            .build();
                    button.active = false;

                    List<Animation> availableAnimations = scene.getAnimations().stream()
                            .filter(anim -> subscene.getAnimations().stream()
                                    .noneMatch(a -> a.getName().equals(anim.getName())))
                            .toList();
                    Button settingsButton = Button.builder(ImageFontConstants.SETTINGS, b -> {
                                PickElementScreen screen = new PickElementScreen(
                                        this,
                                        Translation.message(
                                                "screen.pick.subscene.title",
                                                Translation.message("global.animations"),
                                                Component.literal(subscene.getName())),
                                        Translation.message("global.animations"),
                                        availableAnimations,
                                        subscene.getAnimations(),
                                        entries1 -> {
                                            List<Animation> oldAnimations = subscene.getAnimations();
                                            List<Animation> selected = new ArrayList<>();
                                            for (var entry : entries1) {
                                                Animation a = (Animation) entry.getNarrativeEntry();
                                                selected.add(a);
                                            }
                                            try {
                                                subscene.getAnimations().clear();
                                                subscene.getAnimations().addAll(selected);
                                                NarrativeCraftFile.updateSubsceneFile(scene);
                                                minecraft.setScreen(new SubscenesScreen(scene));
                                            } catch (Exception e) {
                                                subscene.getAnimations().clear();
                                                subscene.getAnimations().addAll(oldAnimations);
                                                fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                                                minecraft.setScreen(null);
                                            }
                                        });
                                this.minecraft.setScreen(screen);
                            })
                            .width(20)
                            .build();
                    settingsButton.setTooltip(
                            Tooltip.create(Translation.message("screen.story_manager.subscene_animation_link")));

                    return new StoryElementList.StoryEntryData(
                            button,
                            List.of(settingsButton),
                            () -> {
                                minecraft.setScreen(
                                        new EditInfoScreen<>(this, subscene, new EditScreenSubsceneAdapter(scene)));
                            },
                            () -> {
                                minecraft.setScreen(new SubscenesScreen(scene));
                                try {
                                    scene.removeSubscene(subscene);
                                    NarrativeCraftFile.updateSubsceneFile(scene);
                                    minecraft.setScreen(new SubscenesScreen(scene));
                                } catch (Exception e) {
                                    scene.addSubscene(subscene);
                                    fr.loudo.narrativecraft.util.Util.sendCrashMessage(minecraft.player, e);
                                }
                            });
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }

    @Override
    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.getSceneFolder(scene).toPath());
    }
}
