package org.newdawn.spaceinvaders;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

public class ScoreScreen {

    public void draw(Graphics2D g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, 800, 600);

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("High Scores", (800 - g.getFontMetrics().stringWidth("High Scores")) / 2, 100);

        g.setFont(new Font("Arial", Font.PLAIN, 30));
        List<Score> highScores = ScoreManager.getHighScores();

        if (highScores.isEmpty()) {
            g.drawString("No scores recorded yet.", (800 - g.getFontMetrics().stringWidth("No scores recorded yet.")) / 2, 250);
        } else {
            int y = 200;
            for (int i = 0; i < highScores.size(); i++) {
                Score score = highScores.get(i);
                String scoreText = String.format("%2d. %-15s %d", i + 1, score.getPlayerName(), score.getScore());
                g.drawString(scoreText, 250, y);
                y += 40;
            }
        }

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String prompt = "Press Z or Space to return to the main menu";
        g.drawString(prompt, (800 - g.getFontMetrics().stringWidth(prompt)) / 2, 500);
    }
}
