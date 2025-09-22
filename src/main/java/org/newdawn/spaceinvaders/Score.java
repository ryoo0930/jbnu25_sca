package org.newdawn.spaceinvaders;

import java.io.Serializable;

public class Score implements Serializable {
    private static final long serialVersionUID = 1L;
    private String playerName;
    private int score;
    private int difficulty;

    public Score(String playerName, int score, int difficulty) {
        this.playerName = playerName;
        this.score = score;
        this.difficulty = difficulty;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public int getDifficulty(){
        return difficulty;
    }

    @Override
    public String toString() {
        return playerName + ": " + score;
    }
}