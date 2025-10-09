package org.newdawn.spaceinvaders;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundStore {
    private static SoundStore single = new SoundStore();

    public static SoundStore get() {
        return single;
    }

    private HashMap<String, Clip> sounds = new HashMap<>();

    public Clip getSound(String ref) {
        if (sounds.get(ref) != null) {
            Clip clip = sounds.get(ref);
            clip.setFramePosition(0);
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
            sounds.put(ref, clip);
            clip.setFramePosition(0);
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

    private void fail(String message) {
        System.err.println(message);
        System.exit(0);
    }
}
