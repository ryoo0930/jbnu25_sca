package org.newdawn.spaceinvaders;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreManager {

    private static final String SCORE_FILE = "scores.dat";
    private static final int MAX_SCORES = 10;

    public static List<Score> getHighScores() {
        List<Score> scores = loadScores();
        sortScores(scores);
        return scores;
    }

    public static void addScore(String playerName, int score) {
        List<Score> scores = loadScores();
        scores.add(new Score(playerName, score));
        sortScores(scores);

        if (scores.size() > MAX_SCORES) {
            scores = new ArrayList<>(scores.subList(0, MAX_SCORES));
        }

        saveScores(scores);
    }

    @SuppressWarnings("unchecked")
    private static List<Score> loadScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORE_FILE))) {
            return (List<Score>) ois.readObject();
        } catch (FileNotFoundException e) {
            return new ArrayList<>(); // Return empty list if file doesn't exist yet
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static void saveScores(List<Score> scores) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORE_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static void sortScores(List<Score> scores) {
        scores.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));
    }
}
