package org.newdawn.spaceinvaders;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class OptionScreen {
    private final String[] options = { "Music Volume", "Effects Volume", "Back" };
    private int currentSelection = 0;
    private float musicVolume = 1.0f;
    private float effectsVolume = 1.0f;

    public void draw(Graphics2D g) {
        g.setFont(new Font("Helvetica", Font.BOLD, 48));
        g.setColor(Color.white);
        String title = "Options";
        g.drawString(title, (800 - g.getFontMetrics().stringWidth(title)) / 2, 250);

        g.setFont(new Font("Helvetica", Font.PLAIN, 20));
        for (int i = 0; i < options.length; i++) {
            if (i == currentSelection) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.gray);
            }
            String optionText = options[i];
            if (i == 0) {
                optionText += ": " + (int) (musicVolume * 100) + "%";
            } else if (i == 1) {
                optionText += ": " + (int) (effectsVolume * 100) + "%";
            }
            g.drawString(optionText, (800 - g.getFontMetrics().stringWidth(optionText)) / 2, 300 + i * 50);
        }
    }

    public void moveUp() {
        currentSelection--;
        if (currentSelection < 0) {
            currentSelection = options.length - 1;
        }
    }

    public void moveDown() {
        currentSelection++;
        if (currentSelection >= options.length) {
            currentSelection = 0;
        }
    }

    public void increaseVolume() {
        if (currentSelection == 0) {
            musicVolume += 0.1f;
            if (musicVolume > 1.0f) {
                musicVolume = 1.0f;
            }
            SoundStore.get().setMusicVolume(musicVolume);
        } else if (currentSelection == 1) {
            effectsVolume += 0.1f;
            if (effectsVolume > 1.0f) {
                effectsVolume = 1.0f;
            }
            SoundStore.get().setEffectsVolume(effectsVolume);
        }
    }

    public void decreaseVolume() {
        if (currentSelection == 0) {
            musicVolume -= 0.1f;
            if (musicVolume < 0.0f) {
                musicVolume = 0.0f;
            }
            SoundStore.get().setMusicVolume(musicVolume);
        } else if (currentSelection == 1) {
            effectsVolume -= 0.1f;
            if (effectsVolume < 0.0f) {
                effectsVolume = 0.0f;
            }
            SoundStore.get().setEffectsVolume(effectsVolume);
        }
    }

    public int getSelection() {
        return currentSelection;
    }
}
