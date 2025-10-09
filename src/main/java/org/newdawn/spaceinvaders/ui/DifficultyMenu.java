package org.newdawn.spaceinvaders.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class DifficultyMenu {
    private final String[] options = { "Easy", "Normal", "Hard", "Lunatic" };

    private int currentSelection = 0;

    public DifficultyMenu() {
    }

    public void draw(Graphics2D g) {
        g.setFont(new Font("Helvetica", Font.PLAIN, 20));
        for (int current = 0; current < options.length; current++) {
            if (current == currentSelection) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.gray);
            }
            String optionText = options[current];
            g.drawString(optionText, (800 - g.getFontMetrics().stringWidth(optionText)) / 2, 250 + current * 50);
        }
    }

    /** 위로 이동하기 */
    public void moveUp() {
        currentSelection--;
        if (currentSelection < 0) {
            currentSelection = options.length - 1;
        }
    }

    /** 아래로 이동하기 */
    public void moveDown() {
        currentSelection++;
        if (currentSelection >= options.length) {
            currentSelection = 0;
        }
    }

    /** 선택 */
    public int getSelection() {
        return currentSelection;
    }

}
