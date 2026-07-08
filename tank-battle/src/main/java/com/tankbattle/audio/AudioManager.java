package com.tankbattle.audio;

import javax.sound.sampled.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AudioManager implements SoundPlayer {
    private static final Logger logger = Logger.getLogger(AudioManager.class.getName());
    private Map<SoundEffect, Clip> clips = new HashMap<>();

    @Override
    public void play(SoundEffect effect) {
        try {
            String resourcePath = "/sounds/" + effect.name().toLowerCase() + ".wav";
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                logger.fine("Sound file not found: " + resourcePath);
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(is);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            clips.put(effect, clip);
        } catch (Exception e) {
            logger.fine("Failed to play sound: " + effect + " - " + e.getMessage());
        }
    }

    @Override
    public void stopAll() {
        for (Clip clip : clips.values()) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
        clips.clear();
    }

    @Override
    public boolean isPlaying(SoundEffect effect) {
        Clip clip = clips.get(effect);
        return clip != null && clip.isRunning();
    }
}