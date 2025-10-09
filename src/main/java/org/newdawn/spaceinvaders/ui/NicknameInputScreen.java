package org.newdawn.spaceinvaders.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class NicknameInputScreen {
    private final char[][] keyLayout = {
            { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L' },
            { 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X' },
            { 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' },
            { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '←', '↵' }
    };
    private int cursorX = 0;
    private int cursorY = 0;
    private StringBuilder nickname;
    private final int MAX_LENGTH = 10;

    public NicknameInputScreen() {
        this.nickname = new StringBuilder();
    }

    public void draw(Graphics2D g){
        g.setColor(Color.white);
        g.setFont(new Font("Monospaced", Font.PLAIN, 24));

        g.drawString("Enter Your Nickname", 290, 200);
        g.drawRect(250, 220, 300, 40);
        g.drawString(nickname.toString(), 260, 245);

        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        for(int row = 0; row < keyLayout.length; row++){
            for(int col = 0; col < keyLayout[row].length; col++){
                char key = keyLayout[row][col];
                if(key == ' ') continue;

                String toDraw = (key == '←') ? "BACK" : (key == '↵') ? "ENTER" : String.valueOf(key);
                if (row == cursorY && col == cursorX) {
                    g.setColor(Color.yellow);
                } else {
                    g.setColor(Color.white);
                }
                g.drawString(toDraw, 200 + col * 35, 380 + row * 30);
            }
        }
    }

    public void moveUp() {
        if (cursorY > 0)
            cursorY--;
    }

    public void moveDown() {
        if (cursorY < keyLayout.length - 1)
            cursorY++;
    }

    public void moveLeft() {
        if (cursorX > 0)
            cursorX--;
    }

    public void moveRight() {
        if (cursorX < keyLayout[0].length - 1)
            cursorX++;
    }

    public boolean processSelection() {
        char key = keyLayout[cursorY][cursorX];
        if (key == '↵')
            return true;
        if (key == '←') {
            if (nickname.length() > 0)
                nickname.deleteCharAt(nickname.length() - 1);
        } else if (key != ' ' && nickname.length() < MAX_LENGTH) {
            nickname.append(key);
        }
        return false;
    }

    public String getNickname() {
        return nickname.toString();
    }

}
