/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.api;

/**
 * Audio compatibility layer for version-specific sound APIs.
 *
 * Pure Java interface - no MC dependencies.
 */
public interface IAudioCompat {

    // ===== Sound Playback =====

    void playSound(String soundId, float volume, float pitch);

    void playSoundAt(String soundId, double x, double y, double z, float volume, float pitch);

    void stopAllSounds();

    void stopSoundsInCategory(String category);

    void stopSound(String soundId);

    boolean isSoundPlaying(String soundId);

    // ===== Volume Control =====

    void setMasterVolume(float volume);

    float getMasterVolume();

    void setCategoryVolume(String category, float volume);

    float getCategoryVolume(String category);

    // ===== Sound Engine State =====

    void pauseAll();

    void resumeAll();

    void fadeOutAll(int durationTicks);

    // ===== Music =====

    void playMusic(String musicId, int fadeInTicks, boolean loop);

    void stopMusic(int fadeOutTicks);
}
