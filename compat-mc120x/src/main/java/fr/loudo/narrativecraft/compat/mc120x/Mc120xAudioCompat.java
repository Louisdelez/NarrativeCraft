/*
 * NarrativeCraft - Create your own stories in Minecraft
 * Copyright (C) 2024 LOUDO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 */
package fr.loudo.narrativecraft.compat.mc120x;

import fr.loudo.narrativecraft.compat.api.IAudioCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

/**
 * Audio compatibility implementation for Minecraft 1.20.x.
 */
public class Mc120xAudioCompat implements IAudioCompat {

    @Override
    public void playSound(String soundId, float volume, float pitch) {
        // Stub
    }

    @Override
    public void playSoundAt(String soundId, double x, double y, double z, float volume, float pitch) {
        // Stub
    }

    @Override
    public void stopAllSounds() {
        Minecraft.getInstance().getSoundManager().stop();
    }

    @Override
    public void stopSoundsInCategory(String category) {
        try {
            SoundSource source = SoundSource.valueOf(category.toUpperCase());
            Minecraft.getInstance().getSoundManager().stop(null, source);
        } catch (IllegalArgumentException e) {
            // Invalid category
        }
    }

    @Override
    public void stopSound(String soundId) {
        // Stub
    }

    @Override
    public boolean isSoundPlaying(String soundId) {
        return false;
    }

    @Override
    public void setMasterVolume(float volume) {
        // Stub
    }

    @Override
    public float getMasterVolume() {
        return Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
    }

    @Override
    public void setCategoryVolume(String category, float volume) {
        // Stub
    }

    @Override
    public float getCategoryVolume(String category) {
        try {
            SoundSource source = SoundSource.valueOf(category.toUpperCase());
            return Minecraft.getInstance().options.getSoundSourceVolume(source);
        } catch (IllegalArgumentException e) {
            return 1.0f;
        }
    }

    @Override
    public void pauseAll() {
        // Stub
    }

    @Override
    public void resumeAll() {
        // Stub
    }

    @Override
    public void fadeOutAll(int durationTicks) {
        stopAllSounds();
    }

    @Override
    public void playMusic(String musicId, int fadeInTicks, boolean loop) {
        // Stub
    }

    @Override
    public void stopMusic(int fadeOutTicks) {
        Minecraft.getInstance().getSoundManager().stop(null, SoundSource.MUSIC);
    }
}
