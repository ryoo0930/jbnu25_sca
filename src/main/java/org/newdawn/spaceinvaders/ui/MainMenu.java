package org.newdawn.spaceinvaders.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class MainMenu {
    /** 메인 메뉴에서 선택 할 수 있는 항목들 */
    private final String[] options = { "Game Start", "Score", "Option", "Quit" };
    /** 초기 선택 항목 */
    private int currentSelection = 0;

    public MainMenu() {
    }

    public void update() {
    }

    public void draw(Graphics2D g) {
        /** 타이틀 */
        g.setFont(new Font("Helvetica", Font.BOLD, 48));
        g.setColor(Color.white);
        String title = "Space Invaders";
        g.drawString(title, (800 - g.getFontMetrics().stringWidth(title)) / 2, 250);

        /** 메뉴 */
        g.setFont(new Font("Helvetica", Font.PLAIN, 20));
        for (int current = 0; current < options.length; current++) {
            if (current == currentSelection) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.gray);
            }
            String optionText = options[current];
            g.drawString(optionText, (800 - g.getFontMetrics().stringWidth(optionText)) / 2, 300 + current * 50);
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
