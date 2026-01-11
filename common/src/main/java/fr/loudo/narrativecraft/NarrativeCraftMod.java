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

import fr.loudo.narrativecraft.compat.api.IColorCompat;
import fr.loudo.narrativecraft.compat.api.IResourceCompat;
import fr.loudo.narrativecraft.compat.api.IUtilCompat;
import fr.loudo.narrativecraft.compat.api.NcId;
import fr.loudo.narrativecraft.compat.api.RenderChannel;
import fr.loudo.narrativecraft.compat.api.VersionAdapterLoader;
import fr.loudo.narrativecraft.managers.*;
import fr.loudo.narrativecraft.narrative.state.NarrativeStateManager;
import fr.loudo.narrativecraft.narrative.state.NarrativeStateManagerImpl;
import fr.loudo.narrativecraft.options.NarrativeClientOption;
import fr.loudo.narrativecraft.options.NarrativeWorldOption;
import fr.loudo.narrativecraft.register.InkActionRegister;
import fr.loudo.narrativecraft.screens.components.NarrativeCraftLogoRenderer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarrativeCraftMod {
    private static final NarrativeCraftMod instance = new NarrativeCraftMod();

    public static final String MOD_ID = "narrativecraft";
    public static final String MOD_NAME = "NarrativeCraft";
    public static final String MAJOR_VERSION = "1.2.0";

    /**
     * Multi-version support marker. When present in all JARs, confirms
     * that common/ code propagates correctly to all build targets.
     * Added in v1.2.0 for cross-version compatibility validation.
     */
    public static final String MULTI_VERSION_BUILD = "5-target-v1.2.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static boolean firstTime = false;
    public static MinecraftServer server;

    /**
     * Gets a render type for the specified channel.
     * The returned object must be cast to the appropriate MC RenderType by the caller.
     *
     * @param channel The render channel
     * @return the render type object (version-specific implementation)
     */
    @SuppressWarnings("unchecked")
    public static <T> T getRenderType(RenderChannel channel) {
        return (T) VersionAdapterLoader.getAdapter().getRenderType(channel);
    }

    /**
     * Gets the color compatibility layer for ARGB operations.
     * Use this instead of direct net.minecraft.util.ARGB calls for cross-version compatibility.
     *
     * @return the color compat implementation
     */
    public static IColorCompat getColorCompat() {
        return VersionAdapterLoader.getAdapter().getColorCompat();
    }

    /**
     * Gets the resource compatibility layer for Identifier/Identifier operations.
     *
     * @return the resource compat implementation
     */
    public static IResourceCompat getResourceCompat() {
        return VersionAdapterLoader.getAdapter().getResourceCompat();
    }

    /**
     * Gets the utility compatibility layer for version-specific utility methods.
     *
     * @return the util compat implementation
     */
    public static IUtilCompat getUtilCompat() {
        return VersionAdapterLoader.getAdapter().getUtilCompat();
    }

    private final CharacterManager characterManager = new CharacterManager();
    private final PlayerSessionManager playerSessionManager = new PlayerSessionManager();
    private final ChapterManager chapterManager = new ChapterManager();
    private final RecordingManager recordingManager = new RecordingManager();
    private final PlaybackManager playbackManager = new PlaybackManager();
    private final NarrativeStateManagerImpl narrativeStateManager = new NarrativeStateManagerImpl();
    private final NarrativeCraftLogoRenderer narrativeCraftLogoRenderer = new NarrativeCraftLogoRenderer(
            NcId.of(NarrativeCraftMod.MOD_ID, "textures/logo.png"));
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

    /**
     * Returns the central state manager for the narrative system.
     * Use this to manage state transitions and cleanup handlers.
     *
     * @return the narrative state manager
     */
    public NarrativeStateManager getNarrativeStateManager() {
        return narrativeStateManager;
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
        narrativeStateManager.reset();
        chapterManager.getChapters().clear();
        playerSessionManager.getPlayerSessions().clear();
        characterManager.getCharacterStories().clear();
        recordingManager.getRecordings().clear();
        playbackManager.getPlaybacks().clear();
    }
}
