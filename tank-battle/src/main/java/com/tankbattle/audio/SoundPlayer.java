package com.tankbattle.audio;

public interface SoundPlayer {
    void play(SoundEffect effect);
    void stopAll();
    boolean isPlaying(SoundEffect effect);
}