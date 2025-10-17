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

package fr.loudo.narrativecraft.screens.mainScreen;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.controllers.AbstractController;
import fr.loudo.narrativecraft.controllers.cutscene.CutsceneController;
import fr.loudo.narrativecraft.controllers.cutscene.CutscenePlayback;
import fr.loudo.narrativecraft.controllers.mainScreen.MainScreenController;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.Environment;
import fr.loudo.narrativecraft.narrative.data.MainScreenData;
import fr.loudo.narrativecraft.narrative.keyframes.cutscene.CutsceneKeyframe;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.StorySave;
import fr.loudo.narrativecraft.narrative.story.StoryValidation;
import fr.loudo.narrativecraft.narrative.story.inkAction.FadeInkAction;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.screens.components.ChapterSelectorScreen;
import fr.loudo.narrativecraft.screens.components.CrashScreen;
import fr.loudo.narrativecraft.screens.components.FinishedStoryScreen;
import fr.loudo.narrativecraft.screens.components.NarrativeCraftLogoRenderer;
import fr.loudo.narrativecraft.util.ErrorLine;
import fr.loudo.narrativecraft.util.Translation;
import fr.loudo.narrativecraft.util.Util;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;

public class MainScreen extends Screen {

    public static final ResourceLocation BACKGROUND_IMAGE =
            ResourceLocation.withDefaultNamespace("textures/narrativecraft_mainscreen/background.png");
    public static final ResourceLocation MUSIC =
            ResourceLocation.withDefaultNamespace("narrativecraft_mainscreen.music");

    public static SimpleSoundInstance musicInstance = new SimpleSoundInstance(
            MainScreen.MUSIC,
            SoundSource.MASTER,
            0.7f,
            1,
            SoundInstance.createUnseededRandom(),
            true,
            0,
            SoundInstance.Attenuation.NONE,
            0.0F,
            0.0F,
            0.0F,
            true);

    private final NarrativeCraftLogoRenderer narrativeCraftLogo =
            NarrativeCraftMod.getInstance().getNarrativeCraftLogoRenderer();
    private final PlayerSession playerSession;
    private final int buttonWidth = 100;
    private final int buttonHeight = 20;

    private final int initialX = 50;
    private final int gap = 5;
    private int initialY;

    private int showDevBtnCount;
    private Button devButton;

    private final boolean finishedStory;
    private final boolean pause;
    private boolean rendered;

    private int userFloodedKeyboard;

    public MainScreen(PlayerSession playerSession, boolean finishedStory, boolean pause) {
        super(Component.literal("Main screen"));
        this.finishedStory = finishedStory;
        this.pause = pause;
        this.playerSession = playerSession;
        userFloodedKeyboard = 0;
    }

    private void playStory() {
        this.onClose();
        StoryHandler storyHandler = new StoryHandler(playerSession);
        minecraft.getSoundManager().stop(musicInstance);
        try {
            List<ErrorLine> errorLines = StoryValidation.validate();
            List<ErrorLine> warns =
                    errorLines.stream().filter(ErrorLine::isWarn).toList();
            List<ErrorLine> errors =
                    errorLines.stream().filter(errorLine -> !errorLine.isWarn()).toList();
            if (errors.isEmpty() && warns.isEmpty()) {
                NarrativeCraftMod.server.execute(storyHandler::start);
                return;
            }
            if (!warns.isEmpty()) {
                ConfirmScreen screen = new ConfirmScreen(
                        t -> {
                            if (!t) {
                                minecraft.setScreen(this);
                                return;
                            }
                            minecraft.setScreen(null);
                            NarrativeCraftMod.server.execute(storyHandler::start);
                        },
                        Component.empty(),
                        Translation.message("screen.main_screen.no_error_but_warns"));
                minecraft.setScreen(screen);
            } else {
                CrashScreen crashScreen = new CrashScreen(
                        playerSession,
                        Translation.message("validation.from_main_screen").getString());
                minecraft.setScreen(crashScreen);
            }
            NarrativeCraftMod.LOGGER.error(" ");
            NarrativeCraftMod.LOGGER.error("Story can't start: ");
            for (ErrorLine errorLine : errorLines) {
                NarrativeCraftMod.LOGGER.error(
                        "{} {} {}", errorLine.getFileName(), errorLine.getLineText(), errorLine.getMessage());
            }
            NarrativeCraftMod.LOGGER.error(" ");
        } catch (Exception e) {
            CrashScreen crashScreen = new CrashScreen(playerSession, e.getMessage());
            minecraft.setScreen(crashScreen);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        if (!pause) {
            minecraft.getSoundManager().stop(musicInstance);
            minecraft.options.hideGui = false;
            if (playerSession.getController() != null) {
                NarrativeCraftMod.server.execute(
                        () -> playerSession.getController().stopSession(false));
            }
        }
    }

    @Override
    protected void init() {
        boolean storyFinished = NarrativeCraftMod.getInstance().getNarrativeWorldOption().finishedStory;
        minecraft.options.hideGui = true;
        showDevBtnCount = 0;
        StorySave save = null;
        try {
            save = NarrativeCraftFile.saveContent();
        } catch (IOException ignored) {
        }
        boolean firstGame = save == null;
        if (!pause && playerSession.getController() == null) {
            try {
                MainScreenData mainScreenData = NarrativeCraftFile.getMainScreenBackground();
                MainScreenController mainScreenController =
                        new MainScreenController(Environment.PRODUCTION, minecraft.player, mainScreenData);
                NarrativeCraftMod.server.execute(mainScreenController::startSession);
            } catch (Exception ignored) {
            }
        }

        if (!pause && !rendered) {
            minecraft.getSoundManager().play(musicInstance);
            rendered = true;
        }

        int totalButtons = storyFinished ? 5 : 4;
        if (pause) totalButtons = 5;
        if (!NarrativeCraftFile.saveExists() && !pause) {
            totalButtons = 3;
        }
        int totalHeight = buttonHeight * totalButtons + gap * (totalButtons - 1);
        initialY = height / 2 - totalHeight / 2;
        if (narrativeCraftLogo.logoExists()) initialY += narrativeCraftLogo.getImageHeight() / 2 + gap;
        int startY = initialY;

        Component playBtnComponent;
        if (firstGame && !pause) {
            playBtnComponent = Translation.message("screen.main_screen.play");
        } else {
            playBtnComponent = Translation.message("screen.main_screen.continue");
        }

        boolean canPlay = !NarrativeCraftMod.getInstance()
                .getChapterManager()
                .getChapters()
                .isEmpty();
        Button playButton = Button.builder(playBtnComponent, button -> {
                    if (pause) {
                        onClose();
                    } else {
                        playStory();
                    }
                })
                .bounds(initialX, startY, buttonWidth, buttonHeight)
                .build();
        playButton.active = canPlay;
        if (!canPlay) {
            playButton.setTooltip(Tooltip.create(Translation.message("screen.main_screen.cant_play_tooltip")));
        }
        this.addRenderableWidget(playButton);

        if (!firstGame && !pause) {
            startY += buttonHeight + gap;
            Button startNewGame = Button.builder(Translation.message("screen.main_screen.new_game"), button -> {
                        ConfirmScreen confirmScreen = new ConfirmScreen(
                                b -> {
                                    if (b) {
                                        NarrativeWorldOption option =
                                                NarrativeCraftMod.getInstance().getNarrativeWorldOption();
                                        NarrativeCraftFile.removeSave();
                                        option.finishedStory = false;
                                        NarrativeCraftFile.updateWorldOptions(option);
                                        playStory();
                                    } else {
                                        minecraft.setScreen(this);
                                    }
                                },
                                Component.literal(""),
                                Translation.message("screen.main_screen.new_game.confirm"),
                                CommonComponents.GUI_YES,
                                CommonComponents.GUI_CANCEL);
                        minecraft.setScreen(confirmScreen);
                    })
                    .bounds(initialX, startY, buttonWidth, buttonHeight)
                    .build();
            startNewGame.active = canPlay;
            if (!canPlay) {
                startNewGame.setTooltip(Tooltip.create(Translation.message("screen.main_screen.cant_play_tooltip")));
            }
            this.addRenderableWidget(startNewGame);
        }

        if (!pause && NarrativeCraftMod.getInstance().getNarrativeWorldOption().finishedStory) {
            startY += buttonHeight + gap;
            Button selectSceneButton = Button.builder(
                            Translation.message("screen.main_screen.select_screen"), button -> {
                                minecraft.getSoundManager().stop(musicInstance);
                                ChapterSelectorScreen screen = new ChapterSelectorScreen(playerSession, this);
                                minecraft.setScreen(screen);
                            })
                    .bounds(initialX, startY, buttonWidth, buttonHeight)
                    .build();
            this.addRenderableWidget(selectSceneButton);
        }

        if (pause) {
            startY += buttonHeight + gap;
            Button loadLastSaveButton = Button.builder(
                            playerSession.getStoryHandler().isDebugMode()
                                    ? Translation.message("screen.main_screen.pause.restart_scene")
                                    : Translation.message("screen.main_screen.pause.load_last_save"),
                            button -> {
                                minecraft.setScreen(null);
                                StoryHandler storyHandler;
                                if (playerSession.getStoryHandler().isDebugMode()) {
                                    storyHandler = new StoryHandler(
                                            playerSession.getChapter(), playerSession.getScene(), playerSession);
                                    storyHandler.setDebugMode(
                                            playerSession.getStoryHandler().isDebugMode());
                                } else {
                                    storyHandler = new StoryHandler(playerSession);
                                }
                                try {
                                    List<ErrorLine> results = StoryValidation.validate();
                                    List<ErrorLine> errorLines =
                                            results.stream().filter(errorLine -> !errorLine.isWarn()).toList();
                                    if (errorLines.isEmpty()) {
                                        NarrativeCraftMod.server.execute(() -> {
                                            playerSession.getStoryHandler().stop();
                                            storyHandler.start();
                                        });
                                    } else {
                                        NarrativeCraftMod.server.execute(() -> {
                                            playerSession.getStoryHandler().stop();
                                        });
                                        for (ErrorLine errorLine : results) {
                                            minecraft.player.displayClientMessage(errorLine.toMessage(), false);
                                        }
                                    }
                                } catch (Exception e) {
                                    Util.sendCrashMessage(minecraft.player, e);
                                }
                            })
                    .bounds(initialX, startY, buttonWidth, buttonHeight)
                    .build();
            this.addRenderableWidget(loadLastSaveButton);

            AbstractController controller = playerSession.getController();
            startY += buttonHeight + gap;
            Button skipCutsceneButton = Button.builder(
                            Translation.message("screen.main_screen.pause.skip_cutscene"), button -> {
                                minecraft.setScreen(null);
                                if (controller instanceof CutsceneController cutsceneController) {
                                    CutscenePlayback cutscenePlayback = cutsceneController.getCutscenePlayback();
                                    if (cutscenePlayback != null) {
                                        playerSession
                                                .getInkActions()
                                                .removeIf(inkAction -> inkAction instanceof FadeInkAction);
                                        NarrativeCraftMod.server.execute(cutsceneController::skip);
                                        cutscenePlayback.stop();
                                        CutsceneKeyframe keyframe = cutsceneController.getLastKeyframeLastGroup();
                                        if (keyframe == null) return;
                                        playerSession.setCurrentCamera(keyframe.getKeyframeLocation());
                                    }
                                }
                            })
                    .bounds(initialX, startY, buttonWidth, buttonHeight)
                    .build();
            skipCutsceneButton.active = controller instanceof CutsceneController && controller != null;
            this.addRenderableWidget(skipCutsceneButton);
        }

        startY += buttonHeight + gap;
        Button optionsButton = Button.builder(Translation.message("screen.main_screen.options"), button -> {
                    minecraft.getSoundManager().stop(musicInstance);
                    MainScreenOptionsScreen screen =
                            new MainScreenOptionsScreen(playerSession, new MainScreen(playerSession, false, pause));
                    minecraft.setScreen(screen);
                })
                .bounds(initialX, startY, buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(optionsButton);

        if (pause) {
            startY += buttonHeight + gap;
            Button quitButton = Button.builder(Translation.message("screen.main_screen.pause.leave"), button -> {
                        boolean debugMod = playerSession.getStoryHandler().isDebugMode();
                        NarrativeCraftMod.server.execute(() -> {
                            playerSession.getStoryHandler().stop();
                            if (NarrativeCraftMod.getInstance().getNarrativeWorldOption().showMainScreen && !debugMod) {
                                MainScreen mainScreen = new MainScreen(playerSession, false, false);
                                minecraft.execute(() -> minecraft.setScreen(mainScreen));
                            } else {
                                minecraft.execute(() -> minecraft.setScreen(null));
                            }
                        });
                    })
                    .bounds(initialX, startY, buttonWidth, buttonHeight)
                    .build();
            this.addRenderableWidget(quitButton);
        } else {
            startY += buttonHeight + gap;
            Button quitButton = Button.builder(Translation.message("screen.main_screen.quit"), button -> {
                        Util.disconnectPlayer(minecraft);
                    })
                    .bounds(initialX, startY, buttonWidth, buttonHeight)
                    .build();
            this.addRenderableWidget(quitButton);
        }

        devButton = Button.builder(Component.literal("Dev Environment"), button -> {
                    minecraft.player.displayClientMessage(Translation.message("global.dev_env"), false);
                    this.onClose();
                })
                .bounds(width - buttonWidth - 10, buttonHeight, buttonWidth, buttonHeight)
                .build();

        if (finishedStory) {
            FinishedStoryScreen screen = new FinishedStoryScreen(playerSession);
            minecraft.setScreen(screen);
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (narrativeCraftLogo.logoExists()) {
            narrativeCraftLogo.render(guiGraphics, initialX, initialY - narrativeCraftLogo.getImageHeight() - gap - 5);
        }
        if (NarrativeCraftMod.getInstance().getChapterManager().getChapters().isEmpty() || userFloodedKeyboard > 20) {
            guiGraphics.drawString(
                    minecraft.font,
                    Translation.message("screen.main_screen.dev_tip").getString(),
                    guiGraphics.guiWidth() / 2
                            - minecraft.font.width(Translation.message("screen.main_screen.dev_tip")) / 2,
                    20,
                    ARGB.colorFromFloat(1, 1, 1, 1));
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (pause) {
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }
        if (playerSession.getController() != null) return;
        if (Util.resourceExists(BACKGROUND_IMAGE)) {
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    BACKGROUND_IMAGE,
                    0,
                    0,
                    0,
                    0,
                    guiGraphics.guiWidth(),
                    guiGraphics.guiHeight(),
                    guiGraphics.guiWidth(),
                    guiGraphics.guiHeight(),
                    ARGB.colorFromFloat(1, 1, 1, 1));
        } else {
            guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), ARGB.colorFromFloat(1, 0, 0, 0));
        }
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}

    @Override
    public boolean isPauseScreen() {
        return pause;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!pause) userFloodedKeyboard++;
        if (event.key() == InputConstants.KEY_LCONTROL && !pause) {
            showDevBtnCount++;
            if (showDevBtnCount == 5) {
                this.addRenderableWidget(devButton);
            }
        }
        if (event.key() == InputConstants.KEY_ESCAPE && !pause) {
            return false;
        }
        return super.keyPressed(event);
    }
}
