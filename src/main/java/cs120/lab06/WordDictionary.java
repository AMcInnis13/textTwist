package cs120.lab06;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

/**
 * WordDictionary — pure Java dictionary loader for TextTwist (CS120 Lab06).
 *
 * No JavaFX imports — required for JUnit 4.12 testability without an FX runtime.
 *
 * File format: first line is an integer word count; subsequent lines are uppercase
 * words (one per line). Files live at project root under twister_words/.
 */
public class WordDictionary {

    private final String folderPath;
    private HashSet<String> words;

    /**
     * Constructs a WordDictionary that reads from the given folder.
     *
     * @param folderPath path to the directory containing word files (e.g. "twister_words")
     */
    public WordDictionary(String folderPath) {
        this.folderPath = folderPath;
        this.words = new HashSet<>();
    }

    /**
     * Loads words of the given letter count from {@code folderPath}/{@code letterCount}.txt.
     *
     * The first line of the file is an integer word count (read and discarded).
     * Each subsequent line is an uppercase word; it is trimmed before being added to the set.
     * Clears any previously loaded words so re-initialization works correctly.
     *
     * @param letterCount number of letters in the words to load (3–10)
     * @throws RuntimeException wrapping any IOException if the file cannot be read
     */
    public void initialize(int letterCount) {
        words.clear();
        String path = folderPath + "/" + letterCount + ".txt";
        try (Scanner sc = new Scanner(new File(path))) {
            // First line is the declared word count — read and discard
            if (sc.hasNextLine()) {
                sc.nextLine();
            }
            while (sc.hasNextLine()) {
                String word = sc.nextLine().trim();
                if (!word.isEmpty()) {
                    words.add(word);
                }
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load dictionary from: " + path, e);
        }
    }

    /**
     * Returns true if the given word is present in the loaded dictionary.
     * Input is normalized to uppercase before lookup (matches stored uppercase words).
     *
     * @param wrd the word to check; null or empty returns false
     * @return true if the word exists in the currently loaded word set
     */
    public boolean isValidWord(String wrd) {
        if (wrd == null || wrd.isEmpty()) {
            return false;
        }
        return words.contains(wrd.toUpperCase());
    }

    /**
     * Returns a random word from the currently loaded word set.
     *
     * The {@code n} parameter documents the expected word length (caller must have called
     * {@code initialize(n)} beforehand). This method does NOT re-load the file; it
     * returns a random element from the in-memory set.
     *
     * @param n the letter count that was passed to initialize() — not used for filtering
     * @return a random word from the loaded set
     */
    public String randomWord(int n) {
        ArrayList<String> list = new ArrayList<>(words);
        return list.get(new Random().nextInt(list.size()));
    }
}
