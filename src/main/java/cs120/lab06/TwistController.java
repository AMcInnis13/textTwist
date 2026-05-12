package cs120.lab06;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * TwistController — pure-Java game model for TextTwist (CS120 Lab06).
 *
 * No JavaFX imports — required for JUnit 4.12 testability without an FX runtime.
 *
 * Manages score, level, target word, and guess tracking. PrimaryController
 * (Phase 3) drives level progression and timer; this class is a pure state engine.
 */
public class TwistController {

    private int score;
    private int levelScore;
    private int level;
    private String targetWord;
    private WordDictionary myDictionary;
    private HashSet<String> guessedWords;

    /**
     * Constructs a TwistController using word files in the given folder.
     *
     * @param folderPath path to the directory containing word files (e.g. "twister_words")
     */
    public TwistController(String folderPath) {
        myDictionary = new WordDictionary(folderPath);
        level = 1;
        score = 0;
        levelScore = 0;
        guessedWords = new HashSet<>();
    }

    /**
     * Begins a new episode with words of the given letter count.
     *
     * Loads the word file for {@code letterCount}, picks a random target word,
     * resets levelScore, and clears the guessed-word set. The cumulative {@code score}
     * is NOT reset — it persists across episodes.
     *
     * @param letterCount number of letters for this episode (3–10)
     */
    public void beginEpisode(int letterCount) {
        myDictionary.initialize(letterCount);
        targetWord = myDictionary.randomWord(letterCount);
        levelScore = 0;
        guessedWords = new HashSet<>();
    }

    /**
     * Checks whether the given word is a valid, non-duplicate guess.
     *
     * Scoring formula: n * n * 10 where n = length of the guessed word.
     * Returns 0 for null, empty, duplicate, or invalid words.
     * Does NOT check whether the letters are a subset of targetWord (Phase 3 concern).
     *
     * @param wrd the word guessed by the player
     * @return points earned (0 if invalid or duplicate)
     */
    public int checkGuessWord(String wrd) {
        if (wrd == null || wrd.isEmpty()) {
            return 0;
        }
        String upper = wrd.toUpperCase();
        if (guessedWords.contains(upper)) {
            return 0;
        }
        if (!myDictionary.isValidWord(upper)) {
            return 0;
        }
        int n = upper.length();
        int points = n * n * 10;
        guessedWords.add(upper);
        score += points;
        levelScore += points;
        return points;
    }

    /**
     * Returns the letters of the current target word as a shuffled List of single-character strings.
     *
     * Each character of targetWord is converted to a one-character String element.
     * The list is shuffled in-place with a fresh Random instance.
     *
     * @return shuffled list of single-character strings representing the target word's letters
     */
    public List<String> shuffleLetters() {
        List<String> list = new ArrayList<>();
        for (char c : targetWord.toCharArray()) {
            list.add(String.valueOf(c));
        }
        Collections.shuffle(list, new Random());
        return list;
    }

    /** @return cumulative score across all episodes */
    public int getScore() {
        return score;
    }

    /** @return score earned in the current episode only */
    public int getLevelScore() {
        return levelScore;
    }

    /** @return current level (starts at 1; incremented by PrimaryController in Phase 3) */
    public int getLevel() {
        return level;
    }

    /** @return the target word for the current episode */
    public String getTargetWord() {
        return targetWord;
    }
}
