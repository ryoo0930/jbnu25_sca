package org.newdawn.spaceinvaders;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundStore {
    private static SoundStore single = new SoundStore();

    public static SoundStore get() {
        return single;
    }

    private HashMap<String, Clip> music = new HashMap<>();
    private HashMap<String, Clip> effects = new HashMap<>();
    private float musicVolume = 1.0f;
    private float effectsVolume = 1.0f;

    public Clip getSound(String ref) {
        return getClip(ref, effectsVolume, effects);
    }

    public Clip getMusic(String ref) {
        return getClip(ref, musicVolume, music);
    }

    private Clip getClip(String ref, float volume, HashMap<String, Clip> map) {
        if (map.get(ref) != null) {
            Clip clip = map.get(ref);
            clip.setFramePosition(0);
            setClipVolume(clip, volume);
            return clip;
        }

        try {
            URL url = this.getClass().getClassLoader().getResource(ref);
            if (url == null) {
                fail("Can't find ref: " + ref);
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            map.put(ref, clip);
            clip.setFramePosition(0);
            setClipVolume(clip, volume);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            fail("Failed to load: " + ref);
        }

        return null;
    }

    public void playSound(String ref) {
        Clip clip = getSound(ref);
        if (clip != null) {
            clip.start();
        }
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = volume;
        for (Clip clip : music.values()) {
            setClipVolume(clip, volume);
        }
    }

    public void setEffectsVolume(float volume) {
        this.effectsVolume = volume;
        for (Clip clip : effects.values()) {
            setClipVolume(clip, volume);
        }
    }

    private void setClipVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (volume == 0.0f) {
                gainControl.setValue(gainControl.getMinimum());
            } else {
                // Convert linear volume (0-1) to logarithmic dB scale
                float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            }
        }
    }

    private void fail(String message) {
        System.err.println(message);
        System.exit(0);
    }
}