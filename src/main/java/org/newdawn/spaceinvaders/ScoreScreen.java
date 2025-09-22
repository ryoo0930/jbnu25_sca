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

        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        List<Score> highScores = ScoreManager.getHighScores();

        if (highScores.isEmpty()) {
            g.drawString("No scores recorded yet.", (800 - g.getFontMetrics().stringWidth("No scores recorded yet.")) / 2, 250);
        } else {
            int y = 200;
            String header = String.format("%-4s %-15s %-8s %s", "Rank", "Name", "Score", "Diff");
            int x = (800 - g.getFontMetrics().stringWidth(header)) / 2;

            g.drawString(header, x, y);
            y += 40;

            for (int i = 0; i < highScores.size(); i++) {
                Score score = highScores.get(i);
                String difficultyStr = getDifficultyString(score.getDifficulty());
                String scoreText = String.format("%2d.  %-15s %-8d (%s)", i + 1, score.getPlayerName(), score.getScore(), difficultyStr);
                g.drawString(scoreText, x, y);
                y += 40;
            }
        }

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String prompt = "Press Z or Space to return to the main menu";
        g.drawString(prompt, (800 - g.getFontMetrics().stringWidth(prompt)) / 2, 550);
    }

    private String getDifficultyString(int difficulty) {
        switch (difficulty) {
            case 0: return "E";
            case 1: return "N";
            case 2: return "H";
            case 3: return "L";
            default: return "?";
        }
    }
}
