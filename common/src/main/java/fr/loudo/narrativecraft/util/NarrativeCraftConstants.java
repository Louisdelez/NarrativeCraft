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

package fr.loudo.narrativecraft.util;

/**
 * Central constants for NarrativeCraft.
 *
 * Groups magic numbers and commonly used values to improve maintainability.
 */
public final class NarrativeCraftConstants {

    private NarrativeCraftConstants() {
        // Utility class
    }

    // ========================
    // Timing & Ticks
    // ========================

    /** Ticks per second in Minecraft */
    public static final int TICKS_PER_SECOND = 20;

    /** Default wait duration for transitions (ticks) */
    public static final int DEFAULT_TRANSITION_TICKS = 20;

    /** Default fade duration in seconds */
    public static final float DEFAULT_FADE_DURATION = 1.0f;

    // ========================
    // Dialog
    // ========================

    /** Default dialog box width (pixels) */
    public static final int DEFAULT_DIALOG_WIDTH = 400;

    /** Default dialog scale */
    public static final float DEFAULT_DIALOG_SCALE = 1.0f;

    /** Default text color (white) */
    public static final int DEFAULT_TEXT_COLOR = 0xFFFFFF;

    /** Default dialog background opacity (0.0 - 1.0) */
    public static final float DEFAULT_DIALOG_OPACITY = 0.8f;

    /** Minimum dialog width */
    public static final int MIN_DIALOG_WIDTH = 100;

    /** Maximum dialog width */
    public static final int MAX_DIALOG_WIDTH = 800;

    // ========================
    // Screen Effects
    // ========================

    /** Default shake strength */
    public static final float DEFAULT_SHAKE_STRENGTH = 1.0f;

    /** Default shake decay rate */
    public static final float DEFAULT_SHAKE_DECAY = 0.9f;

    /** Default shake speed */
    public static final float DEFAULT_SHAKE_SPEED = 0.3f;

    /** Default border size (pixels) */
    public static final int DEFAULT_BORDER_SIZE = 50;

    /** Default border transition duration (seconds) */
    public static final float DEFAULT_BORDER_TRANSITION = 0.5f;

    // ========================
    // Audio
    // ========================

    /** Default sound volume (0.0 - 1.0) */
    public static final float DEFAULT_SOUND_VOLUME = 1.0f;

    /** Default sound pitch (0.5 - 2.0) */
    public static final float DEFAULT_SOUND_PITCH = 1.0f;

    /** Default music fade duration (seconds) */
    public static final float DEFAULT_MUSIC_FADE = 2.0f;

    // ========================
    // Recording/Playback
    // ========================

    /** Maximum recording duration (ticks) - ~30 minutes */
    public static final int MAX_RECORDING_TICKS = 36000;

    /** Entity tracking radius (blocks) */
    public static final double ENTITY_TRACKING_RADIUS = 32.0;

    /** Recording position precision (decimal places) */
    public static final int RECORDING_POSITION_PRECISION = 4;

    // ========================
    // Area Triggers
    // ========================

    /** Debounce ticks for area trigger re-entry */
    public static final int TRIGGER_DEBOUNCE_TICKS = 10;

    /** Minimum area trigger volume (blocks cubed) */
    public static final double MIN_TRIGGER_VOLUME = 1.0;

    // ========================
    // Performance
    // ========================

    /** Initial capacity for ink action lists */
    public static final int INK_ACTION_LIST_INITIAL_CAPACITY = 16;

    /** Initial capacity for playback lists */
    public static final int PLAYBACK_LIST_INITIAL_CAPACITY = 8;

    /** Initial capacity for tracked entity sets */
    public static final int TRACKED_ENTITIES_INITIAL_CAPACITY = 16;

    /** Maximum concurrent recordings */
    public static final int MAX_CONCURRENT_RECORDINGS = 10;

    /** Maximum concurrent playbacks */
    public static final int MAX_CONCURRENT_PLAYBACKS = 20;

    // ========================
    // Validation
    // ========================

    /** Maximum typo suggestion distance (Levenshtein) */
    public static final int MAX_TYPO_DISTANCE = 3;

    /** Maximum error messages per batch */
    public static final int MAX_ERRORS_PER_BATCH = 50;

    // ========================
    // File Limits
    // ========================

    /** Maximum Ink file size (bytes) - 1MB */
    public static final long MAX_INK_FILE_SIZE = 1024 * 1024;

    /** Maximum story name length */
    public static final int MAX_STORY_NAME_LENGTH = 64;

    /** Maximum scene name length */
    public static final int MAX_SCENE_NAME_LENGTH = 64;

    // ========================
    // Time of Day (Minecraft ticks)
    // ========================

    /** Minecraft day start (0) */
    public static final int TIME_DAY = 0;

    /** Minecraft noon (6000) */
    public static final int TIME_NOON = 6000;

    /** Minecraft night start (13000) */
    public static final int TIME_NIGHT = 13000;

    /** Minecraft midnight (18000) */
    public static final int TIME_MIDNIGHT = 18000;
}
