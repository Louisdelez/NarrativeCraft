package fr.loudo.narrativecraft.compat.api;

/**
 * Audio compatibility layer for version-specific sound APIs.
 *
 * Abstracts differences in sound engine access between Minecraft versions.
 * Used for narrative audio control (music, dialog sounds, ambient).
 *
 * Usage:
 *   IAudioCompat compat = adapter.getAudioCompat();
 *   compat.playSound(soundId, volume, pitch);
 *   compat.setMasterVolume(0.5f);
 */
public interface IAudioCompat {

    /**
     * Play a sound effect.
     *
     * @param soundId Sound resource location (e.g., "minecraft:entity.player.levelup")
     * @param volume Volume (0.0 to 1.0)
     * @param pitch Pitch multiplier (0.5 to 2.0 typical)
     */
    void playSound(String soundId, float volume, float pitch);

    /**
     * Play a positioned sound effect in the world.
     *
     * @param soundId Sound resource location
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param volume Volume (0.0 to 1.0)
     * @param pitch Pitch multiplier
     */
    void playSoundAt(String soundId, double x, double y, double z, float volume, float pitch);

    /**
     * Stop all currently playing sounds.
     */
    void stopAllSounds();

    /**
     * Stop sounds from a specific category.
     *
     * @param category Sound category (e.g., "music", "ambient", "voice")
     */
    void stopSoundsInCategory(String category);

    /**
     * Stop a specific sound by ID.
     *
     * @param soundId Sound resource location to stop
     */
    void stopSound(String soundId);

    /**
     * Check if a sound is currently playing.
     *
     * @param soundId Sound resource location
     * @return true if the sound is playing
     */
    boolean isSoundPlaying(String soundId);

    /**
     * Set the master volume for all sounds.
     *
     * @param volume Volume (0.0 to 1.0)
     */
    void setMasterVolume(float volume);

    /**
     * Get the current master volume.
     *
     * @return Current master volume (0.0 to 1.0)
     */
    float getMasterVolume();

    /**
     * Set volume for a specific sound category.
     *
     * @param category Sound category
     * @param volume Volume (0.0 to 1.0)
     */
    void setCategoryVolume(String category, float volume);

    /**
     * Get volume for a specific sound category.
     *
     * @param category Sound category
     * @return Current category volume (0.0 to 1.0)
     */
    float getCategoryVolume(String category);

    /**
     * Pause all sounds (for cutscene transitions).
     */
    void pauseAll();

    /**
     * Resume all paused sounds.
     */
    void resumeAll();

    /**
     * Fade out all sounds over a duration.
     *
     * @param durationTicks Duration in game ticks
     */
    void fadeOutAll(int durationTicks);

    /**
     * Play background music for narrative scenes.
     *
     * @param musicId Music resource location
     * @param fadeInTicks Fade-in duration in ticks
     * @param loop Whether to loop the music
     */
    void playMusic(String musicId, int fadeInTicks, boolean loop);

    /**
     * Stop current narrative music with fade-out.
     *
     * @param fadeOutTicks Fade-out duration in ticks
     */
    void stopMusic(int fadeOutTicks);
}
