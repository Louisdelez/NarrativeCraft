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

package fr.loudo.narrativecraft;

import fr.loudo.narrativecraft.managers.*;
import fr.loudo.narrativecraft.options.NarrativeClientOption;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.register.InkActionRegister;
import fr.loudo.narrativecraft.screens.components.NarrativeCraftLogoRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarrativeCraftMod {
    private static final NarrativeCraftMod instance = new NarrativeCraftMod();

    public static final String MOD_ID = "narrativecraft";
    public static final String MOD_NAME = "NarrativeCraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String MAJOR_VERSION = "1.0.0";

    public static boolean firstTime = false;
    public static MinecraftServer server;
    public static RenderType dialogBackgroundRenderType;

    private final CharacterManager characterManager = new CharacterManager();
    private final PlayerSessionManager playerSessionManager = new PlayerSessionManager();
    private final ChapterManager chapterManager = new ChapterManager();
    private final RecordingManager recordingManager = new RecordingManager();
    private final PlaybackManager playbackManager = new PlaybackManager();
    private final NarrativeCraftLogoRenderer narrativeCraftLogoRenderer =
            new NarrativeCraftLogoRenderer(new ResourceLocation(NarrativeCraftMod.MOD_ID, "textures/logo.png"));
    private NarrativeClientOption narrativeClientOptions = new NarrativeClientOption();
    private NarrativeWorldOption narrativeWorldOption = new NarrativeWorldOption();

    public static NarrativeCraftMod getInstance() {
        return instance;
    }

    public static void commonInit() {
        InkActionRegister.register();
    }

    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    public PlayerSessionManager getPlayerSessionManager() {
        return playerSessionManager;
    }

    public ChapterManager getChapterManager() {
        return chapterManager;
    }

    public RecordingManager getRecordingManager() {
        return recordingManager;
    }

    public PlaybackManager getPlaybackManager() {
        return playbackManager;
    }

    public NarrativeCraftLogoRenderer getNarrativeCraftLogoRenderer() {
        return narrativeCraftLogoRenderer;
    }

    public NarrativeClientOption getNarrativeClientOptions() {
        return narrativeClientOptions;
    }

    public NarrativeWorldOption getNarrativeWorldOption() {
        return narrativeWorldOption;
    }

    public void setNarrativeClientOptions(NarrativeClientOption narrativeClientOptions) {
        this.narrativeClientOptions = narrativeClientOptions;
    }

    public void setNarrativeWorldOption(NarrativeWorldOption narrativeWorldOption) {
        this.narrativeWorldOption = narrativeWorldOption;
    }

    public void clearManagers() {
        chapterManager.getChapters().clear();
        playerSessionManager.getPlayerSessions().clear();
        characterManager.getCharacterStories().clear();
        recordingManager.getRecordings().clear();
        playbackManager.getPlaybacks().clear();
    }
}
